package bg.sofia.uni.fmi.mjt.spotify.server.commands;

import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.server.Server;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands.AddSongToCommand;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands.CreatePlaylistCommand;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands.DisconnectCommand;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands.LoginCommand;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands.PlayCommand;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands.PlaylistCommand;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands.RegisterCommand;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands.SearchCommand;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands.TopSongsCommand;

public class CommandFactory {

    public static Command create(String input, User user, Server server) {
        if (input == null || input.isBlank()) {
            return null;
        }
        if (input.equalsIgnoreCase(DisconnectCommand.COMMAND)) {
            return new DisconnectCommand(user, server);
        }

        String[] commandFields = input.split("\\s+", 2);
        if (commandFields.length != 2 && !commandFields[0].equalsIgnoreCase(SearchCommand.COMMAND)) {
            return null;
        }

        String command = commandFields[0].toLowerCase().strip();
        String commandContent = "";
        if (commandFields.length == 2) {
            commandContent = commandFields[1];
        }
        return switch (command) {
            case PlayCommand.COMMAND -> PlayCommand.of(commandContent, user, server);
            case SearchCommand.COMMAND -> SearchCommand.of(commandContent, server);
            case PlaylistCommand.COMMAND -> PlaylistCommand.of(commandContent, user, server);
            case TopSongsCommand.COMMAND -> TopSongsCommand.of(commandContent, server);
            case AddSongToCommand.COMMAND -> AddSongToCommand.of(commandContent, user, server);
            case CreatePlaylistCommand.COMMAND -> CreatePlaylistCommand.of(commandContent, user, server);
            case LoginCommand.COMMAND -> LoginCommand.of(commandContent, server);
            case RegisterCommand.COMMAND -> RegisterCommand.of(commandContent, server);
            default -> null;
        };
    }
}
