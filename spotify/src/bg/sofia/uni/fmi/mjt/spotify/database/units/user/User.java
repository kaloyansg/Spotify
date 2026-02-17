package bg.sofia.uni.fmi.mjt.spotify.database.units.user;

import bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions.InvalidEmailException;
import bg.sofia.uni.fmi.mjt.spotify.database.units.user.exceptions.InvalidUserException;

public class User {
    private final String email;
    private final String password;

    public static User of(String line) throws InvalidUserException, InvalidEmailException {
        String[] fields = line.split(",", 2);

        if (fields.length != 2) {
            throw new InvalidUserException("User line must have email,password format");
        }
        return new User(fields[0], fields[1]);
    }

    public User(String email, String password) throws InvalidEmailException {
        if (email == null || email.isEmpty()) {
            throw new NullPointerException("email is null");
        }
        if (password == null || password.isEmpty()) {
            throw new NullPointerException("password is null");
        }
        if (!email.contains("@")) {
            throw new InvalidEmailException("Invalid email: "  + email);
        }
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public Boolean checkPassword(String password) {
        return password.equals(this.password);
    }

    public boolean checkPassword(User user) {
        return this.password.equals(user.password);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return email.equals(((User)o).email);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }

    @Override
    public String toString() {
        return email + "," + password;
    }
}
