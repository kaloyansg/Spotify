package bg.sofia.uni.fmi.mjt.spotify.client;

import bg.sofia.uni.fmi.mjt.spotify.client.exceptions.InvalidOperationException;
import bg.sofia.uni.fmi.mjt.spotify.client.player.Player;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Scanner;

public class Client implements Runnable {
    private static final String CLIENT_ERR_LOG = "project/spotify/resources/client/clientErrLog.txt";
    private static final int SERVER_PORT = 6666;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;

    private static final int RESPONSE_STATUS_IDX = 0;
    private static final int ENCODING_IDX = 1;
    private static final int SAMPLE_RATE_IDX = 2;
    private static final int SAMPLE_SIZE_IN_BITS_IDX = 3;
    private static final int CHANNELS_IDX = 4;
    private static final int FRAME_SIZE_IDX = 5;
    private static final int FRAME_RATE_IDX = 6;
    private static final int BIG_ENDIAN_IDX = 7;
    private static final int PORT_IDX = 8;

    private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private SourceDataLine sourceDataLine = null;
    private String userName = null;

    @Override
    public void run() {
        SocketChannel socketChannel = null;
        try (Scanner scanner = new Scanner(System.in);
             PrintWriter errWriter = new PrintWriter(Files.newBufferedWriter(Path.of(CLIENT_ERR_LOG),
                     StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {

            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to the server.");

            while (processUserInput(scanner, socketChannel, errWriter)) {
            }
        } catch (IOException e) {
            System.out.println("Error occurred while processing server requests: " + e.getMessage());
            System.out.println("Connection failed!");
        } finally {
            shutdown(socketChannel);
        }
    }

    private boolean processUserInput(Scanner scanner, SocketChannel socketChannel, PrintWriter logger) {
        System.out.print(System.lineSeparator() + userName + ">>");
        String input = scanner.nextLine().strip();

        if ("stop".equalsIgnoreCase(input)) {
            try {
                stopSong();
                System.out.println("Song stopped.");
            } catch (InvalidOperationException e) {
                handleErr(logger, e);
            }
            return true;
        } else {
            try {
                manageServerCommunication(input, socketChannel);
            } catch (IOException e) {
                System.out.println("Server connection lost.");
                return false;
            } catch (Exception e) {
                handleErr(logger, e);
            }
        }

        return !"disconnect".equalsIgnoreCase(input);
    }

    private void manageServerCommunication(String input, SocketChannel socketChannel)
            throws IOException, LineUnavailableException {
        System.out.println("Sending request <" + input + "> to the server");
        sendRequest(input, socketChannel);
        String reply = receiveResponse(socketChannel);

        if (reply.startsWith("ok ")) {
            playSong(reply);
        } else if (reply.startsWith("Successfully logged")) {
            userName = input.split(" ")[1];
            System.out.println("Logged in as " + userName);
        } else {
            System.out.println(reply);
        }
    }

    private void shutdown(SocketChannel socketChannel) {
        try {
            stopSong();
            System.out.println("Song stopped.");
        } catch (InvalidOperationException ignored) {
            // Ignore; if no song is playing during shutdown
        }
        if (socketChannel != null && socketChannel.isOpen()) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
        System.out.println("Shut down.");
    }

    private void handleErr(PrintWriter errRecordWriter, Exception e) {
        System.out.println(e.getMessage());
        errRecordWriter.println(e.getMessage() + ": " +
                Arrays.toString(e.getStackTrace()) + System.lineSeparator());
        errRecordWriter.flush();
    }

    private void sendRequest(String request, SocketChannel socketChannel) throws IOException {
        buffer.clear();
        buffer.put(request.getBytes());
        buffer.flip();
        socketChannel.write(buffer);
    }

    private String receiveResponse(SocketChannel socketChannel) throws IOException {
        buffer.clear();
        int readBytes = socketChannel.read(buffer);
        if (readBytes < 0) {
            throw new IOException("Server disconnected");
        }
        buffer.flip();
        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);

        return new String(byteArray, StandardCharsets.UTF_8);
    }

    private void playSong(String response) throws LineUnavailableException {
        if (sourceDataLine != null) {
            System.out.println("Song is already playing!");
            return;
        }
        String[] splitReply = response.split("\\s+");

        if (!splitReply[RESPONSE_STATUS_IDX].equalsIgnoreCase("ok")) {
            return;
        }

        AudioFormat audioFormat = new AudioFormat(new AudioFormat.Encoding(splitReply[ENCODING_IDX]),
                Float.parseFloat(splitReply[SAMPLE_RATE_IDX]), Integer.parseInt(splitReply[SAMPLE_SIZE_IN_BITS_IDX]),
                Integer.parseInt(splitReply[CHANNELS_IDX]), Integer.parseInt(splitReply[FRAME_SIZE_IDX]),
                Float.parseFloat(splitReply[FRAME_RATE_IDX]), Boolean.parseBoolean(splitReply[BIG_ENDIAN_IDX]));

        int streamingPort = Integer.parseInt(splitReply[PORT_IDX]);

        Line.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
        sourceDataLine.open();

        System.out.println("now playing...");
        new Thread(new Player(streamingPort, SERVER_HOST, sourceDataLine, this), "Player Thread").start();
    }

    private void stopSong() throws InvalidOperationException {
        if (sourceDataLine == null) {
            throw new InvalidOperationException("Nothing to stop (no song is being played)");
        }
        sourceDataLine.stop();
    }

    public void resetSourceDataLine() {
        sourceDataLine = null;
    }

    public static void main(String[] args) {
        bg.sofia.uni.fmi.mjt.spotify.client.Client client = new Client();
        new Thread(client, "Client Thread").start();
    }
}
