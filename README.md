# Chat Multiusuário — Cliente/Servidor

Sistema de chat em tempo real baseado em arquitetura cliente-servidor, desenvolvido em Java. A comunicação utiliza sockets TCP com TLS, protocolo próprio com cabeçalho binário e payload JSON.

## Visão Geral

```
┌──────────┐        TLS/TCP         ┌──────────┐
│ Cliente  │ ◄───────────────────► │ Servidor │
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

Verifique sua versão antes de rodar:

```bash
java -version
mvn -version
```

## Estrutura do Projeto

```
chat/
├── PROTOCOL.md              # Especificação do protocolo de comunicação
├── README.md
├── pom.xml                  # POM raiz (multi-módulo)
├── servidor/
│   ├── pom.xml
│   └── src/main/java/chat/
│       └── Server.java
└── cliente/
    ├── pom.xml
    └── src/main/java/chat/
        └── Client.java
```

## Como Executar

### 1. Compilar tudo

Na raiz do projeto:

```bash
mvn compile
```

### 2. Iniciar o Servidor

O servidor precisa estar rodando antes de qualquer cliente conectar.

```bash
cd servidor
mvn exec:java -Dexec.mainClass="chat.Server"
```

Saída esperada:
```
= Servidor Iniciado =
- Aguardando Conexões -
```

### 3. Iniciar o Cliente

Em um terminal separado:

```bash
cd cliente
mvn exec:java -Dexec.mainClass="chat.Client"
```

Para simular múltiplos usuários, abra terminais adicionais e repita o comando.



> O contrato de comunicação entre os dois módulos está inteiramente descrito no `PROTOCOL.md`. Qualquer alteração no protocolo deve ser acordada entre os dois antes de ser implementada.