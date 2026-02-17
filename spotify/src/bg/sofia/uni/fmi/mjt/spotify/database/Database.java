package bg.sofia.uni.fmi.mjt.spotify.database;

import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.InvalidPasswordException;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingSongException;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingUserException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.exceptions.SongAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.exceptions.InvalidPlaylistInfoException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions.InvalidEmailException;

import java.io.Closeable;
import java.util.Collection;

public interface Database extends Closeable {
    void registerUser(String email, String password) throws UserAlreadyExistsException, InvalidEmailException;

    void checkUser(String email, String password) throws NonExistingUserException, InvalidPasswordException;

    void checkUser(User user) throws NonExistingUserException, InvalidPasswordException;

    Collection<Song> searchSongs(String... filters); //if no filters return all

    Collection<Song> getTopSongs(int limit);

    void createPlaylist(String title, String ownerEmail) throws NonExistingUserException, InvalidPlaylistInfoException;

    void addSong(String playListTitle, String owner, String title, String artist)
            throws SongAlreadyExistsException, NonExistingPlaylistException, NonExistingSongException;

    Collection<Song> getPlaylistSongs(String title, String owner) throws NonExistingPlaylistException;

    Playlist getPlaylist(String title, String owner) throws NonExistingPlaylistException;

    Song getSong(String title, String artist) throws NonExistingSongException;

    Song getSong(String string) throws NonExistingSongException;

    String getSongSrc();
}
