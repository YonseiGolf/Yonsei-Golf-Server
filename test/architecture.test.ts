import assert from 'node:assert/strict';
import path from 'node:path';
import { describe, it } from 'node:test';
import {
  adapterDependencies,
  coreDependencies,
  layerDependencies,
  noCycles,
  portContracts,
  portImplementations,
  rolePlacement,
  rules,
  sliceBoundaries,
} from './architecture/rules';
import { loadSources, parseSource } from './architecture/source-graph';

const sources = loadSources(path.resolve('src'));
const fixture = (files: Record<string, string>) =>
  Object.entries(files).map(([file, text]) => parseSource(file, text));

describe('hexagonal architecture of src', () => {
  it('reads the layers it checks', () => {
    for (const layer of ['domain/', 'application/', 'adapter/', 'support/'])
      assert.ok(
        sources.some((file) => file.path.startsWith(layer)),
        `no files under ${layer}`,
      );
  });
  for (const [name, rule] of Object.entries(rules))
    it(`follows ${name}`, () => {
      assert.deepEqual(rule(sources), []);
    });
});

// Each rule must still reject the violation it exists for.
describe('architecture rules reject violations', () => {
  it('rejects inner layers importing outer ones', () => {
    const files = fixture({
      'domain/user/user.ts': "import { x } from '../../application/user/x';",
      'application/user/x.ts': "import { y } from '../../adapter/config/y';",
      'domain/board/board.ts': "import { A } from '../../support/stereotype';",
      'adapter/webapi/z.ts': "import { M } from '../../app.module';",
    });
    assert.equal(layerDependencies(files).length, 4);
  });
  it('rejects infrastructure and HTTP concepts in the core', () => {
    const files = fixture({
      'domain/user/user.ts': "import { Injectable } from '@nestjs/common';",
      'application/user/a.service.ts':
        "import { NotFoundException } from '@nestjs/common';",
      'application/user/b.service.ts': "import nodemailer from 'nodemailer';",
      'application/user/c.service.ts': 'const r = await fetch(url);',
    });
    assert.equal(coreDependencies(files).length, 4);
    assert.deepEqual(
      coreDependencies(
        fixture({
          'application/user/d.service.ts':
            "import { Logger } from '@nestjs/common';\nimport { In } from 'typeorm';",
        }),
      ),
      [],
    );
  });
  it('rejects reaching past another slice’s provided ports', () => {
    const files = fixture({
      'application/apply/a.service.ts':
        "import { S } from '../recruitment/recruitment.service';\nimport { R } from '../recruitment/required/r';\nimport { F } from '../recruitment/provided/f';",
      'application/shared/page.ts': "import { U } from '../user/provided/u';",
    });
    assert.equal(sliceBoundaries(files).length, 3);
  });
  it('keeps ports free of implementations', () => {
    const files = fixture({
      'application/user/provided/p.ts':
        "import { S } from '../user.service';\nimport { Injectable } from '@nestjs/common';",
      'application/user/required/r.ts': "import { F } from '../u.service';",
    });
    assert.equal(portContracts(files).length, 3);
  });
  it('rejects cycles between slices and between aggregates', () => {
    const files = fixture({
      'application/a/x.ts': "import { B } from '../b/provided/b';",
      'application/b/y.ts': "import { A } from '../a/provided/a';",
      'domain/a/a.ts': "import { B } from '../b/b';",
      'domain/b/b.ts': "import { A } from '../a/a';",
    });
    assert.equal(noCycles(files).length, 2);
  });
  it('rejects roles outside their layer', () => {
    const files = fixture({
      'application/user/provided/x.ts': '@ApplicationService()\nclass X {}',
      'application/user/y.ts': '@Controller()\nclass Y {}',
      'domain/user/z.ts': '@Injectable()\nclass Z {}',
      'adapter/webapi/m.ts': '@Module({})\nclass M {}',
      'application/user/r.ts': 'class R extends Repository<User> {}',
      'application/user/a.ts': '@Adapter()\nclass A {}',
      // A role named in a comment is not a use.
      'domain/user/doc.ts': '// like @Controller() in adapters\nconst x = 1;',
    });
    assert.equal(rolePlacement(files).length, 6);
  });
  it('keeps adapters on ports', () => {
    const files = fixture({
      'adapter/webapi/user/c.ts':
        "import { S } from '../../../application/user/user.service';\nimport { K } from '../../integration/kakao/k';\nimport { R } from '../../../application/user/required/r';",
      'adapter/integration/kakao/k.ts':
        "import { F } from '../../../application/user/provided/f';",
    });
    assert.equal(adapterDependencies(files).length, 4);
  });
  it('keeps port implementations in their place', () => {
    const files = fixture({
      'application/board/x.service.ts':
        "import { UserFinder } from '../user/provided/user-finder';\nclass X implements UserFinder {}",
      'adapter/webapi/y.ts':
        "import { UserFinder } from '../../application/user/provided/user-finder';\nclass Y implements UserFinder {}",
      'application/user/z.service.ts':
        "import { TokenIssuer } from './required/token-issuer';\nclass Z implements TokenIssuer {}",
    });
    assert.equal(portImplementations(files).length, 3);
  });
});
