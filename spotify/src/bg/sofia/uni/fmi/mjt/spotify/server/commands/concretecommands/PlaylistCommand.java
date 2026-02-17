package bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands;

import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.database.units.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.server.Server;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.Command;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.CommandType;

import java.util.List;

public class PlaylistCommand extends Command {
    public static final String COMMAND = "show-playlist";
    private final String playlistTitle;
    private final User user;

    public PlaylistCommand(String playlistTitle, User user, Server server) {
        super(server, CommandType.SHOW_PLAYLIST);
        this.playlistTitle = playlistTitle;
        this.user = user;
    }

    @Override
    public String call() throws Exception {
        Playlist playlist = server.getDatabase().getPlaylist(playlistTitle, user.getEmail());
        List<Song> songs = List.copyOf(playlist.getSongs());

        return "Playlist " + playlist.getTitle() + " by " + playlist.getOwner().getEmail() + ":" +
                System.lineSeparator() + Command.songsAsStr(songs);
    }

    public static PlaylistCommand of(String line, User user, Server spotifyServer) {
        if (line.isBlank()) {
            return null;
        }

        try {
            return new PlaylistCommand(line, user, spotifyServer);
        } catch (Exception e) {
            //could not be thrown
        }
        return null;
    }
}
