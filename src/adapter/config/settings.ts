import { DynamicModule, Global, Module } from '@nestjs/common';
import { LogFormat } from './logging';

export class Settings {
  constructor(
    readonly database: {
      host: string;
      port: number;
      name: string;
      username: string;
      password: string;
      ssl: boolean;
    },
    readonly jwtSecret: Buffer,
    readonly port: number,
    readonly profile: 'home' | 'aws',
    readonly origins: string[],
    readonly kakao: {
      clientId: string;
      clientSecret: string;
      tokenUrl: string;
      userUrl: string;
      callbackUrl?: string;
    },
    readonly mail: {
      host: string;
      port: number;
      username: string;
      password: string;
      from: string;
      secure: boolean;
      requireTLS: boolean;
    },
    readonly storage: {
      provider: 'minio' | 's3';
      endpoint?: string;
      region: string;
      bucket: string;
      publicUrl: string;
      accessKeyId?: string;
      secretAccessKey?: string;
    },
    readonly cookieSecure = true,
    readonly logFormat: LogFormat = 'text',
  ) {}
}

export function loadSettings(env: NodeJS.ProcessEnv): Settings {
  const required = (key: string): string => {
    const value = env[key];
    if (!value) throw new Error(`Missing environment variable: ${key}`);
    return value;
  };
  const integer = (value: string, name: string): number => {
    const number = Number(value);
    if (!Number.isSafeInteger(number) || number < 1 || number > 65535)
      throw new Error(`Invalid ${name}`);
    return number;
  };
  const url = new URL(required('DATABASE_URL').replace(/^jdbc:/, ''));
  if (url.protocol !== 'mysql:' || !/^\/[A-Za-z0-9_]+$/.test(url.pathname))
    throw new Error('Invalid MySQL DATABASE_URL');
  const encodedSecret = required('JWT_SECRET_KEY');
  if (!/^[A-Za-z0-9+/]+={0,2}$/.test(encodedSecret))
    throw new Error('JWT_SECRET_KEY must be Base64');
  const secret = Buffer.from(encodedSecret, 'base64');
  if (secret.length < 32)
    throw new Error('JWT_SECRET_KEY must contain at least 32 decoded bytes');
  const logFormat =
    env.LOG_FORMAT || (env.NODE_ENV === 'production' ? 'json' : 'text');
  if (logFormat !== 'json' && logFormat !== 'text')
    throw new Error('LOG_FORMAT must be json or text');
  const profile = env.APP_PROFILE ?? env.SPRING_PROFILES_ACTIVE ?? 'home';
  if (profile !== 'home' && profile !== 'aws')
    throw new Error('APP_PROFILE must be home or aws');
  const storageProvider =
    env.STORAGE_PROVIDER || (profile === 'home' ? 'minio' : 's3');
  if (storageProvider !== 'minio' && storageProvider !== 's3')
    throw new Error('STORAGE_PROVIDER must be minio or s3');
  if (
    storageProvider === 'minio' &&
    (!(env.S3_ENDPOINT || env.AWS_S3_ENDPOINT) ||
      !(env.AWS_ACCESS_KEY_ID || env.AWS_ACCESS_KEY) ||
      !env.AWS_SECRET_ACCESS_KEY)
  )
    throw new Error(
      'MinIO storage requires S3_ENDPOINT, AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY',
    );
  return new Settings(
    {
      host: url.hostname,
      port: integer(url.port || '3306', 'database port'),
      name: url.pathname.slice(1),
      username: env.DATABASE_USERNAME || decodeURIComponent(url.username),
      password: env.DATABASE_PASSWORD ?? decodeURIComponent(url.password),
      ssl: env.DATABASE_SSL === 'true',
    },
    secret,
    integer(env.PORT || '8080', 'PORT'),
    profile,
    (
      env.CORS_ORIGINS ||
      'http://localhost:3000,https://www.yonseigolf.site,https://yonseigolf.site,https://www.birdiehyun.store,https://birdiehyun.store,https://www.yonsei-golf.kr,https://yonsei-golf.kr,https://test-yg-clinet.vercel.app'
    )
      .split(',')
      .map((origin) => origin.trim()),
    {
      clientId: required('KAKAO_CLIENT_ID'),
      clientSecret: required('KAKAO_CLIENT_SECRET'),
      tokenUrl:
        env.KAKAO_TOKEN_URL ||
        env.KAKAO_REDIRECT_URI ||
        'https://kauth.kakao.com/oauth/token',
      userUrl: env.KAKAO_LOGIN_URI || 'https://kapi.kakao.com/v2/user/me',
      callbackUrl: env.KAKAO_CALLBACK_URL,
    },
    {
      host: env.SMTP_HOST || env.SPRING_MAIL_HOST || 'localhost',
      port: integer(
        env.SMTP_PORT || env.SPRING_MAIL_PORT || '1025',
        'SMTP_PORT',
      ),
      username: env.SMTP_USERNAME || env.SPRING_MAIL_USERNAME || '',
      password: env.SMTP_PASSWORD || env.SPRING_MAIL_PASSWORD || '',
      from:
        env.SMTP_FROM ||
        env.SMTP_USERNAME ||
        env.SPRING_MAIL_USERNAME ||
        'noreply@yonsei-golf.kr',
      secure: env.SMTP_SECURE === 'true',
      requireTLS:
        (env.SMTP_REQUIRE_TLS ||
          env.SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE) === 'true',
    },
    {
      provider: storageProvider,
      endpoint: env.S3_ENDPOINT || env.AWS_S3_ENDPOINT,
      region: env.AWS_REGION || 'ap-northeast-2',
      bucket: required('AWS_S3_BUCKET'),
      publicUrl: required('AWS_S3_PUBLIC_URL'),
      accessKeyId: env.AWS_ACCESS_KEY_ID || env.AWS_ACCESS_KEY,
      secretAccessKey: env.AWS_SECRET_ACCESS_KEY,
    },
    env.COOKIE_SECURE !== 'false',
    logFormat,
  );
}

@Global()
@Module({})
export class SettingsModule {
  static register(settings: Settings): DynamicModule {
    return {
      module: SettingsModule,
      providers: [{ provide: Settings, useValue: settings }],
      exports: [Settings],
    };
  }
}
