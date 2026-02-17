# **Spotify** 

A robust Client-Server music streaming application implemented in Java. This project demonstrates the use of `Java NIO` for non-blocking communication, `multi-threading` for audio streaming, and `Design Patterns` to ensure scalability and maintainability.


## **Features**

### User Management: 
Registration and Login functionality with secure password checks.

### Music Streaming: 
Real-time audio streaming of .wav files from server to client using a dedicated data channel.

### Search Engine: 
Search for songs by artist or title (supports partial matching).

### Playlists: 
Create custom playlists, add songs to them, and view playlist contents.

### Analytics: 
View the "Top N" most played songs globally.

### Concurrency:

Server handles multiple clients simultaneously using Selector.

Audio streaming happens on a separate thread to ensure the command interface remains responsive.

---

## **Project Structure & Architecture**

### 1. Database (Persistence Layer)
The application uses a custom file-based database located in `bg.sofia.uni.fmi.mjt.spotify.database`.

 - Storage: Data is persisted in text files (`users.txt`, `playlists.txt`) and a directory of `.wav` files.


 - In-Memory Caching: Upon startup, the `DatabaseImpl` loads metadata into thread-safe collections (`ConcurrentHashMap`, `Set`) for fast access.


 - Thread Safety: Critical sections are synchronized to handle concurrent write requests.

### 2. Server
   Located in `bg.sofia.uni.fmi.mjt.spotify.server`.

 - Non-Blocking I/O: Uses `ServerSocketChannel` and `Selector` to manage client connections without creating a thread per client for command processing.


 - Command Pattern: User inputs are parsed via `CommandFactory` and executed by `CommandExecutor`, ensuring the server logic is decoupled from command parsing.


 - Ports Manager: A specialized component that manages dynamic port allocation for audio streaming sessions.

### 3. Client
   Located in `bg.sofia.uni.fmi.mjt.spotify.client`.

 - Command Interface: Reads user input and sends requests to the server.


 - Audio Player: When a `play` command is accepted, the client spins up a separate `Player` thread to receive audio bytes and write them to the `SourceDataLine` (Speaker) using the `javax.sound.sampled` API.

## **Setup & Configuration**

### Prerequisites
 - Java JDK 17 or higher.
 - Maven (optional, if used for build management).

### Directory Requirements
To run the server, you must ensure the file structure matches the `DatabaseImpl` configuration. Create the following folder structure in your project root:

```
project/
└── spotify/
└── resources/
├── client/
│   └── clientErrLog.txt   <-- Auto-created
├── server/
│   └── serverErrLog.txt   <-- Auto-created
└── database/
├── users.txt          <-- Stores "email,password"
├── playlists.txt      <-- Stores playlist data
└── songs/             <-- Put your .wav files here
```

### Song Naming Convention
Audio files inside the `database/songs/` folder must follow this naming format to be parsed correctly: `Title - Artist.wav`

Example: `Bohemian Rhapsody - Queen.wav`

---

## How to Run
 1. Start the Server

    Run the `main` method in `bg.sofia.uni.fmi.mjt.spotify.server.Server`.
    - Port: Defaults to `6666`. 
    - Console: You will see "Server started on port 6666".


 2. Start the Client

    Run the `main` method in `bg.sofia.uni.fmi.mjt.spotify.client.Client`.
    - Console: You will see "Connected to the server."



### Commands
Once the client is running, use the following commands:
#### Authentication
 - **Register**: 
    `register <email> <password>`
- **Login**: 
  `login <email> <password>`
- **Disconnect**: 
  `disconnect`


#### Music & Search
- **Search for songs** (Checks both Title and Artist):
  `search <keyword1> <keyword2> ...`
- **Play a song**: 
  `play <Title - Artist>`
- **Stop playback**: 
  `stop`
- **View Top Songs** (By stream count): 
  `top <number>`

#### Playlists
- **Create a Playlist**: 
  `create-playlist <playlist_name>`
- **Add Song to Playlist**:
  `add-song-to <playlist_name> : <Title - Artist>`
- **Show Playlist**:
  `show-playlist <playlist_name>`
