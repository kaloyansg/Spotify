package bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands;

import bg.sofia.uni.fmi.mjt.spotify.database.units.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.server.Server;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.Command;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.CommandType;

import java.util.ArrayList;
import java.util.List;

public class SearchCommand extends Command {
    public static final String COMMAND = "search";
    private final String[] filters;

    public SearchCommand(String[] filters, Server server) {
        super(server, CommandType.SEARCH);
        this.filters = filters;
    }

    @Override
    public String call() throws Exception {
        List<Song> songs;
        songs = new ArrayList<>(server.getDatabase().searchSongs(filters));

        if (songs.isEmpty()) {
            return "No Songs Found";
        }

        return "Found Songs:" + System.lineSeparator() + Command.songsAsStr(songs);
    }

    public static SearchCommand of(String line, Server spotifyServer) {
        String[] split = line.split(" ");
        return new SearchCommand(split, spotifyServer);
    }
}
