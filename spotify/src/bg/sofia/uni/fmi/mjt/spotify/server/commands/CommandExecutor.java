package bg.sofia.uni.fmi.mjt.spotify.server.commands;

import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands.LoginCommand;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.UserAlreadyLoggedException;
import bg.sofia.uni.fmi.mjt.spotify.server.portsmanager.exceptions.NotLoggedUserException;

import java.nio.channels.SelectionKey;

public class CommandExecutor {

    public String execute(Command command) throws Exception {
        if (command == null) {
            return "Invalid Command";
        }

        return command.call();
    }

    public static void checkCommand(Command command, SelectionKey key)
            throws NotLoggedUserException, UserAlreadyLoggedException {
        if (command == null) {
            return;
        }
        boolean isLoggedIn = key.attachment() != null;

        if (!isLoggedIn) {
            if (command.getType() == CommandType.REGISTER || command.getType() == CommandType.LOGIN) {
                return;
            }
            throw new NotLoggedUserException("You are not logged");
        }

        if (command.getType() == CommandType.LOGIN || command.getType() == CommandType.REGISTER) {
            throw new UserAlreadyLoggedException("You are already logged");
        }
    }

    public static void checkLogin(Command cmd, SelectionKey key) {
        if (cmd == null || cmd.getType() != CommandType.LOGIN) {
            return;
        }

        LoginCommand loginCommand = (LoginCommand) cmd;
        if (!loginCommand.isCalled()) {
            return;
        }

        User user = loginCommand.getUser();
        key.attach(user);
    }
}
