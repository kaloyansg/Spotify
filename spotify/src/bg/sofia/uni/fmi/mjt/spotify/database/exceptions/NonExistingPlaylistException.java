package bg.sofia.uni.fmi.mjt.spotify.database.exceptions;

public class NonExistingPlaylistException extends Exception {
    public NonExistingPlaylistException(String message) {
        super(message);
    }

    public NonExistingPlaylistException(String message, Throwable cause) {
        super(message, cause);
    }
}
