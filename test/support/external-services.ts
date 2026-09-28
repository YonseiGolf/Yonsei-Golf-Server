import { createServer, Server } from 'node:http';
import { AddressInfo } from 'node:net';
import { SMTPServer } from 'smtp-server';

export class ExternalServices {
  readonly messages: string[] = [];
  readonly kakaoRequests: URLSearchParams[] = [];
  rejectMail = false;
  private http: Server | undefined;
  private smtp: SMTPServer | undefined;
  kakaoUrl = '';
  smtpPort = 0;

  async start(): Promise<void> {
    this.http = createServer(async (request, response) => {
      response.setHeader('Content-Type', 'application/json');
      if (request.url === '/oauth/token' && request.method === 'POST') {
        const chunks: Buffer[] = [];
        for await (const chunk of request) chunks.push(Buffer.from(chunk));
        const parameters = new URLSearchParams(
          Buffer.concat(chunks).toString(),
        );
        this.kakaoRequests.push(parameters);
        if (
          parameters.get('code') === 'invalid' ||
          parameters.get('refresh_token') === 'invalid'
        ) {
          response
            .writeHead(400)
            .end(JSON.stringify({ error: 'invalid_grant' }));
          return;
        }
        response.end(
          JSON.stringify({
            access_token: 'kakao-access',
            refresh_token: 'kakao-refresh',
          }),
        );
      } else if (
        request.url === '/v2/user/me' &&
        request.headers.authorization === 'Bearer kakao-access'
      ) {
        response.end(JSON.stringify({ id: 45678 }));
      } else {
        response.writeHead(401).end('{}');
      }
    });
    await new Promise<void>((resolve) =>
      this.http?.listen(0, '127.0.0.1', resolve),
    );
    this.kakaoUrl = `http://127.0.0.1:${(this.http.address() as AddressInfo).port}`;
    this.smtp = new SMTPServer({
      disabledCommands: ['AUTH', 'STARTTLS'],
      onData: (stream, _session, callback) => {
        const chunks: Buffer[] = [];
        stream.on('data', (chunk: Buffer) => chunks.push(chunk));
        stream.on('end', () => {
          if (this.rejectMail) {
            callback(new Error('Test SMTP rejection'));
            return;
          }
          this.messages.push(Buffer.concat(chunks).toString());
          callback();
        });
      },
    });
    await new Promise<void>((resolve) =>
      this.smtp?.listen(0, '127.0.0.1', resolve),
    );
    this.smtpPort = (this.smtp.server.address() as AddressInfo).port;
  }
  async stop(): Promise<void> {
    if (this.http)
      await new Promise<void>((resolve, reject) =>
        this.http?.close((error) => (error ? reject(error) : resolve())),
      );
    if (this.smtp)
      await new Promise<void>((resolve) => this.smtp?.close(resolve));
  }
}
