package bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions;

public class InvalidEmailException extends Exception {
    public InvalidEmailException(String message) {
        super(message);
    }

    public InvalidEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
