package bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.exceptions;

public class SongAlreadyExistsException extends Exception {
    public SongAlreadyExistsException(String message) {
        super(message);
    }

    public SongAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
