package bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions;

public class InvalidUserException extends Exception {
    public InvalidUserException(String message) {
        super(message);
    }

    public InvalidUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
