package bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands;

import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.server.Server;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.Command;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.CommandType;

public class DisconnectCommand extends Command {
    public static final String COMMAND = "disconnect";
    private final Server server;
    private final User user;

    public DisconnectCommand(User user, Server server) {
        super(server, CommandType.DISCONNECT);
        this.server = server;
        this.user = user;
    }

    @Override
    public String call() throws Exception {
        server.getPortsManager().logOut(user);
        return "You've been disconnected from the server!";
    }
}
