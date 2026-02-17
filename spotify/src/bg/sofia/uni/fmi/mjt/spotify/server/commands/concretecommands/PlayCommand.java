package bg.sofia.uni.fmi.mjt.spotify.server.commands.concretecommands;

import bg.sofia.uni.fmi.mjt.spotify.database.units.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.Command;
import bg.sofia.uni.fmi.mjt.spotify.server.commands.CommandType;
import bg.sofia.uni.fmi.mjt.spotify.server.Server;
import bg.sofia.uni.fmi.mjt.spotify.server.streamer.Streamer;

public class PlayCommand extends Command {
    public static final String COMMAND = "play";
    private final String songStr;
    private final User user;

    public PlayCommand(String songStr, User user, Server server) {
        super(server, CommandType.PLAY);
        this.songStr = songStr;
        this.user = user;
    }

    @Override
    public String call() throws Exception {
        Song song = server.getDatabase().getSong(songStr);

        long port = server.getPortsManager().getUserPort(user);
        server.getPortsManager().isPortStreaming(port);

        Streamer streamer = new Streamer((int) port, song, server);

        Thread thread = new Thread(streamer, "Song Streamer for User: " + user);
        thread.setDaemon(true);
        thread.start();

        return "ok " + song.getAudioFormat() + " " + port;
    }

    public static PlayCommand of(String line, User user, Server server) {
        if (line.isBlank()) {
            return null;
        }
        return new PlayCommand(line, user, server);
    }

    public String getSongStr() {
        return songStr;
    }

    public User getUser() {
        return user;
    }
}
