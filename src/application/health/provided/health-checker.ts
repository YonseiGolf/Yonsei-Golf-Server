export abstract class HealthChecker {
  abstract isDatabaseAvailable(): Promise<boolean>;
}
