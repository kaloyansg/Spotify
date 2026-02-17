package bg.sofia.uni.fmi.mjt.spotify.server.portsmanager;

import bg.sofia.uni.fmi.mjt.spotify.database.Database;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.InvalidPasswordException;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingUserException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions.InvalidEmailException;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.PortOccupiedException;
import bg.sofia.uni.fmi.mjt.spotify.server.portsmanager.exceptions.AlreadyLoggedUserException;
import bg.sofia.uni.fmi.mjt.spotify.server.portsmanager.exceptions.NotLoggedUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PortsManagerTest {

    private static final long START_PORT = 7777;

    @Mock
    private Database database;

    private PortsManager portsManager;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        portsManager = new PortsManager(START_PORT, database);
        user = new User("test@mail.com", "password");
    }

    @Test
    void testLogIn() throws Exception {
        portsManager.logInUser(user);
        verify(database).checkUser(user);
        assertEquals(START_PORT, portsManager.getUserPort(user), "First user was not assigned to the start port");
    }

    @Test
    void testLogInPorts() throws Exception {
        User user2 = new User("user2@mail.com", "pass");
        portsManager.logInUser(user);
        portsManager.logInUser(user2);

        assertEquals(START_PORT, portsManager.getUserPort(user), "First user was not assigned to the start port");
        assertEquals(START_PORT + 1, portsManager.getUserPort(user2), "Second user was not assigned to the port after the first");
    }

    @Test
    void testLogInAlreadyLogged() throws Exception {
        portsManager.logInUser(user);

        assertThrows(AlreadyLoggedUserException.class,
                () -> portsManager.logInUser(user),
                "Should throw if user tries to log in twice");
    }

    @Test
    void testLogInInvalid() throws Exception {
        doThrow(new InvalidPasswordException("Bad pass")).when(database).checkUser(user);

        assertThrows(InvalidPasswordException.class,
                () -> portsManager.logInUser(user), "Invalid password must throw");

        assertEquals(-1, portsManager.getUserPort(user), "Not logged user should not be found");
    }

    @Test
    void testLogInNull() throws Exception {
        assertDoesNotThrow(() -> portsManager.logInUser(null), "Null user must throw");
        verify(database, never()).checkUser(any());
    }

    @Test
    void testLogOut() throws Exception {
        portsManager.logInUser(user);
        long assignedPort = portsManager.getUserPort(user);
        assertEquals(START_PORT, assignedPort, "First user was not assigned to the start port");

        portsManager.logOut(user);
        assertEquals(-1, portsManager.getUserPort(user), "Logged out user should not be found");

        User user2 = new User("new@mail.com", "pass");
        portsManager.logInUser(user2);

        assertEquals(START_PORT, portsManager.getUserPort(user2),
                "Port should be recycled after logout");
    }

    @Test
    void testLogOutNotLogged() {
        assertThrows(NotLoggedUserException.class,
                () -> portsManager.logOut(user), "Not logged user must throw when logged out");
    }

    @Test
    void testLogOutNotExist() throws Exception {
        doThrow(new NonExistingUserException("No user")).when(database).checkUser(eq(user.getEmail()), anyString());

        assertThrows(NonExistingUserException.class,
                () -> portsManager.logOut(user), "Not logged user must throw when logged out");
    }

    @Test
    void testIsPortStreamingLocked() {
        portsManager.lockPort(START_PORT);

        assertThrows(PortOccupiedException.class,
                () -> portsManager.isPortStreaming(START_PORT), "Must throw for streaming ports");
    }

    @Test
    void testFreePortReleasesLock() {
        portsManager.lockPort(START_PORT);
        portsManager.freePort(START_PORT);
        assertDoesNotThrow(() -> portsManager.isPortStreaming(START_PORT), "Not streaming port must not throw");
    }

    @Test
    void testLockPortNotAvailable() {
        long testPort = 99999;
        portsManager.lockPort(testPort);
        assertDoesNotThrow(() -> portsManager.isPortStreaming(testPort), "Not available port must not throw, cannot be locked");
    }

    @Test
    void testGetUserPortUnknownUser() throws InvalidEmailException {
        User unknownUser = new User("unknown@test.com", "pass");

        assertEquals(-1, portsManager.getUserPort(unknownUser), "Unknown user should not assigned to any port");
    }
}