package bg.sofia.uni.fmi.mjt.spotify.database.units.song.exceptions;

public class InvalidSongException extends Exception {
    public InvalidSongException(String message) {
        super(message);
    }

    public InvalidSongException(String message, Throwable cause) {
        super(message, cause);
    }
}
