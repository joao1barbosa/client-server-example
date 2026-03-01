package chat;

import chat.auth.Authenticator;
import chat.comum.protocolo.Message;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import com.google.gson.JsonArray;

public class Server {

    private static final Authenticator authenticator = new Authenticator();

    public static void main(String[] args) throws Exception {

        ServerSocket server = new ServerSocket(12345);
        System.out.println("= Servidor Iniciado =");
        System.out.println("- Aguardando Conexões -");

        Socket client = server.accept();
        System.out.println("Cliente conectado | Endereço: " + client.getRemoteSocketAddress());

        JsonObject message = Message.read(client.getInputStream());
        String type = message.get("type").getAsString();

        switch (type) {
            case "LOGIN" -> handleLogin(message, client);
            default      -> System.out.println("Tipo desconhecido: " + type);
        }

        client.close();
        server.close();
    }

    private static void handleLogin(JsonObject message, Socket client) throws Exception {
        String username = message.get("username").getAsString();
        String password = message.get("password").getAsString();

        JsonObject response = new JsonObject();

        if (!authenticator.validate(username, password)) {
            response.addProperty("type", "LOGIN_FAIL");
            response.addProperty("reason", "INVALID_USER_OR_PASSWORD");
        } else {
            response.addProperty("type", "LOGIN_OK");
            response.add("online", new JsonArray());
        }

        Message.send(response, client.getOutputStream());
    }
}