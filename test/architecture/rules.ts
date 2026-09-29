import { ImportRef, SourceFile } from './source-graph';

// Each rule returns violations as readable lines; an empty list passes.
// Paths are relative to `src`. See docs/ARCHITECTURE.md for the reasons.

export type Layer = 'domain' | 'application' | 'adapter' | 'support' | 'root';

export function layerOf(file: string): Layer {
  const [top, ...rest] = file.split('/');
  if (rest.length === 0) return 'root';
  if (
    top === 'domain' ||
    top === 'application' ||
    top === 'adapter' ||
    top === 'support'
  )
    return top;
  return 'root';
}

/** `application/<slice>/...` → slice; `domain/<aggregate>/...` → aggregate. */
export function unitOf(file: string): string | undefined {
  const [top, unit, ...rest] = file.split('/');
  return (top === 'application' || top === 'domain') && rest.length
    ? unit
    : undefined;
}

/** Where a file sits inside its application slice. */
export function slicePartOf(
  file: string,
): 'provided' | 'required' | 'root' | undefined {
  const [top, slice, part, ...rest] = file.split('/');
  if (top !== 'application' || !slice || slice === 'shared' || !part)
    return undefined;
  if (rest.length === 0) return 'root';
  return part === 'provided' || part === 'required' ? part : undefined;
}

function packageOf(specifier: string): string {
  const parts = specifier.split('/');
  return specifier.startsWith('@')
    ? parts.slice(0, 2).join('/')
    : (parts[0] ?? specifier);
}

function internal(file: SourceFile): (ImportRef & { target: string })[] {
  return file.imports.filter(
    (ref): ref is ImportRef & { target: string } => ref.target !== undefined,
  );
}

function external(file: SourceFile): ImportRef[] {
  return file.imports.filter((ref) => ref.target === undefined);
}

const under = (file: string, ...prefixes: string[]) =>
  prefixes.some((prefix) => file.startsWith(prefix));

const LAYER_ACCESS: Record<Layer, Layer[]> = {
  domain: ['domain', 'support'],
  application: ['domain', 'application', 'support'],
  adapter: ['domain', 'application', 'adapter', 'support'],
  support: ['support'],
  root: ['domain', 'application', 'adapter', 'support', 'root'],
};

/** adapter → application → domain; support is shared by all; nothing imports the root. */
export function layerDependencies(files: SourceFile[]): string[] {
  const violations: string[] = [];
  for (const file of files) {
    const from = layerOf(file.path);
    for (const ref of internal(file)) {
      const to = layerOf(ref.target);
      if (!LAYER_ACCESS[from].includes(to))
        violations.push(`${file.path} (${from}) imports ${ref.target} (${to})`);
      // Domain stays free of Nest, which support/stereotype and describe-cause load.
      if (
        from === 'domain' &&
        to === 'support' &&
        ref.target !== 'support/errors.ts'
      )
        violations.push(`${file.path} (domain) imports ${ref.target}`);
    }
  }
  return violations;
}

const EXTERNAL_ACCESS: Partial<Record<Layer, string[]>> = {
  domain: ['typeorm'],
  application: [
    'typeorm',
    'class-validator',
    'class-transformer',
    '@nestjs/common',
    'node:crypto',
  ],
  support: ['@nestjs/common'],
};

// HTTP exceptions, guards and controllers belong to the web adapter.
const APPLICATION_NEST_NAMES = ['Logger'];

