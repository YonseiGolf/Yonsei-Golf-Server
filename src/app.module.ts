import { DynamicModule, Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ApplyModule } from './adapter/config/apply.module';
import { databaseOptions } from './adapter/config/database';
import { HealthModule } from './adapter/config/health.module';
import { MailTemplateModule } from './adapter/config/mail-template.module';
import { RecruitmentModule } from './adapter/config/recruitment.module';
import { Settings, SettingsModule } from './adapter/config/settings';
import { UserModule } from './adapter/config/user.module';

// The composition root: each slice module binds its ports to implementations.
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
        UserModule,
        MailTemplateModule,
        RecruitmentModule,
        ApplyModule,
        HealthModule,
      ],
    };
  }
}
