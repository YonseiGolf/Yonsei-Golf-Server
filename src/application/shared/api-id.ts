// Keep numeric API IDs when safe; never silently round a MySQL BIGINT.
export function apiId(value: string | number): number | string {
  const number = Number(value);
  return Number.isSafeInteger(number) ? number : String(value);
}
