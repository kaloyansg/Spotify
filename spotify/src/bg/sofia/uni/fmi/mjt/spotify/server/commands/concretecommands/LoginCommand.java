package bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands;

import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions.InvalidEmailException;
import bg.sofia.uni.fmi.mjt.spotify.server.Server;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.Command;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.CommandType;

public class LoginCommand extends Command {
    public static final String COMMAND = "login";
    private final User user;
    private Boolean called;

    public LoginCommand(String email, String pass, Server server) throws InvalidEmailException {
        super(server, CommandType.LOGIN);
        this.user = new User(email, pass);
        this.called = false;
    }

    @Override
    public String call() throws Exception {
        server.getPortsManager().logInUser(user);
        called = true;
        return "Successfully logged in";
    }

    public static LoginCommand of(String line, Server server) {
        String[] split = line.split(" ", 2);

        if (split.length != 2) {
            return null;
        }

        try {
            return new LoginCommand(split[0], split[1], server);
        } catch (InvalidEmailException e) {
            return null;
        }
    }

    public boolean isCalled() {
        return called;
    }

    public User getUser() {
        return user;
    }
}
