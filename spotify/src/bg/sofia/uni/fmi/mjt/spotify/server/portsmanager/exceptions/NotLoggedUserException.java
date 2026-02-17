package bg.sofia.uni.fmi.mjt.spotify.server.portsmanager.exceptions;

public class NotLoggedUserException extends RuntimeException {
    public NotLoggedUserException(String message) {
        super(message);
    }

    public NotLoggedUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
