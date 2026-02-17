package bg.sofia.uni.fmi.mjt.spotify.database.exceptions;

public class NonExistingUserException extends Exception {
    public NonExistingUserException(String message) {
        super(message);
    }

    public NonExistingUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
