package bg.sofia.uni.fmi.mjt.spotify.database.exceptions;

public class NonExistingSongException extends Exception {
    public NonExistingSongException(String message) {
        super(message);
    }

    public NonExistingSongException(String message, Throwable cause) {
        super(message, cause);
    }
}
