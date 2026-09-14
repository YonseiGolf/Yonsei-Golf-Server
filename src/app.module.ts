import {
  Controller,
  DynamicModule,
  Get,
  Module,
  ServiceUnavailableException,
} from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { DataSource } from 'typeorm';
import { ApplicationsModule } from './applications/applications.module';
import { BoardsModule } from './boards/boards.module';
import { Settings, SettingsModule } from './config/settings';
import { databaseOptions } from './database/database';
import { UsersModule } from './users/users.module';

@Controller()
class HealthController {
  constructor(private readonly db: DataSource) {}
  @Get('healthcheck')
  async healthcheck() {
    try {
      await this.db.query('SELECT 1');
    } catch {
      throw new ServiceUnavailableException('Database unavailable');
    }
    return 'ok';
  }
}

@Module({})
export class AppModule {
  static register(settings: Settings): DynamicModule {
    return {
      module: AppModule,
      imports: [
        SettingsModule.register(settings),
        TypeOrmModule.forRoot({
          ...databaseOptions(settings),
          retryAttempts: 1,
        }),
        UsersModule,
        BoardsModule,
        ApplicationsModule,
      ],
      controllers: [HealthController],
    };
  }
}
