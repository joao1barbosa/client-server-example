package chat;

import chat.comum.protocolo.Message;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.net.ssl.*;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) throws Exception {

        // Configuração Criptografia
        InputStream certStream = Client.class.getResourceAsStream("/servidor.crt");

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(certStream);

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStore.setCertificateEntry("servidor", cert);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        // Conexão com o servidor
        SSLSocketFactory factory = sslContext.getSocketFactory();
        Socket socket = factory.createSocket(HOST, PORT);
        System.out.println("Conectado ao servidor " + HOST + ":" + PORT);

        // Login
        Scanner keyboard = new Scanner(System.in);

        System.out.print("Usuário: ");
        String username = keyboard.nextLine();

        System.out.print("Senha: ");
        String password = keyboard.nextLine();

        JsonObject loginMessage = new JsonObject();
        loginMessage.addProperty("type", "LOGIN");
        loginMessage.addProperty("username", username);
        loginMessage.addProperty("password", password);
        Message.send(loginMessage, socket.getOutputStream());

        // Aguarda resposta do login
        JsonObject loginResponse = Message.read(socket.getInputStream());
        String responseType = loginResponse.get("type").getAsString();

        if (responseType.equals("LOGIN_FAIL")) {
            String reason = loginResponse.get("reason").getAsString();
            System.out.println("Login recusado: " + reason);
            socket.close();
            return;
        }

        // Login bem-sucedido
        System.out.println("Login bem-sucedido! Bem-vindo, " + username + ".");

        JsonArray online = loginResponse.getAsJsonArray("online");
        if (online.size() == 0) {
            System.out.println("Nenhum outro usuário conectado no momento.");
        } else {
            System.out.println("Usuários online: " + online);
        }

        System.out.println("Digite sua mensagem ou /sair para encerrar.\n");

        // Thread de escuta
        Thread listener = new Thread(() -> {
            try {
                while (true) {
                    JsonObject incoming = Message.read(socket.getInputStream());
                    String type = incoming.get("type").getAsString();

                    switch (type) {
                        case "BROADCAST" -> {
                            String from = incoming.get("from").getAsString();
                            String text = incoming.get("text").getAsString();
                            String time = incoming.get("time").getAsString();
                            System.out.println("[" + time + "] " + from + ": " + text);
                        }
                        case "USER_JOINED" -> {
                            String joined = incoming.get("username").getAsString();
                            System.out.println(">>> " + joined + " entrou no chat. Online: " + incoming.getAsJsonArray("online"));
                        }
                        case "USER_LEFT" -> {
                            String left = incoming.get("username").getAsString();
                            System.out.println(">>> " + left + " saiu do chat. Online: " + incoming.getAsJsonArray("online"));
                        }
                        default -> System.out.println("Mensagem desconhecida: " + type);
                    }
                }
            } catch (Exception e) {
                System.out.println("Conexão com o servidor encerrada.");
            }
        });
        listener.setDaemon(true);
        listener.start();

        // Thread principal
        while (keyboard.hasNextLine()) {
            String input = keyboard.nextLine();

            if (input.equalsIgnoreCase("/sair")) {
                System.out.println("Encerrando...");
                break;
            }

            if (input.isBlank()) continue;

            JsonObject chatMessage = new JsonObject();
            chatMessage.addProperty("type", "CHAT");
            chatMessage.addProperty("text", input);
            Message.send(chatMessage, socket.getOutputStream());
        }

        socket.close();
    }
}
