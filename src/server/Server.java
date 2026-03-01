package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Server {

    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 10;

    // Set thread-safe para rastrear clientes conectados
    private static final Set<ClientHandler> connectedClients =
            Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("=== Servidor de Chat iniciado na porta " + PORT + " ===");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                // Bloqueia aqui até um cliente conectar — isso é o accept()
                Socket clientSocket = serverSocket.accept();

                if (connectedClients.size() >= MAX_CLIENTS) {
                    System.out.println("Limite de clientes atingido. Recusando conexão.");
                    clientSocket.close();
                    continue;
                }

                System.out.println("Nova conexão de: " + clientSocket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(clientSocket, connectedClients);
                connectedClients.add(handler);

                // Cada cliente roda em sua própria thread
                Thread thread = new Thread(handler);
                thread.setDaemon(true); // encerra com o servidor
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Erro fatal no servidor: " + e.getMessage());
        }
    }
}