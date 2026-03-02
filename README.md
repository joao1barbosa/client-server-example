# Chat Multiusuário — Cliente/Servidor

Sistema de chat em tempo real baseado em arquitetura cliente-servidor, desenvolvido em Java. A comunicação utiliza sockets TCP com TLS 1.3, protocolo próprio com cabeçalho binário e payload JSON.

## Visão Geral

```
┌──────────┐        TLS/TCP         ┌──────────┐
│ Cliente  │ ◄────────────────────► │ Servidor │
└──────────┘      porta 12345       └──────────┘
                                         ▲
                                         │
                                    ┌──────────┐
                                    │ Cliente  │
                                    └──────────┘
```

O servidor gerencia múltiplos clientes simultaneamente. Quando um usuário envia uma mensagem, ela é redistribuída a todos os outros conectados.

## Protocolo

A comunicação segue um protocolo próprio definido em [`PROTOCOL.md`](./PROTOCOL.md). Resumidamente, cada mensagem é composta por:

```
┌─────────────────┬──────────────────────────────┐
│  Header 4 bytes │       Payload JSON UTF-8     │
│  (tamanho N)    │           (N bytes)          │
└─────────────────┴──────────────────────────────┘
```

## Usuários

O sistema utiliza autenticação estática. Os usuários disponíveis estão definidos no servidor:

| Usuário  | Senha        |
|----------|--------------|
| `joao`   | (ver código) |
| `marcelo`| (ver código) |
| `rubens` | (ver código) |

## Requisitos

- **Java 21** (LTS)
- **Maven 3.8+**
- **OpenSSL** — necessário para geração e exportação do certificado TLS

Verifique sua versão antes de rodar:

```bash
java -version
mvn -version
openssl version
```

## Estrutura do Projeto

```
raiz/
├── PROTOCOL.md                        # Especificação do protocolo de comunicação
├── README.md
├── pom.xml                            # POM raiz (multi-módulo)
├── comum/
│   ├── pom.xml
│   └── src/main/java/chat/
│       └── protocolo/
│           └── Message.java           # Leitura e escrita do protocolo (compartilhado)
├── servidor/
│   ├── pom.xml
│   ├── servidor.keystore              # Chave privada TLS — NÃO commitar (ver .gitignore)
│   └── src/main/java/chat/
│       ├── Server.java                # Ponto de entrada do servidor
│       ├── ClientHandler.java         # Gerencia o ciclo de vida de cada cliente
│       └── auth/
│           └── Authenticator.java     # Validação de usuários e controle de sessões
└── cliente/
    ├── pom.xml
    └── src/main/
        ├── java/chat/
        │   └── Client.java            # Ponto de entrada do cliente
        └── resources/
            └── servidor.crt           # Certificado público TLS do servidor
```

## Segurança
A comunicação é inteiramente criptografada com TLS 1.3. O servidor se identifica com um certificado autoassinado gerado localmente.

- `servidor.keystore` — contém a chave privada do servidor.
- `servidor.crt` — certificado público exportado do keystore.

O `.gitignore` já está configurado para ignorar o keystore:

```
servidor/servidor.keystore
```

## Como Executar

### 1. Gerar o certificado TLS (apenas na primeira vez)

Execute dentro da pasta `servidor/`:

```bash
keytool -genkeypair \
  -alias servidor \
  -keyalg RSA \
  -keysize 2048 \
  -validity 365 \
  -keystore servidor.keystore \
  -storepass senhaKeystore \
  -keypass senhaKeystore \
  -dname "CN=localhost, OU=Chat, O=Chat, L=Brasil, ST=Brasil, C=BR"
```

Depois exporte o certificado público para o módulo do cliente:

```bash
keytool -exportcert \
  -alias servidor \
  -keystore servidor.keystore \
  -storepass senhaKeystore \
  -file ../cliente/src/main/resources/servidor.crt \
  -rfc
```

### 2. Compilar tudo

Na raiz do projeto:

```bash
mvn install
```

### 3. Iniciar o Servidor

O servidor precisa estar rodando antes de qualquer cliente conectar.

```bash
cd servidor
mvn exec:java -Dexec.mainClass="chat.Server"
```

Saída esperada:

```
Servidor aguardando conexões na porta 12345 (TLS)...
```

### 4. Iniciar o Cliente

Em um terminal separado:

```bash
cd cliente
mvn exec:java -Dexec.mainClass="chat.Client"
```

Para simular múltiplos usuários, abra terminais adicionais e repita o comando.



> O contrato de comunicação entre os dois módulos está inteiramente descrito no `PROTOCOL.md`. Qualquer alteração no protocolo deve ser acordada entre os dois antes de ser implementada.