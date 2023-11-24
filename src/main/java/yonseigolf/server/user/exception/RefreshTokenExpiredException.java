package yonseigolf.server.user.exception;

public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException(String message) {

        super(message);
    }
}
