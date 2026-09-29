/** `oauth` is the short-lived token issued before sign-up; `user` and `admin` need a member token. */
export type AccessLevel = 'oauth' | 'user' | 'admin';

export abstract class AccessVerifier {
  /** Checks the token's signature, expiry and purpose and returns its principal ID. */
  abstract authenticate(token: string, level: AccessLevel): string;
  /** Checks the member's current state in the DB, which wins over older token claims. */
  abstract authorize(principalId: string, level: AccessLevel): Promise<void>;
}
