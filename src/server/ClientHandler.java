package server;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Set<ClientHandler> connectedClients; // referência ao pool global
    private PrintWriter out;
    private String clientName;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ClientHandler(Socket socket, Set<ClientHandler> connectedClients) {
        this.socket = socket;
        this.connectedClients = connectedClients;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
        ) {
            // PrintWriter mantido como campo para poder enviar mensagens de fora
            out = new PrintWriter(socket.getOutputStream(), true);

            // Protocolo de handshake: primeira linha é o nome
            out.println("Digite seu nome:");
            clientName = in.readLine();
            if (clientName == null || clientName.isBlank()) clientName = "Anônimo";

            log("Conectado como: " + clientName);
            broadcast("[" + clientName + " entrou no chat]", this);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("/sair")) break;

                String formatted = String.format("[%s] %s: %s",
                        LocalTime.now().format(FMT), clientName, message);

                log("Recebido → " + formatted);
                broadcast(formatted, null); // null = envia para todos incluindo remetente
            }

        } catch (IOException e) {
            log("Conexão encerrada abruptamente: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    // Envia mensagem para todos os clientes conectados
    public void broadcast(String message, ClientHandler exclude) {
        synchronized (connectedClients) {
            for (ClientHandler client : connectedClients) {
                if (client != exclude) {
                    client.sendMessage(message);
                }
            }
        }
    }

    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }

    private void disconnect() {
        try {
            connectedClients.remove(this);
            if (clientName != null) {
                broadcast("[" + clientName + " saiu do chat]", this);
            }
            socket.close();
            log("Desconectado.");
        } catch (IOException e) {
            log("Erro ao fechar socket: " + e.getMessage());
        }
    }

    private void log(String msg) {
        System.out.printf("[SERVER][%s] %s → %s%n",
                LocalTime.now().format(FMT),
                socket.getRemoteSocketAddress(),
                msg);
    }
}