package bg.sofia.uni.fmi.mjt.spotify.server.exceptions;

public class PortOccupiedException extends Exception {
    public PortOccupiedException(String message) {
        super(message);
    }

    public PortOccupiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
