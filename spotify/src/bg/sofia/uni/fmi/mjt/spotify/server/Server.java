package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.database.Database;
import bg.sofia.uni.fmi.mjt.spotify.database.DatabaseImpl;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingUserException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.Command;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.CommandExecutor;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.CommandFactory;
import bg.sofia.uni.fmi.mjt.spotify.server.portsmanager.PortsManager;
import bg.sofia.uni.fmi.mjt.spotify.server.portsmanager.exceptions.NotLoggedUserException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;

public class Server implements Runnable {
    private static final String SERVER_ERR_LOG = "project/spotify/resources/server/serverErrLog.txt";
    private static final int STREAMING_PORT = 7777;
    private static final int PORT = 6666;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;

    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private Selector selector;

    private final Database database;
    private final PortsManager portsManager;
    private final CommandExecutor commandExecutor;
    private boolean terminated = false;

    public Server(Database database, CommandExecutor commandExecutor) {
        this.database = database;
        this.commandExecutor = commandExecutor;
        this.portsManager = new PortsManager(STREAMING_PORT, database);
    }

    @Override
    public void run() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); database;
             PrintWriter errWriter = createErrWriter()) {
            System.out.println("Server started on port " + PORT);
            selector = Selector.open();
            setUpServerSocketChannel(serverSocketChannel, selector);

            while (!terminated) {
                if (selector.select() == 0) continue;
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        handleReadable(key, errWriter);
                    } else if (key.isAcceptable()) {
                        handleAcceptable(key);
                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private PrintWriter createErrWriter() throws IOException {
        return new PrintWriter(Files.newBufferedWriter(Path.of(SERVER_ERR_LOG),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND));
    }

    private void handleAcceptable(SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();
        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    private void handleReadable(SelectionKey key, PrintWriter errWriter) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        try {
            String clientInput = getClientInput(clientChannel);
            if (clientInput == null) {
                portsManager.logOut((User) key.attachment());
                return;
            }
            logRequest((User) key.attachment(), clientInput);
            String output = executeCommand(clientInput, key, errWriter);
            writeClientOutput(clientChannel, output);

        } catch (IOException e) {
            handleConnectionReset(e, key, clientChannel);
        } catch (NotLoggedUserException | NonExistingUserException e) {
            // ignore - user has disconnected
        }
    }

    private void logRequest(User user, String input) {
        if (user == null) {
            System.out.println("An unknown user requested <" + input + ">");
        } else {
            System.out.println("User: " + user + " requested <" + input + ">");
        }
    }

    private String executeCommand(String clientInput, SelectionKey key, PrintWriter errWriter) {
        try {
            Command command = CommandFactory.create(clientInput, (User) key.attachment(), this);
            CommandExecutor.checkCommand(command, key);
            String output = commandExecutor.execute(command);
            CommandExecutor.checkLogin(command, key);
            System.out.println(output);
            return output;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            errWriter.println(e.getMessage() + Arrays.toString(e.getStackTrace()));
            errWriter.flush();
            return e.getMessage() + System.lineSeparator();
        }
    }

    private void handleConnectionReset(IOException e, SelectionKey key, SocketChannel channel) {
        if (e.getMessage().contains("Connection reset")) {
            try {
                portsManager.logOut((User) key.attachment());
                channel.close();
                key.cancel();
            } catch (Exception ex) {
                // ignore
            }
        } else {
            System.out.println("Error processing client request: " + e.getMessage());
        }
    }

    private void setUpServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(SERVER_HOST, PORT));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientChannel.write(buffer);
    }

    public void terminate() {
        terminated = true;
        if (selector != null && selector.isOpen()) {
            selector.wakeup();
        }
    }

    public Database getDatabase() {
        return database;
    }

    public PortsManager getPortsManager() {
        return portsManager;
    }

    public static void main(String[] args) {
        Server server = new Server(new DatabaseImpl("project/spotify/resources/database/",
                "songs/", "users.txt", "playlists.txt"), new CommandExecutor());
        Thread serverThread = new Thread(server, "Server Thread");
        serverThread.start();

        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            while (true) {
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("terminate")) {
                    System.out.println("Terminate server...");
                    server.terminate();
                    break;
                }
            }
        }

        try {
            serverThread.join();
            System.out.println("Server terminated complete.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
