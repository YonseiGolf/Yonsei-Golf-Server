import { Controller, Injectable } from '@nestjs/common';

// Role markers. They register Nest providers like @Injectable()/@Controller()
// and name the role so the architecture test can check where each one lives.

/** A use case in an application slice root, usually implementing provided ports. */
export const ApplicationService = (): ClassDecorator => Injectable();

/** An adapter implementing a required port (JWT, Kakao, SMTP, S3). */
export const Adapter = (): ClassDecorator => Injectable();

/** A web API inbound adapter: translates HTTP into provided port calls. */
export const WebApiAdapter = (): ClassDecorator => Controller();
