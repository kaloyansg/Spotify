package bg.sofia.uni.fmi.mjt.spotify.server.commands;

import bg.sofia.uni.fmi.mjt.spotify.database.units.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.server.Server;

import java.util.List;
import java.util.concurrent.Callable;

public abstract class Command implements Callable<String> {

    protected Server server;
    private final CommandType type;

    protected Command(Server server, CommandType type) {
        this.server = server;
        this.type = type;
    }

    public CommandType getType() {
        return type;
    }

    protected static String songsStreamsAsStr(List<Song> songs) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < songs.size(); ++i) {
            result.append(i + 1).append(". ").append(songs.get(i)).append(" -> Streams: ")
                    .append(songs.get(i).getStreamsCnt()).append(System.lineSeparator());
        }

        return result.toString();
    }

    protected static String songsAsStr(List<Song> songs) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < songs.size(); ++i) {
            result.append(i + 1).append(". ").append(songs.get(i)).append(System.lineSeparator());
        }

        return result.toString();
    }
}
