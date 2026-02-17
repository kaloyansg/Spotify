package bg.sofia.uni.fmi.mjt.spotify.server.commands;

public enum CommandType {
    REGISTER("register"),
    LOGIN("login"),
    DISCONNECT("disconnect"),
    SEARCH("search"),
    TOP("top"),
    CREATE_PLAYLIST("create-playlist"),
    ADD_SONG_TO("add-song-to"),
    SHOW_PLAYLIST("show-playlist"),
    PLAY("play"),
    TERMINATE("terminate");

    private final String string;

    CommandType(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}
