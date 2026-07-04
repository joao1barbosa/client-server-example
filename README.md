# 💬 Multi-User Chat — Client/Server (Java, TLS)

> A real-time multi-user chat over TLS 1.3 TCP sockets, with a custom binary-header + JSON protocol — built in Java as a multi-module client/server.

## 📋 Overview

A client/server chat where the server handles many clients concurrently and broadcasts each
message to everyone else connected. Communication runs over encrypted TCP sockets with a
small, explicit wire protocol.

```
┌──────────┐        TLS/TCP         ┌──────────┐
│  Client  │ ◄────────────────────► │  Server  │
└──────────┘       port 12345       └──────────┘
                                         ▲
                                         │
                                    ┌──────────┐
                                    │  Client  │
                                    └──────────┘
```

## 🚀 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 21 (LTS) |
| **Build** | Maven (multi-module: `comum`, `servidor`, `cliente`) |
| **Transport** | TCP sockets over TLS 1.3 |
| **Payload** | JSON (Gson) |

## 🏗 Architecture & Protocol

Three Maven modules: `comum` (shared protocol), `servidor` (the server), and `cliente`
(the client). The server runs one thread per connected client and keeps a shared, guarded
map of active clients for broadcasting.

Each message is a **4-byte length header + a UTF-8 JSON payload**:

```
┌─────────────────┬──────────────────────────────┐
│  Header 4 bytes │       Payload JSON UTF-8     │
│  (length N)     │           (N bytes)          │
└─────────────────┴──────────────────────────────┘
```

The full contract is specified in [`PROTOCOL.md`](./PROTOCOL.md).

## 🧠 Technical Decisions

- **TLS 1.3 with a self-signed certificate** — the whole channel is encrypted; the server
  identifies itself with a locally generated certificate.
- **Custom binary framing** — a length-prefixed header makes message boundaries explicit
  over a stream socket, instead of relying on delimiters.
- **Thread-per-client with a synchronized client registry** — simple concurrency that keeps
  broadcast and connect/disconnect consistent.

## 🔐 Security

The channel is fully encrypted with TLS 1.3. `servidor.keystore` (private key) is
git-ignored; `servidor.crt` (public certificate) is shipped to the client module.

## 🔧 How to Run Locally

**Prerequisites:** Java 21, Maven 3.8+, OpenSSL.

**1. Generate the TLS certificate (first time only)** — inside `servidor/`:

```bash
keytool -genkeypair -alias servidor -keyalg RSA -keysize 2048 -validity 365 \
  -keystore servidor.keystore -storepass senhaKeystore -keypass senhaKeystore \
  -dname "CN=localhost, OU=Chat, O=Chat, L=Brasil, ST=Brasil, C=BR"

keytool -exportcert -alias servidor -keystore servidor.keystore \
  -storepass senhaKeystore -file ../cliente/src/main/resources/servidor.crt -rfc
```

**2. Build everything** (from the repo root): `mvn install`

**3. Start the server:** `cd servidor && mvn exec:java -Dexec.mainClass="chat.Server"`

**4. Start a client** (in another terminal): `cd cliente && mvn exec:java -Dexec.mainClass="chat.Client"`

Open more terminals to simulate additional users. Authentication is static (demo users
defined in the server).

## 👤 Author

**João Barbosa** — Software Engineer (backend / platform).
[LinkedIn](https://www.linkedin.com/in/joao1barbosa/) · [GitHub](https://github.com/joao1barbosa)
