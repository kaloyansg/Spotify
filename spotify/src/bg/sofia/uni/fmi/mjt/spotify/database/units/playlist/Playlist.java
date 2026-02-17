package bg.sofia.uni.fmi.mjt.spotify.database.units.playlist;

import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.exceptions.SongAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;

import java.util.Collection;

public interface Playlist {
    void addSong(Song song) throws SongAlreadyExistsException;

    Collection<Song> getSongs();

    String getTitle();

    User getOwner();
}
