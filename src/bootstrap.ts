import 'reflect-metadata';
import { INestApplication, ValidationPipe } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';
import cookieParser from 'cookie-parser';
import helmet from 'helmet';
import { createLogger } from './adapter/config/logging';
import { Settings } from './adapter/config/settings';
import { ApiExceptionFilter } from './adapter/webapi/api-exception.filter';
import { requestLogger } from './adapter/webapi/request-logger';
import { AppModule } from './app.module';

export function configureApp(app: INestApplication, settings: Settings): void {
  app.use(requestLogger());
  app.use(helmet());
  app.use(cookieParser());
  app.enableCors({
    origin: settings.origins,
    credentials: true,
    methods: ['GET', 'POST', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS'],
    // Lets the client show the ID so a report can be matched to the server log.
    exposedHeaders: ['X-Request-Id'],
  });
  app.useGlobalPipes(
    new ValidationPipe({
      transform: true,
      whitelist: true,
      forbidNonWhitelisted: true,
    }),
  );
  app.useGlobalFilters(new ApiExceptionFilter());
  app.enableShutdownHooks();
}

export async function createApp(settings: Settings): Promise<INestApplication> {
  const app = await NestFactory.create(AppModule.register(settings), {
    logger: createLogger(settings.logFormat),
  });
  configureApp(app, settings);
  return app;
}
