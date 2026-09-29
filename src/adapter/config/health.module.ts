import { Module } from '@nestjs/common';
import { HealthService } from '../../application/health/health.service';
import { HealthChecker } from '../../application/health/provided/health-checker';
import { HealthController } from '../webapi/health/health.controller';

@Module({
  controllers: [HealthController],
  providers: [{ provide: HealthChecker, useClass: HealthService }],
})
export class HealthModule {}
