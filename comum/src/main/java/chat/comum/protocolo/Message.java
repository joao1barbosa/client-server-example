package chat.comum.protocolo;

import com.google.gson.JsonObject;
import com.google.gson.Gson;
import java.io.*;
import java.nio.ByteBuffer;

public class Message {

    private static final Gson gson = new Gson();

    // ler body da requisição
    public static JsonObject read(InputStream input) throws Exception {
        byte[] header       = input.readNBytes(4);
        int payloadSize     = ByteBuffer.wrap(header).getInt();
        byte[] payloadBytes = input.readNBytes(payloadSize);
        String json         = new String(payloadBytes, "UTF-8");
        return gson.fromJson(json, JsonObject.class);
    }

    // escreve body da requisição
    public static void send(JsonObject mensagem, OutputStream output) throws Exception {
        byte[] payload = gson.toJson(mensagem).getBytes("UTF-8");
        byte[] header  = ByteBuffer.allocate(4).putInt(payload.length).array();
        output.write(header);
        output.write(payload);
        output.flush();
    }
}