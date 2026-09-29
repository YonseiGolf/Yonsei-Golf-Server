export const success = <T>(message: string, data: T | null = null) => ({
  status: 'success',
  code: 200,
  message,
  data,
});
