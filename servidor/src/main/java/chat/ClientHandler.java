package chat;

import chat.auth.Authenticator;
import chat.comum.protocolo.Message;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ClientHandler implements Runnable {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Socket socket;
    private final Authenticator authenticator;
    private final Map<String, ClientHandler> connectedClients;
    private String username;

    public ClientHandler(Socket socket, Authenticator authenticator,
                         Map<String, ClientHandler> connectedClients) {
        this.socket = socket;
        this.authenticator = authenticator;
        this.connectedClients = connectedClients;
    }

    @Override
    public void run() {
        try {
            // Primeira mensagem deve ser LOGIN
            JsonObject message = Message.read(socket.getInputStream());
            String type = message.get("type").getAsString();

            if (!type.equals("LOGIN")) {
                socket.close();
                return;
            }

            // Tentar autenticar
            if (!login(message)) return;

            // Loop principal — aguarda mensagens do cliente
            while (true) {
                JsonObject incoming = Message.read(socket.getInputStream());
                String incomingType = incoming.get("type").getAsString();

                switch (incomingType) {
                    case "CHAT" -> handleChat(incoming);
                    default     -> System.out.println("Tipo desconhecido: " + incomingType);
                }
            }

        } catch (Exception e) {
            System.out.println("Conexão encerrada: " + (username != null ? username : socket.getRemoteSocketAddress()));
        } finally {
            disconnect();
        }
    }

    private boolean login(JsonObject message) throws Exception {
        String user = message.get("username").getAsString();
        String pass = message.get("password").getAsString();

        JsonObject response = new JsonObject();

        if (!authenticator.validate(user, pass)) {
            response.addProperty("type", "LOGIN_FAIL");
            response.addProperty("reason", "INVALID_USER");
            Message.send(response, socket.getOutputStream());
            socket.close();
            return false;
        }

        if (authenticator.isConnected(user)) {
            response.addProperty("type", "LOGIN_FAIL");
            response.addProperty("reason", "ALREADY_CONNECTED");
            Message.send(response, socket.getOutputStream());
            socket.close();
            return false;
        }

        // Registrar usuário
        username = user;
        authenticator.connect(username);

        synchronized (connectedClients) {
            connectedClients.put(username, this);
        }

        // Responder LOGIN_OK com lista de conectados (excluindo o próprio)
        JsonArray online = new JsonArray();
        authenticator.getConnectedUsers().stream()
                .filter(u -> !u.equals(username))
                .forEach(online::add);

        response.addProperty("type", "LOGIN_OK");
        response.add("online", online);
        Message.send(response, socket.getOutputStream());

        System.out.println("Login bem-sucedido: " + username);

        // Avisar os outros que alguém entrou
        JsonObject joined = new JsonObject();
        joined.addProperty("type", "USER_JOINED");
        joined.addProperty("username", username);

        JsonArray allOnline = new JsonArray();
        authenticator.getConnectedUsers().forEach(allOnline::add);
        joined.add("online", allOnline);

        broadcast(joined, true); // true = excluir o próprio
        return true;
    }

    private void handleChat(JsonObject message) throws Exception {
        String text = message.get("text").getAsString();
        String time = LocalTime.now().format(FMT);

        System.out.println("[" + time + "] " + username + ": " + text);

        JsonObject broadcast = new JsonObject();
        broadcast.addProperty("type", "BROADCAST");
        broadcast.addProperty("from", username);
        broadcast.addProperty("text", text);
        broadcast.addProperty("time", time);

        broadcast(broadcast, true); // true = excluir o remetente
    }

    private void disconnect() {
        if (username == null) return;

        authenticator.disconnect(username);

        synchronized (connectedClients) {
            connectedClients.remove(username);
        }

        System.out.println("Usuário desconectado: " + username);

        // Avisar os outros que alguém saiu
        JsonObject left = new JsonObject();
        left.addProperty("type", "USER_LEFT");
        left.addProperty("username", username);

        JsonArray online = new JsonArray();
        authenticator.getConnectedUsers().forEach(online::add);
        left.add("online", online);

        broadcast(left, false); // false = enviar para todos restantes

        try { socket.close(); } catch (Exception ignored) {}
    }

    // excludeSelf = true  → envia para todos exceto este cliente
    // excludeSelf = false → envia para todos (usado após desconexão)
    private void broadcast(JsonObject message, boolean excludeSelf) {
        synchronized (connectedClients) {
            for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
                if (excludeSelf && entry.getKey().equals(username)) continue;
                try {
                    Message.send(message, entry.getValue().socket.getOutputStream());
                } catch (Exception e) {
                    System.out.println("Erro ao enviar para: " + entry.getKey());
                }
            }
        }
    }
}