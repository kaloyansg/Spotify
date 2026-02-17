package bg.sofia.uni.fmi.mjt.spotify.database.units.playlist;

import bg.sofia.uni.fmi.mjt.spotify.database.Database;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingSongException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.exceptions.InvalidPlaylistInfoException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.exceptions.SongAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions.InvalidEmailException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions.InvalidUserException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultPlaylist implements Playlist {
    private final Set<Song> songs;
    private final String title;
    private final User owner;

    private static final int PLAYLIST_INFO_CNT = 2;

    public DefaultPlaylist(String title, User owner) throws InvalidPlaylistInfoException {
        if (title == null) {
            throw new NullPointerException("title is null");
        }
        if (owner == null) {
            throw new NullPointerException("owner is null");
        }
        if (title.contains(":")) {
            throw new InvalidPlaylistInfoException("title must not contain ':'");
        }
        this.title = title;
        this.owner = owner;
        songs = new HashSet<>();
    }

    public static DefaultPlaylist of(String line, Database database) throws InvalidPlaylistInfoException {
        if (line == null || line.isEmpty()) {
            throw new NullPointerException("line is null");
        }
        String[] fields = line.split(" ", PLAYLIST_INFO_CNT);
        if (fields.length != PLAYLIST_INFO_CNT) {
            throw new InvalidPlaylistInfoException("Playlist line is invalid: " + line);
        }
        User owner;
        try {
            owner = User.of(fields[0]);
        } catch (InvalidEmailException | InvalidUserException e) {
            throw new InvalidPlaylistInfoException(e.getMessage());
        }

        int colonIndex = fields[1].indexOf(':');
        if (colonIndex == -1) {
            throw new InvalidPlaylistInfoException("Missing ':' separator between title and songs");
        }
        String playlistTitle = fields[1].substring(0, colonIndex);
        String songsPart = fields[1].substring(colonIndex + 1);

        DefaultPlaylist result = new DefaultPlaylist(playlistTitle, owner);
        fillPlaylist(songsPart, database, result);
        return result;
    }

    private static void fillPlaylist(String songsPart, Database database, Playlist playlist) {
        if (!songsPart.isEmpty()) {
            String[] songTokens = songsPart.split(";");
            for (String songStr : songTokens) {
                if (!songStr.contains("-")) {
                    continue;
                }
                String[] parts = songStr.split("-");
                if (parts.length < 2) {
                    continue;
                }

                try {
                    Song dbSong = database.getSong(parts[0].strip(), parts[1].strip());
                    playlist.addSong(dbSong);
                } catch (NonExistingSongException ex) {
                    System.err.println("Warning: Song not found " + songStr);
                } catch (SongAlreadyExistsException ex) {
                    //just skip reps
                }
            }
        }
    }

    public void addSong(Song song) throws SongAlreadyExistsException {
        if (song == null) {
            throw new NullPointerException("song is null");
        }
        synchronized (songs) {
            if (songs.contains(song)) {
                throw new SongAlreadyExistsException("Song already exists: " + song);
            }
            songs.add(song);
        }
    }

    public Collection<Song> getSongs() {
        synchronized (songs) {
            return new ArrayList<>(songs);
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public User getOwner() {
        return owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultPlaylist playlist = (DefaultPlaylist) o;
        return title.equals(playlist.title) && owner.equals(playlist.owner);
    }

    @Override
    public int hashCode() {
        String lowerTitle = title.toLowerCase();
        int result = lowerTitle.hashCode();
        result = 31 * result + owner.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String songList;
        synchronized (songs) {
            songList = songs.stream()
                    .map(Song::toString)
                    .collect(Collectors.joining(";"));
        }
        return owner.toString() + " " + title + ":" + songList;
    }
}
