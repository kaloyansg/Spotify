package bg.sofia.uni.fmi.mjt.spotify.database;

import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.InvalidPasswordException;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingSongException;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingUserException;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.exceptions.InvalidPlaylistInfoException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions.InvalidEmailException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseImplTest {
    private static Path tempDir;
    private DatabaseImpl database;
    private static final String SONGS_DIR_NAME = "songs/";
    private static final String USERS_FILE_NAME = "users.txt";
    private static final String PLAYLISTS_FILE_NAME = "playlists.txt";
    private static String dbPath;

    @BeforeAll
    static void setUpGlobal() throws IOException {
        tempDir = Files.createTempDirectory("spotify-db-test-manual");
        dbPath = tempDir.toString() + File.separator;
    }

    @AfterAll
    static void tearDownGlobal() throws IOException {
        // 3. Delete the directory and everything inside it after all tests are done
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walkFileTree(tempDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        Path songsPath = tempDir.resolve(SONGS_DIR_NAME);
        if (!Files.exists(songsPath)) {
            Files.createDirectories(songsPath);
        }

        createWavFile(songsPath, "Title1-Artist1.wav");
        createWavFile(songsPath, "Title2-Artist2.wav");
        createWavFile(songsPath, "HitSong-PopStar.wav");

        Path usersPath = tempDir.resolve(USERS_FILE_NAME);
        Files.writeString(usersPath, "existing@mail.com,password123" + System.lineSeparator());

        Path playlistsPath = tempDir.resolve(PLAYLISTS_FILE_NAME);
        String playlistLine = "existing@mail.com,password123 MyHits:Title1-Artist1";
        Files.writeString(playlistsPath, playlistLine + System.lineSeparator());

        database = new DatabaseImpl(dbPath, SONGS_DIR_NAME, USERS_FILE_NAME, PLAYLISTS_FILE_NAME);
    }

    private void createWavFile(Path dir, String name) throws IOException {
        File wav = dir.resolve(name).toFile();
        byte[] validWavHeader = new byte[] {
                82, 73, 70, 70, 36, 0, 0, 0, 87, 65, 86, 69, 102, 109, 116, 32,
                16, 0, 0, 0, 1, 0, 1, 0, 68, -84, 0, 0, -120, 88, 1, 0,
                2, 0, 16, 0, 100, 97, 116, 97, 0, 0, 0, 0
        };
        try (FileOutputStream fos = new FileOutputStream(wav)) {
            fos.write(validWavHeader);
        }
    }

    @Test
    void testLoadInitialData() {
        Collection<Song> songs = database.searchSongs();
        assertEquals(3, songs.size(), "Should load 3 songs from disk");
        assertDoesNotThrow(() -> database.checkUser("existing@mail.com", "password123"), "Existing user should not throw");
    }

    @Test
    void testRegisterUser() throws UserAlreadyExistsException, InvalidEmailException, NonExistingUserException, InvalidPasswordException {
        database.registerUser("new@mail.com", "pass");
        assertDoesNotThrow(() -> database.checkUser("new@mail.com", "pass"), "Added user should not throw");
    }

    @Test
    void testRegisterDuplicate() {
        assertThrows(UserAlreadyExistsException.class,
                () -> database.registerUser("existing@mail.com", "password123"), "Duplicate user must throw");
    }

    @Test
    void testCheckUserWrongPassword() {
        assertThrows(InvalidPasswordException.class,
                () -> database.checkUser("existing@mail.com", "wrongpass"), "Wrong password must throw");
    }

    @Test
    void testCheckUserNonExistingUser() {
        assertThrows(NonExistingUserException.class,
                () -> database.checkUser("ghost@mail.com", "pass"), "Non existing user must throw");
    }

    @Test
    void testSearchSongs() {
        assertEquals(1, database.searchSongs("Title1").size(), "Invalid search result with one title filter");
        assertEquals(3, database.searchSongs().size(), "Invalid search result with no filters");
    }

    @Test
    void testGetTopSongs() {
        Song s1 = database.searchSongs("Title1").iterator().next();
        s1.stream();

        List<Song> top = (List<Song>) database.getTopSongs(1);
        assertEquals("Title1", top.get(0).getTitle(), "Could find the top 1 song");
    }

    @Test
    void testGetSongNotFound() {
        assertThrows(NonExistingSongException.class,
                () -> database.getSong("Ghost", "Busters"), "Song not found");
    }

    @Test
    void testCreatePlaylist() throws NonExistingUserException, InvalidPlaylistInfoException, NonExistingPlaylistException {
        database.createPlaylist("NewJams", "existing@mail.com");

        Playlist p = database.getPlaylist("NewJams", "existing@mail.com");
        assertNotNull(p, "Existing test playlist should not be null");
        assertTrue(p.getSongs().isEmpty(), "Test playlist should be empty");
    }

    @Test
    void testAddSongToPlaylist() throws Exception {
        database.addSong("MyHits", "existing@mail.com", "Title2", "Artist2");
        assertEquals(2, database.getPlaylistSongs("MyHits", "existing@mail.com").size(), "Test playlist must have 2 songs");
    }

    @Test
    void testAddSongPlaylistNotFound() {
        assertThrows(NonExistingPlaylistException.class,
                () -> database.addSong("FakeList", "existing@mail.com", "Title1", "Artist1"), "Adding song to nonexisting playlist must throw");
    }

    @Test
    void testCloseSavesData() throws Exception {
        database.registerUser("savedUser@mail.com", "secure");
        database.createPlaylist("SavedList", "savedUser@mail.com");

        database.close();

        DatabaseImpl reloadedDb = new DatabaseImpl(dbPath, SONGS_DIR_NAME, USERS_FILE_NAME, PLAYLISTS_FILE_NAME);

        assertDoesNotThrow(() -> reloadedDb.checkUser("savedUser@mail.com", "secure"), "After closing database, the new user are not added");
        assertDoesNotThrow(() -> reloadedDb.getPlaylist("SavedList", "savedUser@mail.com"), "After closing database, the new playlist are not added");
    }
}