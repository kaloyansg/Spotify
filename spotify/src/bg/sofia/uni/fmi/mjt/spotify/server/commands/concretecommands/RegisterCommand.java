package bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands;

import bg.sofia.uni.fmi.mjt.spotify.server.Server;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.Command;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.CommandType;

public class RegisterCommand extends Command {
    public static final String COMMAND = "register";
    private final String email;
    private final String password;

    public RegisterCommand(String email, String password, Server server) {
        super(server, CommandType.REGISTER);
        this.email = email;
        this.password = password;
    }

    @Override
    public String call() throws Exception {
        server.getDatabase().registerUser(email, password);
        return "Successfully registered";
    }

    public static RegisterCommand of(String line, Server server) {
        String[] split = line.split(" ");

        if (split.length != 2) {
            return null;
        }

        return new RegisterCommand(split[0], split[1], server);
    }
}
