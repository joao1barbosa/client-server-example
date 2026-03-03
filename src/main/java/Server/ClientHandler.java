package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import Cliente.configurador;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final servidor server;
    private PrintWriter out;
    private String clientName;
    private String perfil;

    public ClientHandler(Socket socket, servidor server) {
        this.clientSocket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (
            PrintWriter writer = new PrintWriter(
                clientSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()))
        ) {
            this.out = writer;

            out.println(configurador.getMensagemBoasVindas());
            out.println("Digite seu usuario:");
            String nome = reader.readLine();

            out.println("Digite sua senha:");
            String senha = reader.readLine();

            if (!configurador.validarUsuario(nome, senha)) {
                out.println("LOGIN_FAIL");
                return;
            }

            this.perfil    = configurador.getPerfilUsuario(nome);
            this.clientName = nome;

            out.println("LOGIN_OK. Bem-vindo, " + clientName
                + "! Perfil: " + perfil);

            server.log("Login: [" + clientName + "] Perfil: [" + perfil + "]");
            server.broadcastMessage(
                "[SISTEMA] " + clientName + " entrou no chat.", this);

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {

                if ("sair".equalsIgnoreCase(clientMessage.trim())) {
                    break;
                }

                if (clientMessage.toLowerCase().startsWith("/msg ")) {
                    String[] parts = clientMessage.split(" ", 3);
                    if (parts.length == 3) {
                        server.sendPrivateMessage(parts[2], this, parts[1]);
                    } else {
                        sendMessage("[SISTEMA] Uso: /msg <usuario> <mensagem>");
                    }

                } else if (clientMessage.toLowerCase().startsWith("/kick ")) {
                    if (this.perfil.equals("ADMIN")) {
                        String[] parts = clientMessage.split(" ", 2);
                        server.kickCliente(parts[1], this);
                    } else {
                        sendMessage("[SISTEMA] Apenas ADMIN pode usar /kick.");
                    }

                } else {
                    server.broadcastMessage(
                        "[" + perfil + "] " + clientName + ": " + clientMessage, this);
                }
            }

        } catch (IOException e) {
            server.log("Conexao perdida: [" + clientName + "]: " + e.getMessage());
        } finally {
            server.removeClient(this);
            try { clientSocket.close(); } catch (IOException e) { /* ignora */ }
        }
    }

    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }

    public String getClientName() {
        return this.clientName;
    }

    public String getPerfil() {
        return this.perfil;
    }
}