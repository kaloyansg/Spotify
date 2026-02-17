package bg.sofia.uni.fmi.mjt.spotify.database.units.song;

import bg.sofia.uni.fmi.mjt.spotify.database.units.song.exceptions.InvalidSongException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class Song {
    private final String title;
    private final String artist;
    private final String fileName;
    private final AtomicInteger streamsCnt;
    private final String audioFormat;
    private final int frameSize;

    public void stream() {
        streamsCnt.incrementAndGet();
    }

    public void endStream() {
        streamsCnt.updateAndGet(current -> current > 0 ? current - 1 : 0);
    }

    public int getStreamsCnt() {
        return streamsCnt.get();
    }

    public static Song of(String folderName, String fileName) throws InvalidSongException {
        if (fileName == null || fileName.isEmpty() ||
                fileName.chars().filter(ch -> ch == '-').count() != 1 || fileName.contains(";")) {
            throw new InvalidSongException("Invalid file name: " + fileName +
                            "(must not be empty and must not contain more than one '-' and must not contain ';')");
        }
        int idxFormat = fileName.lastIndexOf('.');
        if (idxFormat == -1 || !fileName.substring(idxFormat + 1).toLowerCase(Locale.ROOT).equals("wav")) {
            throw new InvalidSongException("Invalid file name: " + fileName + "(must be wav)");
        }

        String[] songInfo = fileName.substring(0, idxFormat).split("-");
        if (songInfo.length != 2) {
            throw new InvalidSongException(fileName + " is not a valid song file");
        }

        String audioFormatStr;
        int frameSize;
        try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(folderName + fileName))) {
            AudioFormat audioFormat = inputStream.getFormat();
            audioFormatStr = audioFormat.getEncoding().toString() + " " + audioFormat.getSampleRate() + " " +
                    audioFormat.getSampleSizeInBits() + " " + audioFormat.getChannels() + " " +
                    audioFormat.getFrameSize() + " " + audioFormat.getFrameRate() + " " + audioFormat.isBigEndian();
            frameSize = audioFormat.getFrameSize();
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new InvalidSongException("A Song with the Name: " + songInfo[0] + " does not exist");
        }

        return new Song(songInfo[0].strip(), songInfo[1].strip(), fileName, audioFormatStr, frameSize);
    }

    private Song(String title, String artist, String fileName, String audioFormat, int frameSize)
            throws InvalidSongException {
        if (title == null || artist == null || fileName == null || audioFormat == null) {
            throw new InvalidSongException("arguments cannot be null");
        }
        this.title = title;
        this.artist = artist;
        this.fileName = fileName;
        this.streamsCnt = new AtomicInteger(0);
        this.audioFormat = audioFormat;
        this.frameSize = frameSize;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public  String getFileName() {
        return fileName;
    }

    public String getAudioFormat() {
        return audioFormat;
    }

    public int getFrameSize() {
        return frameSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Song song = (Song) o;
        return  title.equals(song.title) && artist.equals(song.artist);
    }

    @Override
    public int hashCode() {
        String lowerTitle = title.toLowerCase(Locale.ROOT);
        String lowerArtist = artist.toLowerCase(Locale.ROOT);

        int result = lowerTitle.hashCode();
        result = 31 * result + lowerArtist.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return title + "-" + artist;
    }
}
