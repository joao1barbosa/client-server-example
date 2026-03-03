package Cliente;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class configurador {

    private static final JsonObject config = carregarConfig();

    private static JsonObject carregarConfig() {
        try (InputStream in = configurador.class
                .getClassLoader()
                .getResourceAsStream("dados.json")) {

            if (in == null) {
                throw new RuntimeException("dados.json nao encontrado!");
            }

            return new Gson().fromJson(
                new InputStreamReader(in, StandardCharsets.UTF_8),
                JsonObject.class
            );

        } catch (Exception e) {
            throw new RuntimeException("Erro: " + e.getMessage(), e);
        }
    }

    public static String getIp() {
        return config.getAsJsonObject("servidor").get("ip").getAsString();
    }

    public static int getPorta() {
        return config.getAsJsonObject("servidor").get("porta").getAsInt();
    }

    public static int getMaxClientes() {
        return config.getAsJsonObject("servidor").get("maxClientes").getAsInt();
    }

    public static String getNome() {
        return config.getAsJsonObject("servidor").get("nome").getAsString();
    }

    public static String getMensagemBoasVindas() {
        return config.getAsJsonObject("servidor").get("mensagemBoasVindas").getAsString();
    }

    public static String getMensagemSaida() {
        return config.getAsJsonObject("servidor").get("mensagemSaida").getAsString();
    }

    public static int getTimeoutMs() {
        return config.getAsJsonObject("servidor").get("timeoutMs").getAsInt();
    }

    public static boolean isLogEmArquivo() {
        return config.getAsJsonObject("servidor").get("logEmArquivo").getAsBoolean();
    }

    public static String getArquivoLog() {
        return config.getAsJsonObject("servidor").get("arquivoLog").getAsString();
    }

    public static String getVersao() {
        return config.getAsJsonObject("servidor").get("versao").getAsString();
    }

    public static String getAmbiente() {
        return config.getAsJsonObject("servidor").get("ambiente").getAsString();
    }

    public static boolean validarUsuario(String nome, String senha) {
        JsonArray usuarios = config.getAsJsonArray("usuarios");

        for (int i = 0; i < usuarios.size(); i++) {
            JsonObject u = usuarios.get(i).getAsJsonObject();

            if (u.get("nome").getAsString().equals(nome) &&
                u.get("senha").getAsString().equals(senha) &&
                u.get("ativo").getAsBoolean()) {
                return true;
            }
        }
        return false;
    }

    public static boolean usuarioExiste(String nome) {
        JsonArray usuarios = config.getAsJsonArray("usuarios");

        for (int i = 0; i < usuarios.size(); i++) {
            JsonObject u = usuarios.get(i).getAsJsonObject();
            if (u.get("nome").getAsString().equals(nome) &&
                u.get("ativo").getAsBoolean()) {
                return true;
            }
        }
        return false;
    }

    public static String getPerfilUsuario(String nome) {
        JsonArray usuarios = config.getAsJsonArray("usuarios");

        for (int i = 0; i < usuarios.size(); i++) {
            JsonObject u = usuarios.get(i).getAsJsonObject();
            if (u.get("nome").getAsString().equals(nome)) {
                return u.get("perfil").getAsString();
            }
        }
        return "DESCONHECIDO";
    }
}