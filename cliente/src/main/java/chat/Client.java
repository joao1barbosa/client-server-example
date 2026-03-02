package chat;

import chat.comum.protocolo.Message;
import java.io.InputStream;

public class Client {
    public static void main(String[] args) {
        // Carregar o certificado do classpath
        InputStream certStream = Client.class.getResourceAsStream("/servidor.crt");

        System.out.println("Hello World!");
    }
}
