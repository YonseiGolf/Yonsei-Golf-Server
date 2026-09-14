import 'reflect-metadata';
import { INestApplication, ValidationPipe } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';
import cookieParser from 'cookie-parser';
import helmet from 'helmet';
import { AppModule } from './app.module';
import { ApiExceptionFilter } from './common/http';
import { Settings } from './config/settings';

export function configureApp(app: INestApplication, settings: Settings): void {
  app.use(helmet());
  app.use(cookieParser());
  app.enableCors({
    origin: settings.origins,
    credentials: true,
    methods: ['GET', 'POST', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS'],
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
  const app = await NestFactory.create(AppModule.register(settings));
  configureApp(app, settings);
  return app;
}
