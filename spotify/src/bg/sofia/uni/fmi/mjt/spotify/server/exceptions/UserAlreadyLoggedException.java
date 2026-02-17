package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class UserAlreadyLoggedException extends RuntimeException {
    public UserAlreadyLoggedException(String message) {
        super(message);
    }

    public UserAlreadyLoggedException(String message, Throwable cause) {
        super(message, cause);
    }
}