/** The core only uses persistence, validation and logging libraries. */
export function coreDependencies(files: SourceFile[]): string[] {
  const violations: string[] = [];
  for (const file of files) {
    const layer = layerOf(file.path);
    const allowed = EXTERNAL_ACCESS[layer];
    if (!allowed) continue;
    for (const ref of external(file)) {
      if (!allowed.includes(packageOf(ref.specifier)))
        violations.push(`${file.path} (${layer}) imports ${ref.specifier}`);
      else if (layer === 'application' && ref.specifier === '@nestjs/common')
        for (const name of ref.names)
          if (!APPLICATION_NEST_NAMES.includes(name))
            violations.push(`${file.path} imports ${name} from @nestjs/common`);
    }
    if (
      (layer === 'domain' || layer === 'application') &&
      /\bfetch\(|process\.env/.test(file.text)
    )
      violations.push(
        `${file.path} calls fetch or reads process.env outside an adapter`,
      );
  }
  return violations;
}

/**
 * A slice reaches another slice only through its provided ports;
 * application/shared is open to all and depends on no slice.
 */
export function sliceBoundaries(files: SourceFile[]): string[] {
  const violations: string[] = [];
  for (const file of files) {
    if (layerOf(file.path) !== 'application') continue;
    const from = unitOf(file.path);
    for (const ref of internal(file)) {
      if (layerOf(ref.target) !== 'application') continue;
      const to = unitOf(ref.target);
      if (to === 'shared' || to === from) continue;
      if (from === 'shared')
        violations.push(
          `${file.path} (shared) imports slice file ${ref.target}`,
        );
      else if (slicePartOf(ref.target) !== 'provided')
        violations.push(
          `${file.path} imports ${ref.target}, which is not a provided port of ${to}`,
        );
    }
  }
  return violations;
}

/** Ports are contracts: interfaces and request/response data, never implementations. */
export function portContracts(files: SourceFile[]): string[] {
  const violations: string[] = [];
  for (const file of files) {
    const part = slicePartOf(file.path);
    if (part !== 'provided' && part !== 'required') continue;
    const slice = unitOf(file.path);
    for (const ref of internal(file)) {
      const ok =
        layerOf(ref.target) === 'domain' ||
        ref.target === 'support/errors.ts' ||
        under(ref.target, 'application/shared/') ||
        slicePartOf(ref.target) === 'provided' ||
        (part === 'required' &&
          unitOf(ref.target) === slice &&
          slicePartOf(ref.target) === 'required');
      if (!ok)
        violations.push(`${file.path} (${part} port) imports ${ref.target}`);
    }
    const packages =
      part === 'provided'
        ? ['class-validator', 'class-transformer']
        : ['typeorm'];
    for (const ref of external(file))
      if (!packages.includes(packageOf(ref.specifier)))
        violations.push(`${file.path} (${part} port) imports ${ref.specifier}`);
  }
  return violations;
}

function cycles(files: SourceFile[], layer: 'domain' | 'application') {
  const graph = new Map<string, Set<string>>();
  for (const file of files) {
    const from = layerOf(file.path) === layer ? unitOf(file.path) : undefined;
    if (!from) continue;
    const edges = graph.get(from) ?? new Set<string>();
    graph.set(from, edges);
    for (const ref of internal(file)) {
      const to = layerOf(ref.target) === layer ? unitOf(ref.target) : undefined;
      if (to && to !== from) edges.add(to);
    }
  }
  const found: string[] = [];
  const done = new Set<string>();
  const visit = (node: string, trail: string[]) => {
    const start = trail.indexOf(node);
    if (start >= 0) {
      found.push(
        `${layer} cycle: ${[...trail.slice(start), node].join(' → ')}`,
      );
      return;
    }
    if (done.has(node)) return;
    for (const next of graph.get(node) ?? []) visit(next, [...trail, node]);
    done.add(node);
  };
  for (const node of graph.keys()) visit(node, []);
  return found;
}

export function noCycles(files: SourceFile[]): string[] {
  return [...cycles(files, 'domain'), ...cycles(files, 'application')];
}

const PLACEMENT: [RegExp, (file: string) => boolean, string][] = [
  [
    /@(WebApiAdapter|Controller)\(/,
    (file) => under(file, 'adapter/webapi/'),
    'controllers belong in adapter/webapi',
  ],
  [
    /@ApplicationService\(/,
    (file) => slicePartOf(file) === 'root',
    'application services belong in an application slice root',
  ],
  [
    /@Adapter\(/,
    (file) => under(file, 'adapter/'),
    'adapters belong in adapter',
  ],
  [
    /@(Module|Global)\(/,
    (file) => under(file, 'adapter/config/') || file === 'app.module.ts',
    'Nest modules belong in adapter/config',
  ],
  [
    /@Injectable\(/,
    (file) => layerOf(file) === 'adapter' || layerOf(file) === 'support',
    'use @ApplicationService in application',
  ],
  [
    /\bextends\s+Repository</,
    (file) => slicePartOf(file) === 'required',
    'repository ports belong in application/<slice>/required',
  ],
];

/** Stereotypes, Nest modules and repository ports live where their role says. */
export function rolePlacement(files: SourceFile[]): string[] {
  return files.flatMap((file) =>
    PLACEMENT.filter(
      ([pattern, allowed]) => pattern.test(file.text) && !allowed(file.path),
    ).map(([, , reason]) => `${file.path}: ${reason}`),
  );
}

/**
 * Controllers call provided ports only; outbound adapters implement
 * required ports only. Neither reaches into a slice's services.
 */
export function adapterDependencies(files: SourceFile[]): string[] {
  const violations: string[] = [];
  for (const file of files) {
    const webApi = under(file.path, 'adapter/webapi/');
    const outbound = under(
      file.path,
      'adapter/integration/',
      'adapter/security/',
    );
    if (!webApi && !outbound) continue;
    for (const ref of internal(file)) {
      if (under(ref.target, 'application/shared/')) continue;
      const part = slicePartOf(ref.target);
      if (
        layerOf(ref.target) === 'application' &&
        part !== (webApi ? 'provided' : 'required')
      )
        violations.push(`${file.path} imports ${ref.target}`);
      if (
        webApi &&
        under(ref.target, 'adapter/') &&
        !under(ref.target, 'adapter/webapi/', 'adapter/config/')
      )
        violations.push(`${file.path} imports outbound adapter ${ref.target}`);
    }
  }
  return violations;
}

/** A provided port is implemented by a service in the root of its own slice. */
export function portImplementations(files: SourceFile[]): string[] {
  const violations: string[] = [];
  for (const file of files)
    for (const declared of file.classes)
      for (const name of declared.implements) {
        const port = internal(file).find((ref) => ref.names.includes(name));
        if (!port) continue;
        const part = slicePartOf(port.target);
        if (
          part === 'provided' &&
          (slicePartOf(file.path) !== 'root' ||
            unitOf(file.path) !== unitOf(port.target))
        )
          violations.push(
            `${file.path}: ${declared.name} implements provided ${name} outside its slice root`,
          );
        if (
          part === 'required' &&
          layerOf(file.path) !== 'adapter' &&
          !(
            slicePartOf(file.path) === 'root' &&
            unitOf(file.path) !== unitOf(port.target)
          )
        )
          violations.push(
            `${file.path}: ${declared.name} implements required ${name} outside an adapter or another slice`,
          );
      }
  return violations;
}

export const rules: Record<string, (files: SourceFile[]) => string[]> = {
  layerDependencies,
  coreDependencies,
  sliceBoundaries,
  portContracts,
  noCycles,
  rolePlacement,
  adapterDependencies,
  portImplementations,
};
