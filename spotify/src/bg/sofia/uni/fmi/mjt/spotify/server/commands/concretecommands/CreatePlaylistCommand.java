package bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands;

import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.server.Server;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.Command;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.CommandType;

public class CreatePlaylistCommand extends Command {
    public static final String COMMAND = "create-playlist";
    private final String playlistName;
    private final User user;

    public CreatePlaylistCommand(String playlistName, User user, Server server) {
        super(server, CommandType.CREATE_PLAYLIST);
        this.playlistName = playlistName;
        this.user = user;
    }

    @Override
    public String call() throws Exception {
        server.getDatabase().createPlaylist(playlistName, user.getEmail());
        return "Playlist titled: " + playlistName + " by " + user.getEmail() + " was created";
    }

    public static CreatePlaylistCommand of(String line, User user, Server server) {
        if (line.isBlank()) {
            return null;
        }
        return new CreatePlaylistCommand(line, user, server);
    }
}
