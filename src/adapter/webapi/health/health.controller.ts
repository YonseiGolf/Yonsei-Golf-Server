import { Get, ServiceUnavailableException } from '@nestjs/common';
import { HealthChecker } from '../../../application/health/provided/health-checker';
import { WebApiAdapter } from '../../../support/stereotype';

@WebApiAdapter()
export class HealthController {
  constructor(private readonly health: HealthChecker) {}
  @Get('healthcheck')
  async healthcheck() {
    if (!(await this.health.isDatabaseAvailable()))
      throw new ServiceUnavailableException('Database unavailable');
    return 'ok';
  }
}
