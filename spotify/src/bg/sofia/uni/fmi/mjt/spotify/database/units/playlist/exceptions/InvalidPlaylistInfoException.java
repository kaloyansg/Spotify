package bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.exceptions;

public class InvalidPlaylistInfoException extends Exception {
    public InvalidPlaylistInfoException(String message) {
        super(message);
    }

    public InvalidPlaylistInfoException(String message, Throwable cause) {
        super(message, cause);
    }
}
