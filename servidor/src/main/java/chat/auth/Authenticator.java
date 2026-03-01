package chat.auth;

import java.util.Map;

public class Authenticator {

    private static final Map<String, String> USERS = Map.of(
            "joao",    "senha123",
            "marcelo", "senha456",
            "rubens",  "senha789"
    );

    public boolean validate(String username, String password) {
        return USERS.containsKey(username)
                && USERS.get(username).equals(password);
    }
}