---

## Technical Highlights (Design Patterns)

 1. Command Pattern:

 - Classes: `Command`, `CommandExecutor`, `CommandFactory`, and concrete implementations like `PlayCommand`, `SearchCommand`.

 - Benefit: Easily extensible. To add a new feature (e.g., "Remove Song"), you simply add a new Command class and update the Factory, without touching the core Server loop.

 2. Factory Pattern:

 - Class: `CommandFactory`.

 - Benefit: Encapsulates the logic of parsing raw strings into executable objects.

 3. Singleton (Scoped):

 - The `Database` instance is shared across the server but injected via dependency injection, allowing for easier testing and resource management.


## Full Structure

```
project/
└── spotify/
    ├── resources/
    │   ├── client/
    │   │   └── clientErrLog.txt
    │   ├── database/
    │   │   ├── songs/
    │   │   ├── playlists.txt
    │   │   └── users.txt
    │   └── server/
    │       └── serverErrLog.txt
    ├── src/bg.sofia.uni.fmi.mjt.spotify/
    │   ├── client/
    │   │   ├── exceptions/
    │   │   │   └── InvalidOperationException.java
    │   │   ├── player/
    │   │   │   └── Player.java
    │   │   └── Client.java
    │   ├── database/
    │   │   ├── exceptions/
    │   │   │   ├── InvalidPasswordException.java
    │   │   │   ├── NonExistingPlaylistException.java
    │   │   │   ├── NonExistingSongException.java
    │   │   │   ├── NonExistingUserException.java
    │   │   │   └── UserAlreadyExistsException.java
    │   │   ├── units/
    │   │   │   ├── playlist/
    │   │   │   │   ├── exceptions/
    │   │   │   │   │   ├── InvalidPlaylistInfoException.java
    │   │   │   │   │   └── SongAlreadyExistsException.java
    │   │   │   │   ├── DefaultPlaylist.java
    │   │   │   │   └── Playlist.java
    │   │   │   ├── song/
    │   │   │   │   ├── exceptions/
    │   │   │   │   │   └── InvalidSongException.java
    │   │   │   │   └── Song.java
    │   │   │   └── user/
    │   │   │       ├── exceptions/
    │   │   │       │   ├── InvalidEmailException.java
    │   │   │       │   └── InvalidUserException.java
    │   │   │       └── User.java
    │   │   ├── Database.java
    │   │   └── DatabaseImpl.java
    │   └── server/
    │       ├── commands/
    │       │   ├── concretecommands/
    │       │   │   ├── AddSongToCommand.java
    │       │   │   ├── CreatePlaylistCommand.java
    │       │   │   ├── DisconnectCommand.java
    │       │   │   ├── LoginCommand.java
    │       │   │   ├── PlayCommand.java
    │       │   │   ├── PlaylistCommand.java
    │       │   │   ├── RegisterCommand.java
    │       │   │   ├── SearchCommand.java
    │       │   │   └── TopSongsCommand.java
    │       │   ├── Command.java
    │       │   ├── CommandExecutor.java
    │       │   ├── CommandFactory.java
    │       │   └── CommandType.java
    │       ├── exceptions/
    │       │   ├── PortOccupiedException.java
    │       │   └── UserAlreadyLoggedException.java
    │       ├── portsmanager/
    │       │   ├── exceptions/
    │       │   │   ├── AlreadyLoggedUserException.java
    │       │   │   └── NotLoggedUserException.java
    │       │   └── PortsManager.java
    │       ├── streamer/
    │       │   └── Streamer.java
    │       └── Server.java
    └── test/bg.sofia.uni.fmi.mjt.spotify/
        ├── database/
        │   ├── units/
        │   │   ├── playlist/
        │   │   │   └── DefaultPlaylistTest.java
        │   │   ├── song/
        │   │   │   └── SongTest.java
        │   │   └── user/
        │   │       └── UserTest.java
        │   └── DatabaseImplTest.java
        └── server/
            ├── commands/
            │   └── CommandFactoryTest.java
            └── portsmanager/
                └── PortsManagerTest.java
```
