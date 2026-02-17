package bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands;

import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.database.units.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.server.Server;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.Command;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.CommandType;

public class AddSongToCommand extends Command {
    public static final String COMMAND = "add-song-to";
    private final String fullSongName;
    private final String playlistName;
    private final User user;

    public AddSongToCommand(String fullSongName, String playlistName, User user, Server server) {
        super(server, CommandType.ADD_SONG_TO);
        this.fullSongName = fullSongName;
        this.playlistName = playlistName;
        this.user = user;
    }

    @Override
    public String call() throws Exception {
        Song song = server.getDatabase().getSong(fullSongName);
        try {
            Playlist playlist = server.getDatabase().getPlaylist(playlistName, user.getEmail());
            playlist.addSong(song);
        } catch (NonExistingPlaylistException e) {
            throw new NonExistingPlaylistException("You don't have a playlist named: " + playlistName);
        }
        return "Song added successfully";
    }

    public static AddSongToCommand of(String line, User user, Server server) {
        String[] split = line.split("\\s*:\\s*", 2);

        if (split.length != 2) {
            return null;
        }

        return new AddSongToCommand(split[1], split[0], user, server);
    }
}
