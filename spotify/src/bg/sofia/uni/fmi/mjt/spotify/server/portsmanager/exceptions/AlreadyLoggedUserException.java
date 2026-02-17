package bg.sofia.uni.fmi.mjt.spotify.server.portsmanager.exceptions;

public class AlreadyLoggedUserException extends Exception {
    public AlreadyLoggedUserException(String message) {
        super(message);
    }

    public AlreadyLoggedUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
