import { DataSource } from 'typeorm';
import { ApplicationService } from '../../support/stereotype';
import { HealthChecker } from './provided/health-checker';

@ApplicationService()
export class HealthService implements HealthChecker {
  constructor(private readonly db: DataSource) {}

  async isDatabaseAvailable(): Promise<boolean> {
    try {
      await this.db.query('SELECT 1');
      return true;
    } catch {
      return false;
    }
  }
}
