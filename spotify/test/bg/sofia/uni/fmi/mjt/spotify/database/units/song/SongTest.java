package bg.sofia.uni.fmi.mjt.spotify.database.units.song;

import bg.sofia.uni.fmi.mjt.spotify.database.units.song.exceptions.InvalidSongException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SongTest {

    private static Path testDir;
    private static final String TEST_SONG = "Title-Artist.wav";

    @BeforeAll
    static void setUp() throws IOException {
        testDir = Files.createTempDirectory("spotify-test-dir");
        File wavFile = testDir.resolve(TEST_SONG).toFile();

        // 44-byte wav header, so AudioSystem recognizes it
        byte[] validWavHeader = new byte[]{
                82, 73, 70, 70, 36, 0, 0, 0, 87, 65, 86, 69, 102, 109, 116, 32,
                16, 0, 0, 0, 1, 0, 1, 0, 68, -84, 0, 0, -120, 88, 1, 0,
                2, 0, 16, 0, 100, 97, 116, 97, 0, 0, 0, 0
        };

        try (FileOutputStream output = new FileOutputStream(wavFile)) {
            output.write(validWavHeader);
        }
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (testDir != null) {
            Files.deleteIfExists(testDir.resolve(TEST_SONG));
            Files.deleteIfExists(testDir);
        }
    }

    @Test
    void testOfValid() throws InvalidSongException {
        String folderPath = testDir.toString() + File.separator;
        Song song = Song.of(folderPath, TEST_SONG);

        assertNotNull(song);
        assertEquals("Title", song.getTitle(), "Invalid title");
        assertEquals("Artist", song.getArtist(), "Invalid artist");
        assertEquals(TEST_SONG, song.getFileName(),  "Invalid file name");
        assertNotNull(song.getAudioFormat(),  "Invalid audio format");
    }

    @Test
    void testOfInvalidSong() {
        String folderPath = testDir.toString() + File.separator;
        String nonExistentFile = "Fake-TestFile.wav";

        assertThrows(InvalidSongException.class,
                () -> Song.of(folderPath, nonExistentFile),
                "Must throw if the file cannot be found");
    }

    @Test
    void testOfInvalidSongExceptionNull() {
        String folderPath = testDir.toString() + File.separator;
        assertThrows(InvalidSongException.class, () -> Song.of(folderPath, null),
                "Must throw if the file name is null");
    }

    @Test
    void testOfInvalidSongExceptionEmpty() {
        String folderPath = testDir.toString() + File.separator;
        assertThrows(InvalidSongException.class, () -> Song.of(folderPath, ""),
                "Must throw if the file name is empty");
    }

    @Test
    void testOfInvalidFileName() {
        String folderPath = testDir.toString() + File.separator;
        assertThrows(InvalidSongException.class,
                () -> Song.of(folderPath, "SongTitle.wav"),
                "Must throw if the file name is invalid");
    }

    @Test
    void testOfSwitchedArgs() {
        String folderPath = testDir.toString() + File.separator;
        assertThrows(InvalidSongException.class,
                () -> Song.of(folderPath, "Artist-Title;.wav"), "Artist and title switched");
    }

    @Test
    void testOfInvalidFormat() {
        String folderPath = testDir.toString() + File.separator;
        assertThrows(InvalidSongException.class,
                () -> Song.of(folderPath, "Artist-Title.mp3"), "Invalid format");
    }

    @Test
    void testStream() throws InvalidSongException {
        String folderPath = testDir.toString() + File.separator;
        Song song = Song.of(folderPath, TEST_SONG);

        assertEquals(0, song.getStreamsCnt(), "No streamed song must have 0 streams");

        song.stream();
        assertEquals(1, song.getStreamsCnt(), "Invalid streams count");

        song.stream();
        assertEquals(2, song.getStreamsCnt(), "Invalid streams count");
    }

    @Test
    void testEndStream() throws InvalidSongException {
        String folderPath = testDir.toString() + File.separator;
        Song song = Song.of(folderPath, TEST_SONG);

        song.stream();
        song.stream();
        song.endStream();

        assertEquals(1, song.getStreamsCnt(),  "Invalid streams count");
    }

    @Test
    void testEndStreamBelowZero() throws InvalidSongException {
        String folderPath = testDir.toString() + File.separator;
        Song song = Song.of(folderPath, TEST_SONG);

        song.endStream();
        assertEquals(0, song.getStreamsCnt(),  "Invalid streams count");
    }

    @Test
    void testEqualsAndHashCode() throws InvalidSongException {
        String folderPath = testDir.toString() + File.separator;

        Song song1 = Song.of(folderPath, TEST_SONG);
        Song song2 = Song.of(folderPath, TEST_SONG);

        assertEquals(song1, song2, "Songs from one file must be equal");
        assertEquals(song1.hashCode(), song2.hashCode(), "Songs from one file must have the same hash code");
    }
}