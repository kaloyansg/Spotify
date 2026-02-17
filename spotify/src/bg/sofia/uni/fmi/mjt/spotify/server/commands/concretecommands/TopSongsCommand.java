package bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands;

import bg.sofia.uni.fmi.mjt.spotify.database.units.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.server.Server;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.Command;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.CommandType;

import java.util.ArrayList;
import java.util.List;

public class TopSongsCommand extends Command {
    public static final String COMMAND = "top";
    private final int limit;

    public TopSongsCommand(int limit, Server server) {
        super(server, CommandType.TOP);
        this.limit = limit;
    }

    @Override
    public String call() throws Exception {
        List<Song> topSongs;
        topSongs = new ArrayList<>(server.getDatabase().getTopSongs(limit));

        if (topSongs.isEmpty()) {
            return "No Songs Found";
        }

        return "Top Songs: " + System.lineSeparator() + Command.songsStreamsAsStr(topSongs);
    }

    public static TopSongsCommand of(String line, Server spotifyServer) {
        try {
            int limit = Integer.parseInt(line);
            return new TopSongsCommand(limit, spotifyServer);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
