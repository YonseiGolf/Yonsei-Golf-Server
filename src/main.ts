import { existsSync } from 'node:fs';
import { loadSettings } from './adapter/config/settings';
import { createApp } from './bootstrap';

async function main(): Promise<void> {
  if (existsSync('.env')) process.loadEnvFile('.env');
  const settings = loadSettings(process.env);
  const app = await createApp(settings);
  await app.listen(settings.port, '0.0.0.0');
}

void main().catch((error: unknown) => {
  console.error(error instanceof Error ? error : 'Startup failed');
  process.exitCode = 1;
});
