package yonseigolf.server.user.exception;

public class DuplicatedLoginException extends RuntimeException {

    public DuplicatedLoginException(String message) {
        super(message);
    }
}
