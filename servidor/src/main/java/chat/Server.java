package chat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Server {

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {

        ServerSocket server = new ServerSocket(12345);
        System.out.println("= Servidor Iniciado =");
        System.out.println("- Aguardando Conexões -");

        Socket cliente = server.accept();
        System.out.println("Cliente conectado | Endereço: " + cliente.getRemoteSocketAddress());

        InputStream input = cliente.getInputStream();

        // Leitura do cabeçalho + payload
        byte[] header       = input.readNBytes(4);
        int payloadSize     = ByteBuffer.wrap(header).getInt();
        byte[] payloadBytes = input.readNBytes(payloadSize);
        String json         = new String(payloadBytes, "UTF-8");

        JsonObject message = gson.fromJson(json, JsonObject.class);

        String type = message.get("type").getAsString();
        System.out.println("Tipo recebido: " + type);

        switch (type) {
            case "LOGIN" -> handleLogin(message);
            default      -> System.out.println("Tipo desconhecido: " + type);
        }

        cliente.close();
        server.close();
    }

    private static void handleLogin(JsonObject message) {
        String username = message.get("username").getAsString();
        String password = message.get("password").getAsString();

        System.out.println("Tentativa de login — usuário: " + username + " | senha: " + password);
    }
}