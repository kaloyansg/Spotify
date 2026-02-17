package bg.sofia.uni.fmi.mjt.spotify.client.player;

import bg.sofia.uni.fmi.mjt.spotify.client.Client;

import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

public class Player implements Runnable {
    private final int port;
    private final String serverHost;
    private final SourceDataLine dataLine;
    private final Client client;

    public Player(int port, String serverHost, SourceDataLine dataLine, Client client) {
        this.port = port;
        this.serverHost = serverHost;
        this.dataLine = dataLine;
        this.client = client;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(serverHost, port);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream())) {

            byte[] toWrite = new byte[dataLine.getFormat().getFrameSize()];
            dataLine.start();
            try {
                do {
                    int readBytes = bufferedInputStream.read(toWrite, 0, toWrite.length);
                    if (readBytes == -1) {
                        break;
                    }
                    dataLine.write(toWrite, 0, readBytes);
                } while (dataLine.isRunning());

            } catch (IllegalArgumentException e) {
                //bufferedInputStream reached the end, it doesn't matter
            }
        } catch (IOException e) {
            System.out.println("Player stopped: Server connection lost.");
        } finally {
            if (dataLine.isOpen()) {
                dataLine.drain();
                dataLine.close();
            }
            client.resetSourceDataLine();
        }
    }
}
