package bg.sofia.uni.fmi.mjt.spotify.server.portsmanager;

import bg.sofia.uni.fmi.mjt.spotify.database.Database;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.InvalidPasswordException;
import bg.sofia.uni.fmi.mjt.spotify.database.exceptions.NonExistingUserException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.User;
import bg.sofia.uni.fmi.mjt.spotify.server.exceptions.PortOccupiedException;
import bg.sofia.uni.fmi.mjt.spotify.server.portsmanager.exceptions.AlreadyLoggedUserException;
import bg.sofia.uni.fmi.mjt.spotify.server.portsmanager.exceptions.NotLoggedUserException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class PortsManager {
    private final Database database;

    private final PriorityQueue<Long> portsPool;
    private final Map<User, Long> portsByUser;
    private final Set<Long> streamingPorts;

    private final Object lock = new Object();

    public PortsManager(long firstPortIdx, Database database) {
        this.database = database;

        portsPool = new PriorityQueue<>();
        portsPool.add(firstPortIdx);

        portsByUser = new HashMap<>();
        streamingPorts = new HashSet<>();
    }

    public void logInUser(User user)
            throws AlreadyLoggedUserException, NonExistingUserException, InvalidPasswordException {
        if (user != null) {
            connectUserToPort(user);
        }
    }

    public void logOut(User user) throws NotLoggedUserException, NonExistingUserException {
        if (user != null) {
            disconnectUserFromPort(user);
        }
    }

    private void connectUserToPort(User user) throws AlreadyLoggedUserException,
            NonExistingUserException, InvalidPasswordException {
        synchronized (lock) {
            database.checkUser(user);
            if (portsByUser.containsKey(user)) {
                throw new AlreadyLoggedUserException("User " + user.getEmail() + " is already logged in");
            }

            long port = portsPool.poll();
            if (portsPool.isEmpty()) {
                portsPool.add(port + 1);
            }
            portsByUser.put(user, port);
        }
    }

    private void disconnectUserFromPort(User user) throws NotLoggedUserException,  NonExistingUserException {
        synchronized (lock) {
            checkRegistered(user);
            if (!portsByUser.containsKey(user)) {
                throw new NotLoggedUserException("User " + user.getEmail() + " is not logged in");
            }

            portsPool.add(portsByUser.get(user));
            portsByUser.remove(user);
        }
    }

    private void checkRegistered(User user) throws NonExistingUserException {
        try {
            database.checkUser(user.getEmail(), "");
        } catch (InvalidPasswordException e) {
            //ignore
        }
    }

    public long getUserPort(User user) {
        if (portsByUser.containsKey(user)) {
            return portsByUser.get(user);
        }
        return -1;
    }

    public void lockPort(long port) {
        if (!portsPool.contains(port)) {
            return;
        }
        streamingPorts.add(port);
    }

    public void isPortStreaming(long port) throws PortOccupiedException {
        if (streamingPorts.contains(port)) {
            throw new PortOccupiedException("Cannot play a song, while listening to another");
        }
    }

    public void freePort(long port) {
        streamingPorts.remove(port);
    }
}
