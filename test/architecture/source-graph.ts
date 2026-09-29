import { readdirSync, readFileSync } from 'node:fs';
import path from 'node:path';
import ts from 'typescript';

export interface ImportRef {
  specifier: string;
  /** Imported binding names; `*` for a namespace import. */
  names: string[];
  /** The imported file relative to `src`, for a relative import. */
  target?: string;
}

export interface ClassRef {
  name: string;
  /** Names in the `implements` clause. */
  implements: string[];
}

export interface SourceFile {
  /** Relative to `src`, with `/` separators. */
  path: string;
  /** The source without comments, so a role mentioned in a comment is not a use. */
  code: string;
  imports: ImportRef[];
  classes: ClassRef[];
}

function importNames(clause: ts.ImportClause | undefined): string[] {
  if (!clause) return [];
  const names = clause.name ? [clause.name.text] : [];
  const bindings = clause.namedBindings;
  if (bindings && ts.isNamespaceImport(bindings)) names.push('*');
  else if (bindings)
    for (const element of bindings.elements)
      names.push((element.propertyName ?? element.name).text);
  return names;
}

export function parseSource(filePath: string, text: string): SourceFile {
  const source = ts.createSourceFile(
    filePath,
    text,
    ts.ScriptTarget.Latest,
    true,
  );
  const imports: ImportRef[] = [];
  const classes: ClassRef[] = [];
  for (const statement of source.statements) {
    if (
      (ts.isImportDeclaration(statement) ||
        ts.isExportDeclaration(statement)) &&
      statement.moduleSpecifier &&
      ts.isStringLiteral(statement.moduleSpecifier)
    ) {
      const specifier = statement.moduleSpecifier.text;
      imports.push({
        specifier,
        names: ts.isImportDeclaration(statement)
          ? importNames(statement.importClause)
          : [],
        target: specifier.startsWith('.')
          ? `${path.posix.join(path.posix.dirname(filePath), specifier)}.ts`
          : undefined,
      });
    }
    if (ts.isClassDeclaration(statement) && statement.name)
      classes.push({
        name: statement.name.text,
        implements: (statement.heritageClauses ?? [])
          .filter((clause) => clause.token === ts.SyntaxKind.ImplementsKeyword)
          .flatMap((clause) => clause.types)
          .map((type) => type.expression.getText(source)),
      });
  }
  const code = ts.createPrinter({ removeComments: true }).printFile(source);
  return { path: filePath, code, imports, classes };
}

export function loadSources(root: string): SourceFile[] {
  return (readdirSync(root, { recursive: true }) as string[])
    .filter((file) => file.endsWith('.ts'))
    .map((file) => {
      const relative = file.split(path.sep).join('/');
      return parseSource(relative, readFileSync(path.join(root, file), 'utf8'));
    });
}
