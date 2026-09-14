import assert from 'node:assert/strict';
import { it } from 'node:test';
import { MySqlContainer, StartedMySqlContainer } from '@testcontainers/mysql';
import mysql from 'mysql2/promise';
import {
  GenericContainer,
  Network,
  StartedNetwork,
  StartedTestContainer,
  Wait,
} from 'testcontainers';

it('runs the production images and persists an HTTP request in real MySQL', {
  timeout: 240000,
}, async () => {
  let network: StartedNetwork | undefined;
  let database: StartedMySqlContainer | undefined;
  let server: StartedTestContainer | undefined;
  let connection: mysql.Connection | undefined;
  try {
    network = await new Network().start();
    database = await new MySqlContainer('mysql:8.4')
      .withDatabase('yg_container_test')
      .withUsername('yg_test')
      .withUserPassword('yg_test_password')
      .withNetwork(network)
      .withNetworkAliases('mysql')
      .start();
    const migration = await new GenericContainer(
      'yonsei-golf-server:flyway-local',
    )
      .withPullPolicy({ shouldPull: () => false })
      .withNetwork(network)
      .withEnvironment({
        FLYWAY_URL:
          'jdbc:mysql://mysql:3306/yg_container_test?allowPublicKeyRetrieval=true&useSSL=false',
        FLYWAY_USER: database.getUsername(),
        FLYWAY_PASSWORD: database.getUserPassword(),
      })
      .withCommand(['migrate'])
      .withWaitStrategy(Wait.forOneShotStartup())
      .start();
    await migration.stop();
    server = await new GenericContainer('yonsei-golf-server:nestjs-local')
      .withPullPolicy({ shouldPull: () => false })
      .withNetwork(network)
      .withExposedPorts(8080)
      .withEnvironment({
        APP_PROFILE: 'aws',
        DATABASE_URL: 'mysql://mysql:3306/yg_container_test',
        DATABASE_USERNAME: database.getUsername(),
        DATABASE_PASSWORD: database.getUserPassword(),
        JWT_SECRET_KEY: Buffer.alloc(32, 't').toString('base64'),
        KAKAO_CLIENT_ID: 'test',
        KAKAO_CLIENT_SECRET: 'test',
        AWS_S3_BUCKET: 'test-bucket',
        AWS_S3_PUBLIC_URL: 'https://images.example.test',
      })
      .withWaitStrategy(Wait.forHttp('/healthcheck', 8080))
      .start();
    const base = `http://${server.getHost()}:${server.getMappedPort(8080)}`;
    const response = await fetch(`${base}/application/emailAlarm`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: 'container@example.test', semester: 40 }),
    });
    assert.equal(response.status, 200, await response.text());
    connection = await mysql.createConnection({
      host: database.getHost(),
      port: database.getPort(),
      user: database.getUsername(),
      password: database.getUserPassword(),
      database: 'yg_container_test',
    });
    const [rows] = await connection.execute<mysql.RowDataPacket[]>(
      'SELECT email, semester FROM email_alarm',
    );
    assert.equal(rows.length, 1);
    assert.equal(rows[0]?.email, 'container@example.test');
    assert.equal(Number(rows[0]?.semester), 40);
    const user = await server.exec(['id', '-u']);
    assert.notEqual(
      user.output.trim(),
      '0',
      'Production server must run as a non-root user',
    );
  } finally {
    await connection?.end();
    await server?.stop();
    await database?.stop();
    await network?.stop();
  }
});
