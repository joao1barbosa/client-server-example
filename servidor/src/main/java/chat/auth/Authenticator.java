package chat.auth;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class Authenticator {

    private static final Map<String, String> USERS = Map.of(
            "joao",    "senha123",
            "rubens",  "senha789"
    );

    private final Set<String> connectedUsers = new HashSet<>();

    public boolean validate(String username, String password) {
        return USERS.containsKey(username)
                && USERS.get(username).equals(password);
    }

    public boolean isConnected(String username) {
        return connectedUsers.contains(username);
    }

    public void connect(String username) {
        connectedUsers.add(username);
    }

    public void disconnect(String username) {
        connectedUsers.remove(username);
    }

    public Set<String> getConnectedUsers() {
        return new HashSet<>(connectedUsers);
    }
}