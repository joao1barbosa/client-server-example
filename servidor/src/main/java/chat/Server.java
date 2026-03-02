package chat;

import chat.auth.Authenticator;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static final int PORT = 12345;
    private static final String KEYSTORE_PATH = "servidor.keystore";
    private static final String KEYSTORE_PASS = "senhaKeystore";

    private static final Authenticator authenticator = new Authenticator();

    // Mapa thread-safe: username → handler do cliente
    private static final Map<String, ClientHandler> connectedClients =
            Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) throws Exception {

        // Carregar o keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(KEYSTORE_PATH), KEYSTORE_PASS.toCharArray());

        // Configurar o gerenciador de chaves
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, KEYSTORE_PASS.toCharArray());

        // Criar o contexto SSL
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        // Trocar ServerSocket por SSLServerSocket
        SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
        try (ServerSocket serverSocket = factory.createServerSocket(PORT)) {

            System.out.println("Servidor aguardando conexões na porta " + PORT + " (TLS)...");

            boolean running = true;
            while (running) {
                Socket client = serverSocket.accept();
                System.out.println("Nova conexão: " + client.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(client, authenticator, connectedClients);
                Thread thread = new Thread(handler);
                thread.setDaemon(true);
                thread.start();
            }
        }

    }

}