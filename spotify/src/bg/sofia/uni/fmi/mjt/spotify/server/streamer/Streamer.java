package bg.sofia.uni.fmi.mjt.spotify.server.streamer;

import bg.sofia.uni.fmi.mjt.spotify.database.units.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.server.Server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Streamer implements Runnable {
    private final int port;
    private final Song song;
    private final Server server;

    public Streamer(int port, Song song, Server server) {
        this.port = port;
        this.song = song;
        this.server = server;
    }

    @Override
    public void run() {
        server.getPortsManager().lockPort(port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            try (Socket socket = serverSocket.accept();
                 BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
                 BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(
                         Path.of(server.getDatabase().getSongSrc() + song.getFileName())))) {

                song.stream();

                byte[] output = new byte[song.getFrameSize()];
                while (inputStream.available() > 0) {
                    int readBytes = inputStream.read(output, 0, output.length);
                    outputStream.write(output, 0, readBytes);
                }
                outputStream.flush();
            } catch (SocketException ignored) {
                //user stopped the song
            }
        } catch (IOException e) {
            System.out.println("Could not stream the song: " + song.getFileName() + ", " + e.getMessage());
        }

        song.endStream();
        server.getPortsManager().freePort(port);

        System.out.println("Song has ended");
    }
}
