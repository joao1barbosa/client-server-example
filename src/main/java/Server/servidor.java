package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import Cliente.configurador;

public class servidor {

    private static final int PORT       = configurador.getPorta();
    private static final int MAX_CLIENTS = configurador.getMaxClientes();

    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final DateTimeFormatter dtf =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        new servidor().start();
    }

    public void start() {
        log("Iniciando: "  + configurador.getNome());
        log("Versao: "     + configurador.getVersao());
        log("Ambiente: "   + configurador.getAmbiente());
        log("Porta: "      + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {

                if (clients.size() >= MAX_CLIENTS) {
                    Socket recusado = serverSocket.accept();
                    recusado.close();
                    log("Conexao recusada: limite atingido.");
                    continue;
                }

                Socket clientSocket = serverSocket.accept();
                log("Nova conexao de: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            log("ERRO CRITICO: " + e.getMessage());
        }
    }

    public void log(String message) {
        System.out.println("[" + dtf.format(LocalDateTime.now()) + "] " + message);
    }

    public void broadcastMessage(String message, ClientHandler sender) {
        log("BROADCAST de [" + sender.getClientName() + "]: " + message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void sendPrivateMessage(String message, ClientHandler sender, String targetName) {
        for (ClientHandler client : clients) {
            if (client.getClientName().equalsIgnoreCase(targetName)) {
                client.sendMessage("[Privado de " + sender.getClientName() + "]: " + message);
                sender.sendMessage("[Voce -> " + targetName + "]: " + message);
                log("MSG PRIVADA de [" + sender.getClientName() + "] para [" + targetName + "]");
                return;
            }
        }
        sender.sendMessage("[SISTEMA] Usuario '" + targetName + "' nao encontrado.");
    }

    public void kickCliente(String targetName, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client.getClientName().equalsIgnoreCase(targetName)) {
                client.sendMessage("[SISTEMA] Voce foi removido por "
                    + sender.getClientName());
                removeClient(client);
                return;
            }
        }
        sender.sendMessage("[SISTEMA] Usuario '" + targetName + "' nao encontrado.");
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        log("Cliente [" + clientHandler.getClientName() + "] desconectado.");
        broadcastMessage("[SISTEMA] " + clientHandler.getClientName()
            + " " + configurador.getMensagemSaida(), clientHandler);
    }
}