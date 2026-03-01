package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("=== Cliente de Chat ===");
        System.out.println("Conectando em " + HOST + ":" + PORT + "...");

        try (
                Socket socket = new Socket(HOST, PORT);
                BufferedReader serverIn = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
                Scanner keyboard = new Scanner(System.in);
        ) {
            System.out.println("Conectado! Digite /sair para encerrar.\n");

            // Thread separada para RECEBER mensagens do servidor
            // (sem isso, o main ficaria bloqueado esperando input do teclado)
            Thread listener = new Thread(() -> {
                try {
                    String line;
                    while ((line = serverIn.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    System.out.println("[Conexão com o servidor encerrada]");
                }
            });
            listener.setDaemon(true);
            listener.start();

            // Main thread: lê do teclado e envia ao servidor
            while (keyboard.hasNextLine()) {
                String input = keyboard.nextLine();
                serverOut.println(input);
                if (input.equalsIgnoreCase("/sair")) break;
            }

        } catch (IOException e) {
            System.err.println("Erro ao conectar: " + e.getMessage());
        }

        System.out.println("Desconectado.");
    }
}