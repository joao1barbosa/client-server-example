package Cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Usuario {

    private static final String HOST = configurador.getIp();
    private static final int    PORT = configurador.getPorta();

    public static void main(String[] args) {
        try (
            Socket socket          = new Socket(HOST, PORT);
            PrintWriter out        = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in      = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            Scanner consoleScanner = new Scanner(System.in)
        ) {
            System.out.println("Conectado ao servidor.");

            Thread serverListener = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (Exception e) {
                    System.out.println("Conexao encerrada.");
                }
            });
            serverListener.setDaemon(true);
            serverListener.start();

            String userInput;
            while (consoleScanner.hasNextLine()) {
                userInput = consoleScanner.nextLine();
                out.println(userInput);
                if ("sair".equalsIgnoreCase(userInput)) break;
            }

        } catch (IOException e) {
            System.err.println("Erro ao conectar: " + e.getMessage());
        }
    }
}
