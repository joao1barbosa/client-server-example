package chat;

import chat.auth.Authenticator;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static final int PORT = 12345;
    private static final Authenticator authenticator = new Authenticator();

    // Mapa thread-safe: username → handler do cliente
    private static final Map<String, ClientHandler> connectedClients =
            Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor aguardando conexões na porta " + PORT + "...");

        while (true) {
            Socket client = serverSocket.accept();
            System.out.println("Nova conexão: " + client.getRemoteSocketAddress());

            ClientHandler handler = new ClientHandler(client, authenticator, connectedClients);
            Thread thread = new Thread(handler);
            thread.setDaemon(true);
            thread.start();
        }

    }

}