package bg.sofia.uni.fmi.mjt.spotify.database.units.user;

import bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions.InvalidEmailException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions.InvalidUserException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private static final String TEST_EMAIL = "test@mail.com";
    private static final String TEST_PASS = "secret123";


    @Test
    void testConstructorValid() throws InvalidEmailException {
        User user = new User(TEST_EMAIL, TEST_PASS);

        assertNotNull(user);
        assertEquals(TEST_EMAIL, user.getEmail());
    }

    @Test
    void testConstructorEmailNull() {
        assertThrows(NullPointerException.class,
                () -> new User(null, TEST_PASS),
                "Must throw NullPointerException if email is null");
    }

    @Test
    void testConstructorEmailEmpty() {
        assertThrows(NullPointerException.class,
                () -> new User("", TEST_PASS),
                "Must throw NullPointerException if email is empty");
    }

    @Test
    void testConstructorPasswordNull() {
        assertThrows(NullPointerException.class,
                () -> new User(TEST_EMAIL, null),
                "Must throw NullPointerException if password is null");
    }

    @Test
    void testConstructorPasswordEmpty() {
        assertThrows(NullPointerException.class,
                () -> new User(TEST_EMAIL, ""),
                "Must throw NullPointerException if password is empty");
    }

    @Test
    void testConstructorEmailInvalid() {
        assertThrows(InvalidEmailException.class,
                () -> new User("invalid-email.com", TEST_PASS),
                "Must throw InvalidEmailException if '@' is missing");
    }

    @Test
    void testOfValid() throws InvalidUserException, InvalidEmailException {
        String line = "john@doe.com,password123";
        User user = User.of(line);

        assertEquals("john@doe.com", user.getEmail());
        assertTrue(user.checkPassword("password123"));
    }

    @Test
    void testOfInvalid() {
        String line = "john@doe.com-password123";
        assertThrows(InvalidUserException.class,
                () -> User.of(line),
                "Must throw InvalidUserException if line does not contain a comma");
    }

    @Test
    void testOfEmailMissingAt() {
        String line = "notAnEmail,pass";
        assertThrows(InvalidEmailException.class,
                () -> User.of(line),
                "Should propagate InvalidEmailException from constructor");
    }

    @Test
    void testCheckPassword() throws InvalidEmailException {
        User user = new User(TEST_EMAIL, TEST_PASS);
        assertTrue(user.checkPassword(TEST_PASS));
    }

    @Test
    void testCheckPasswordWrongPassword() throws InvalidEmailException {
        User user = new User(TEST_EMAIL, TEST_PASS);
        assertFalse(user.checkPassword("wrongPass"));
    }

    @Test
    void testCheckPasswordCorrectPassword() throws InvalidEmailException {
        User user = new User(TEST_EMAIL, TEST_PASS);
        User loginAttempt = new User(TEST_EMAIL, TEST_PASS);

        assertTrue(user.checkPassword(loginAttempt));
    }

    @Test
    void testCheckWrongPassword() throws InvalidEmailException {
        User user = new User(TEST_EMAIL, TEST_PASS);
        User loginAttempt = new User(TEST_EMAIL, "wrongPass");

        assertFalse(user.checkPassword(loginAttempt));
    }

    @Test
    void testEqualsAndHashCode() throws InvalidEmailException {
        User user1 = new User(TEST_EMAIL, "passA");
        User user2 = new User(TEST_EMAIL, "passB");

        assertEquals(user1, user2, "Users with same email must be equal");
        assertEquals(user1.hashCode(), user2.hashCode(), "HashCodes must match for equal objects");
    }

    @Test
    void testNotEqualsDifferentEmail() throws InvalidEmailException {
        User user1 = new User("a@test.com", TEST_PASS);
        User user2 = new User("b@test.com", TEST_PASS);

        assertNotEquals(user1, user2);
    }

    @Test
    void testNotEqualsNullAndDifferentClass() throws InvalidEmailException {
        User user = new User(TEST_EMAIL, TEST_PASS);

        assertNotEquals(user, null);
        assertNotEquals(user, "Some String");
    }

    @Test
    void testToString() throws InvalidEmailException {
        User user = new User("me@here.bg", "myPass");
        assertEquals("me@here.bg,myPass", user.toString());
    }
}