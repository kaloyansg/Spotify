package bg.sofia.uni.fmi.mjt.spotify.server.commands;

import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.server.Server;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class CommandFactoryTest {

    @Mock
    private Server server;

    @Mock
    private User user;

    @Test
    void testCreateNull() {
        assertNull(CommandFactory.create(null, user, server), "Null command should not have been created");
    }

    @Test
    void testCreateBlank() {
        assertNull(CommandFactory.create("   ", user, server), "Blank command should not have been created");
    }

    @Test
    void testCreateUnknownCommand() {
        assertNull(CommandFactory.create("unknown command arguments", user, server),  "Unknown command should not have been created");
    }

    @Test
    void testCreateIncompleteCommand() {
        assertNull(CommandFactory.create(RegisterCommand.COMMAND, user, server), "Incomplete command should not have been created");
    }

    @Test
    void testCreateDisconnectCommand() {
        Command cmd = CommandFactory.create(DisconnectCommand.COMMAND, user, server);
        assertInstanceOf(DisconnectCommand.class, cmd, "Disconnect command should have been created");
    }

    @Test
    void testCreateRegisterCommand() {
        String input = RegisterCommand.COMMAND + " test@mail.com pass123";
        Command cmd = CommandFactory.create(input, user, server);
        assertInstanceOf(RegisterCommand.class, cmd, "Register command should have been created");
    }

    @Test
    void testCreateLoginCommand() {
        String input = LoginCommand.COMMAND + " test@mail.com pass123";
        Command cmd = CommandFactory.create(input, user, server);
        assertInstanceOf(LoginCommand.class, cmd,  "Login command should have been created");
    }

    @Test
    void testCreatePlayCommand() {
        String input = PlayCommand.COMMAND + " MySong";
        Command cmd = CommandFactory.create(input, user, server);
        assertInstanceOf(PlayCommand.class, cmd, "Play command should have been created");
    }

    @Test
    void testCreateSearchCommand() {
        String input = SearchCommand.COMMAND + " metallica";
        Command cmd = CommandFactory.create(input, user, server);
        assertInstanceOf(SearchCommand.class, cmd,  "Search command with keywords should have been created");
    }

    @Test
    void testCreateEmptySearchCommand() {
        Command cmd = CommandFactory.create(SearchCommand.COMMAND, user, server);
        assertInstanceOf(SearchCommand.class, cmd, "Search command with no keywords should have been created");
    }

    @Test
    void testCreateTopSongsCommand() {
        String input = TopSongsCommand.COMMAND + " 10";
        Command cmd = CommandFactory.create(input, user, server);
        assertInstanceOf(TopSongsCommand.class, cmd,   "Top songs command should have been created");
    }

    @Test
    void testCreateCreatePlaylistCommand() {
        String input = CreatePlaylistCommand.COMMAND + " MyJam";
        Command cmd = CommandFactory.create(input, user, server);
        assertInstanceOf(CreatePlaylistCommand.class, cmd, "Create playlist command should have been created");
    }

    @Test
    void testCreatePlaylistCommand() {
        String input = PlaylistCommand.COMMAND + " MyJam";
        Command cmd = CommandFactory.create(input, user, server);
        assertInstanceOf(PlaylistCommand.class, cmd, "Playlist command should have been created");
    }

    @Test
    void testCreateCaseInsensitiveCommandName() {
        String commandUpper = LoginCommand.COMMAND.toUpperCase();
        String input = commandUpper + " test@mail.com pass";
        Command cmd = CommandFactory.create(input, user, server);
        assertInstanceOf(LoginCommand.class, cmd, "Login command should have been created from Uppercase command string");
    }

    @Test
    void testCreateAddSongToCommand() {
        String input = AddSongToCommand.COMMAND + " MyPlaylist : Title-Artist";

        Command cmd = CommandFactory.create(input, user, server);

        assertInstanceOf(AddSongToCommand.class, cmd, "Add song to command should have been created");
    }

    @Test
    void testCreateTrimsWhitespace() {
        String validInput = PlayCommand.COMMAND + "    MySong";
        Command cmd = CommandFactory.create(validInput, user, server);
        assertInstanceOf(PlayCommand.class, cmd, "Command creator should trim white spaces and create the command");
    }
}