package bg.sofia.uni.fmi.mjt.spotify.database.units.playlist;

import bg.sofia.uni.fmi.mjt.spotify.database.Database;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingSongException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.exceptions.InvalidPlaylistInfoException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.exceptions.SongAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions.InvalidEmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPlaylistTest {

    @Mock
    private Database database;

    @Mock
    private Song song1;

    @Mock
    private Song song2;

    private User user;

    @BeforeEach
    void setUp() throws InvalidEmailException {
        user = new User("test@mail.com", "pass123");
    }


    @Test
    void testConstructorEmptyPlaylist() throws InvalidPlaylistInfoException {
        DefaultPlaylist playlist = new DefaultPlaylist("MyPlaylist", user);

        assertEquals("MyPlaylist", playlist.getTitle());
        assertEquals(user, playlist.getOwner());
        assertTrue(playlist.getSongs().isEmpty());
    }

    @Test
    void testConstructorNullTitle() {
        assertThrows(NullPointerException.class,
                () -> new DefaultPlaylist(null, user), "Title must not be null");
    }

    @Test
    void testConstructorNullOwner() {
        assertThrows(NullPointerException.class,
                () -> new DefaultPlaylist("Title", null),  "Owner must not be null");
    }

    @Test
    void testConstructorInvalidFormat() {
        assertThrows(InvalidPlaylistInfoException.class,
                () -> new DefaultPlaylist("My:Jams", user),
                "Must throw if title contains reserved char ':'");
    }

    @Test
    void testOfValid() throws InvalidPlaylistInfoException, NonExistingSongException {
        String line = "test@mail.com,pass123 GymBeats:Eye of the Tiger-Survivor;Stronger-Kanye";

        when(database.getSong("Eye of the Tiger", "Survivor")).thenReturn(song1);
        when(database.getSong("Stronger", "Kanye")).thenReturn(song2);

        DefaultPlaylist playlist = DefaultPlaylist.of(line, database);

        assertEquals("GymBeats", playlist.getTitle());
        assertEquals(user.getEmail(), playlist.getOwner().getEmail());
        assertEquals(2, playlist.getSongs().size());
        assertTrue(playlist.getSongs().contains(song1));
        assertTrue(playlist.getSongs().contains(song2));
    }

    @Test
    void testOfValidNoSongs() throws InvalidPlaylistInfoException {
        String line = "test@mail.com,pass123 EmptyList:";

        DefaultPlaylist playlist = DefaultPlaylist.of(line, database);

        assertEquals("EmptyList", playlist.getTitle());
        assertTrue(playlist.getSongs().isEmpty());
    }

    @Test
    void testOfNonExistingSongs() throws InvalidPlaylistInfoException, NonExistingSongException {
        String line = "test@mail.com,pass123 MyList:RealSong-Artist;FakeSong-Artist";

        when(database.getSong("RealSong", "Artist")).thenReturn(song1);
        when(database.getSong("FakeSong", "Artist")).thenThrow(new NonExistingSongException("Not found"));

        DefaultPlaylist playlist = DefaultPlaylist.of(line, database);

        assertEquals(1, playlist.getSongs().size());
        assertTrue(playlist.getSongs().contains(song1));
    }

    @Test
    void testOfSkipInvalidSongs() throws InvalidPlaylistInfoException, NonExistingSongException {
        String line = "test@mail.com,pass123 MyList:BadFormatSong";

        DefaultPlaylist playlist = DefaultPlaylist.of(line, database);

        verify(database, never()).getSong("BadFormatSong", "");

        assertEquals(0, playlist.getSongs().size(), "Should have skipped bad format song");
    }

    @Test
    void testOfNullLine() {
        assertThrows(NullPointerException.class, () -> DefaultPlaylist.of(null, database));
    }

    @Test
    void testOfInvalidFormat() {
        String line = "invalidline";
        assertThrows(InvalidPlaylistInfoException.class, () -> DefaultPlaylist.of(line, database));
    }

    @Test
    void testOfNoColon() {
        String line = "test@mail.com,pass123 TitleNoColon";
        assertThrows(InvalidPlaylistInfoException.class,
                () -> DefaultPlaylist.of(line, database),
                "Must throw if ':' is missing");
    }

    @Test
    void testOfInvalidUser() {
        String line = "notanemail,pass Title:Song-Artist";
        assertThrows(InvalidPlaylistInfoException.class,
                () -> DefaultPlaylist.of(line, database),
                "Must wrap InvalidUser or InvalidEmail exceptions");
    }

    @Test
    void testAddSongValid() throws InvalidPlaylistInfoException, SongAlreadyExistsException {
        DefaultPlaylist playlist = new DefaultPlaylist("Title", user);

        playlist.addSong(song1);

        assertEquals(1, playlist.getSongs().size());
        assertTrue(playlist.getSongs().contains(song1));
    }

    @Test
    void testAddSongNull() throws InvalidPlaylistInfoException {
        DefaultPlaylist playlist = new DefaultPlaylist("Title", user);
        assertThrows(NullPointerException.class, () -> playlist.addSong(null));
    }

    @Test
    void testAddSongDuplicate() throws InvalidPlaylistInfoException, SongAlreadyExistsException {
        DefaultPlaylist playlist = new DefaultPlaylist("Title", user);
        playlist.addSong(song1);

        assertThrows(SongAlreadyExistsException.class,
                () -> playlist.addSong(song1),
                "Must throw when adding the same song instance twice");
    }

    @Test
    void testGetSongs() throws InvalidPlaylistInfoException, SongAlreadyExistsException {
        DefaultPlaylist playlist = new DefaultPlaylist("Title", user);
        playlist.addSong(song1);

        Collection<Song> songs = playlist.getSongs();
        songs.clear();

        assertFalse(playlist.getSongs().isEmpty(), "Internal set should not be affected by external modification");
    }

    @Test
    void testEqualsAndHashCode() throws InvalidPlaylistInfoException {
        DefaultPlaylist p1 = new DefaultPlaylist("Title", user);
        DefaultPlaylist p2 = new DefaultPlaylist("Title", user);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void testToString() throws InvalidPlaylistInfoException, SongAlreadyExistsException {
        DefaultPlaylist playlist = new DefaultPlaylist("MyPlaylist", user);

        when(song1.toString()).thenReturn("Song1-Artist1");
        when(song2.toString()).thenReturn("Song2-Artist2");

        playlist.addSong(song1);
        playlist.addSong(song2);

        String result = playlist.toString();

        assertTrue(result.startsWith("test@mail.com,pass123 MyPlaylist:"));
        assertTrue(result.contains("Song1-Artist1"));
        assertTrue(result.contains("Song2-Artist2"));
    }
}