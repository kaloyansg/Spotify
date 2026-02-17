package bg.sofia.uni.fmi.mjt.spotify.database;

import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.InvalidPasswordException;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingPlaylistException;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingSongException;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingUserException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.exceptions.SongAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.DefaultPlaylist;
import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.database.units.playlist.exceptions.InvalidPlaylistInfoException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.song.Song;
import bg.sofia.uni.fmi.mjt.spotify.database.units.song.exceptions.InvalidSongException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions.InvalidEmailException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions.InvalidUserException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseImpl implements Database {
    private final String databaseDir;
    private final String songsSrc;
    private final String usersSrc;
    private final String playlistsSrc;
    private static final String WAV = "*.wav";

    private final Set<Song> songs;
    private final Map<User, Set<Playlist>> playlistsByUser;

    public DatabaseImpl(String databaseDir, String songsSrc, String usersSrc, String playlistsSrc) {
        this.databaseDir = databaseDir;
        this.songsSrc = songsSrc;
        this.usersSrc = usersSrc;
        this.playlistsSrc = playlistsSrc;

        songs = new HashSet<>();
        playlistsByUser = new HashMap<>();
        loadSongs();
        loadUsers();
        loadPlaylists();
    }

    private void loadSongs() {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Path.of(databaseDir + songsSrc), WAV)) {
            for (Path filePath : directoryStream) {
                if (!Files.isDirectory(filePath)) {
                    String fileName = filePath.getFileName().toString();
                    try {
                        songs.add(Song.of(databaseDir + songsSrc, fileName));
                    } catch (InvalidSongException e) {
                        System.out.println("The song: " + fileName + " is not loaded");
                        //continue with the rest
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("The playlists folder could not be opened", e);
        }
    }

    private void loadUsers() {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(databaseDir + usersSrc))) {
            for (String line : reader.lines().collect(Collectors.toSet())) {
                try {
                    User currUser = User.of(line);
                    playlistsByUser.put(currUser, new HashSet<>());
                } catch (InvalidUserException | InvalidEmailException e) {
                    System.out.println(e.getMessage() + ": " + line);
                    //continue
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("The users folder could not be opened", e);
        }
    }

    private void loadPlaylists() {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(databaseDir + playlistsSrc))) {
            for (String line : reader.lines().collect(Collectors.toSet())) {
                try {
                    DefaultPlaylist currPL = DefaultPlaylist.of(line, this);

                    if (!playlistsByUser.containsKey(currPL.getOwner())) {
                        playlistsByUser.put(currPL.getOwner(), new HashSet<>());
                    }
                    playlistsByUser.get(currPL.getOwner()).add(currPL);
                } catch (InvalidPlaylistInfoException e) {
                    System.out.println(e.getMessage() + ": " + line);
                    //continue with the rest
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("The playlists folder could not be opened", e);
        }
    }

    @Override
    public void registerUser(String email, String password) throws UserAlreadyExistsException, InvalidEmailException {
        synchronized (playlistsByUser) {
            User user = new User(email, password);
            if (playlistsByUser.containsKey(user)) {
                throw new UserAlreadyExistsException("User already exists" + user.toString());
            }
            playlistsByUser.put(user, new HashSet<>());
        }
    }

    @Override
    public void checkUser(String email, String password) throws NonExistingUserException, InvalidPasswordException {
        synchronized (playlistsByUser) {
            User user = checkUser(email);
            if (user == null) {
                throw new NonExistingUserException("There is no such a user: " + email);
            }
            if (user.checkPassword(password)) {
                return;
            }
        }
        throw new  InvalidPasswordException("Invalid password for user: " + email);
    }

    @Override
    public void checkUser(User user) throws NonExistingUserException, InvalidPasswordException {
        synchronized (playlistsByUser) {
            for (User curr : new ArrayList<>(playlistsByUser.keySet())) {
                if (curr.getEmail().equals(user.getEmail())) {
                    if (!curr.checkPassword(user)) {
                        throw new InvalidPasswordException("Invalid password for user: " + user.getEmail());
                    }
                    return;
                }
            }
            throw new NonExistingUserException("There is no such a user: " + user.getEmail());
        }
    }

    @Override
    public Collection<Song> searchSongs(String... filters) {
        List<Song> snapshot;
        synchronized (songs) {
            if (filters == null || filters.length == 0) {
                return new ArrayList<>(songs);
            }
            snapshot = new ArrayList<>(songs);
        }

        return snapshot.stream().filter(song -> {
            boolean check = true;
            for (String filter : filters) {
                String filterLower = filter.toLowerCase();
                boolean matchesArtist = song.getArtist().toLowerCase().contains(filterLower);
                boolean matchesTitle = song.getTitle().toLowerCase().contains(filterLower);
                check &= (matchesArtist || matchesTitle);
            }
            return check;
        }).toList();
    }

    @Override
    public Collection<Song> getTopSongs(int limit) {
        if (limit < 1) {
            return new ArrayList<>();
        }

        List<Song> snapshot;
        synchronized (songs) {
            snapshot = new ArrayList<>(songs);
        }
        return snapshot.stream()
                .sorted(Comparator.comparingInt(Song::getStreamsCnt).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public void createPlaylist(String title, String owner)
            throws NonExistingUserException, InvalidPlaylistInfoException {
        synchronized (playlistsByUser) {
            User user = checkUser(owner);
            if (user == null) {
                throw new NonExistingUserException("no such a user: " + owner);
            }
            playlistsByUser.putIfAbsent(user, new HashSet<>());
            playlistsByUser.get(user).add(new DefaultPlaylist(title, user));
        }
    }

    @Override
    public void addSong(String playListTitle, String owner, String title, String artist)
            throws SongAlreadyExistsException, NonExistingPlaylistException, NonExistingSongException {
        synchronized (playlistsByUser) {
            User user = checkUser(owner);
            if (user == null) {
                throw new NonExistingPlaylistException("No such a user: " + owner + "with playlist: " + playListTitle);
            }
            if (playlistsByUser.get(user) != null) {
                for (Playlist playlist : playlistsByUser.get(user)) {
                    if (playlist.getTitle().equalsIgnoreCase(playListTitle)) {
                        Song song = getSong(title, artist);
                        playlist.addSong(getSong(title, artist));
                        return;
                    }
                }
            }
        }
        throw new NonExistingPlaylistException("No such a playlist: " + playListTitle);
    }

    @Override
    public Collection<Song> getPlaylistSongs(String title, String owner) throws NonExistingPlaylistException {
        return getPlaylist(title, owner).getSongs();
    }

    @Override
    public Playlist getPlaylist(String title, String owner) throws NonExistingPlaylistException {
        synchronized (playlistsByUser) {
            User user = checkUser(owner);
            if (user == null) {
                throw new NonExistingPlaylistException("No such a user: " + owner + "with playlist: " + title);
            }
            if (playlistsByUser.get(user) != null) {
                for (Playlist playlist : playlistsByUser.get(user)) {
                    if (playlist.getTitle().equals(title)) {
                        return playlist;
                    }
                }
            }
        }
        throw new NonExistingPlaylistException("No such a playlist: " + title);
    }

    @Override
    public Song getSong(String title, String artist) throws NonExistingSongException {
        synchronized (songs) {
            for (Song song : songs) {
                if (song.getTitle().equalsIgnoreCase(title) && song.getArtist().equalsIgnoreCase(artist)) {
                    return song;
                }
            }
        }
        throw new NonExistingSongException("No such a song: " + title + " - " + artist);
    }

    @Override
    public Song getSong(String string)  throws NonExistingSongException {
        String[] split = string.split("(\\s*-\\s*)");

        if (split.length < 2) {
            throw new NonExistingSongException("Song: " + string + " not found");
        }

        return getSong(split[0].strip(), split[1].strip());
    }

    @Override
    public String getSongSrc() {
        return databaseDir + songsSrc;
    }

    @Override
    public void close() throws IOException {
        saveCollectionToFile(playlistsByUser.keySet(), usersSrc);

        Set<Playlist> allPlaylists = playlistsByUser.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());
        saveCollectionToFile(allPlaylists, playlistsSrc);
    }

    private void saveCollectionToFile(Collection<?> obs, String fileName) throws IOException {
        Path dir = Path.of(databaseDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of(databaseDir + fileName))) {
            for (Object ob : obs) {
                bufferedWriter.write(ob.toString() + System.lineSeparator());
            }
        }
    }

    private User checkUser(String email) {
        for (User user : playlistsByUser.keySet()) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }
}
