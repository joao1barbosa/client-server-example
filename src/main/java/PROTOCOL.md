# Protocolo de Comunicação — Chat Multiusuário

**Versão:** 1.0  
**Data:** 2026-03-01

---

## 1. Visão Geral

Este documento define o protocolo de comunicação entre cliente e servidor da aplicação de chat multiusuário. Todo intercâmbio de dados segue o mesmo padrão: um cabeçalho binário de tamanho fixo seguido de um payload JSON em UTF-8. A camada de transporte é TCP com TLS obrigatório.

---

## 2. Camada de Transporte

| Parâmetro | Valor |
|---|---|
| Protocolo | TCP |
| Segurança | TLS 1.2 ou superior (obrigatório) |
| Porta padrão | `12345` |
| Encoding do payload | UTF-8 |

---

## 3. Formato das Mensagens

Toda mensagem — em ambas as direções — obedece à seguinte estrutura:

```
┌──────────────────────────┬─────────────────────────────────────────┐
│      HEADER (4 bytes)    │           PAYLOAD (N bytes)             │
│  Inteiro big-endian = N  │         Objeto JSON em UTF-8            │
└──────────────────────────┴─────────────────────────────────────────┘
```

### 3.1 Cabeçalho

- **Tamanho:** 4 bytes fixos
- **Tipo:** inteiro sem sinal, big-endian (padrão de rede)
- **Valor:** número de bytes do payload que vem a seguir

### 3.2 Payload

- **Formato:** objeto JSON válido
- **Encoding:** UTF-8, sem BOM
- **Campo obrigatório:** `"type"` — identifica o tipo da mensagem
- **Tamanho máximo:** 65.535 bytes

### 3.3 Pseudocódigo de leitura

```
// Leitura
headerBytes = read(4)
payloadSize = toInt(headerBytes)          // big-endian
payloadBytes = read(payloadSize)          // lê exatamente N bytes
message = JSON.parse(payloadBytes)

// Escrita
payloadBytes = JSON.serialize(message).toUTF8()
headerBytes = toBytes(payloadBytes.length) // big-endian, 4 bytes
write(headerBytes + payloadBytes)
```

---

## 4. Tipos de Mensagem

### 4.1 Tabela de tipos

| `type` | Direção | Descrição |
|---|---|---|
| `LOGIN` | Cliente → Servidor | Solicitação de autenticação |
| `LOGIN_OK` | Servidor → Cliente | Login aceito |
| `LOGIN_FAIL` | Servidor → Cliente | Login recusado |
| `CHAT` | Cliente → Servidor | Envio de mensagem ao grupo |
| `BROADCAST` | Servidor → Cliente | Mensagem de outro usuário |
| `USER_JOINED` | Servidor → Cliente | Aviso de novo usuário conectado |
| `USER_LEFT` | Servidor → Cliente | Aviso de usuário desconectado |

---

## 5. Definição de Cada Mensagem

### `LOGIN` — Cliente → Servidor

Primeira mensagem obrigatória após estabelecer conexão. O servidor **ignora** qualquer outra mensagem recebida antes de concluir o login.

```json
{
  "type": "LOGIN",
  "username": "joao",
  "password": "senha"
}
```

| Campo      | Tipo | Obrigatório | Descrição            |
|------------|---|---|----------------------|
| `type`     | string | sim | Valor fixo `"LOGIN"` |
| `username` | string | sim | Nome do usuário      |
| `senha`    | string | sim | Senha do usuário     |


---

### `LOGIN_OK` — Servidor → Cliente

Enviado quando o login é aceito. O cliente pode enviar mensagens `CHAT` a partir deste momento.

```json
{
  "type": "LOGIN_OK",
  "online": ["marcelo", "rubens"]
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `type` | string | sim | Valor fixo `"LOGIN_OK"` |
| `online` | array de strings | sim | Lista de usuários já conectados (excluindo o próprio) |

---

### `LOGIN_FAIL` — Servidor → Cliente

Enviado quando o login é recusado. A conexão é encerrada pelo servidor imediatamente após.

```json
{
  "type": "LOGIN_FAIL",
  "reason": "INVALID_USER_OR_PASSWORD"
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `type` | string | sim | Valor fixo `"LOGIN_FAIL"` |
| `reason` | string | sim | Código do motivo (ver tabela abaixo) |

**Códigos de `reason`:**

| Código                     | Significado                          |
|----------------------------|--------------------------------------|
| `INVALID_USER_OR_PASSWORD` | Credênciais Inválidas                |
| `ALREADY_CONNECTED`        | Usuário já está conectado no momento |

---

### `CHAT` — Cliente → Servidor

Mensagem de texto enviada pelo cliente. Só é válida após `LOGIN_OK`.

```json
{
  "type": "CHAT",
  "text": "Olá mundo"
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `type` | string | sim | Valor fixo `"CHAT"` |
| `text` | string | sim | Conteúdo da mensagem (máx. 500 caracteres) |

---

### `BROADCAST` — Servidor → Cliente

Redistribuição de uma mensagem `CHAT` para todos os outros clientes conectados. **O remetente original não recebe esta mensagem.**

```json
{
  "type": "BROADCAST",
  "from": "joao",
  "text": "Oi pessoal",
  "time": "14:32:01"
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `type` | string | sim | Valor fixo `"BROADCAST"` |
| `from` | string | sim | Username do remetente |
| `text` | string | sim | Conteúdo da mensagem |
| `time` | string | sim | Horário de recebimento no servidor (`HH:mm:ss`) |

---

### `USER_JOINED` — Servidor → Cliente

Enviado a todos os clientes já conectados quando um novo usuário conclui o login.

```json
{
  "type": "USER_JOINED",
  "username": "joao",
  "online": ["joao", "marcelo", "rubens"]
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `type` | string | sim | Valor fixo `"USER_JOINED"` |
| `username` | string | sim | Nome do usuário que acabou de entrar |
| `online` | array de strings | sim | Lista completa de conectados após a entrada |

---

### `USER_LEFT` — Servidor → Cliente

Enviado a todos os clientes quando um usuário se desconecta (saída voluntária ou queda de conexão).

```json
{
  "type": "USER_LEFT",
  "username": "joao",
  "online": ["marcelo", "rubens"]
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `type` | string | sim | Valor fixo `"USER_LEFT"` |
| `username` | string | sim | Nome do usuário que saiu |
| `online` | array de strings | sim | Lista de conectados após a saída |

---

## 6. Fluxo de Comunicação

### 6.1 Conexão e login bem-sucedido

```
Cliente                          Servidor
   |                                |
   |──── (TLS Handshake) ─────────►|
   |◄─── (TLS Handshake) ──────────|
   |                                |
   |──── LOGIN {"username":"joao"} ►|  verifica lista predefinida
   |◄─── LOGIN_OK {"online":[...]} ─|
   |                                |
   |                          ──────┼──► USER_JOINED → (todos os outros)
   |                                |
   |  (sessão ativa)                |
```

### 6.2 Troca de mensagens

```
Cliente A                        Servidor                    Cliente B
   |                                |                            |
   |──── CHAT {"text":"Oi"} ───────►|                            |
   |                                |──── BROADCAST ────────────►|
   |                                |     {"from":"joao",...}    |
   |   (não recebe o BROADCAST)     |                            |
```

### 6.3 Desconexão

```
Cliente                          Servidor
   |                                |
   |  (fecha conexão TCP)           |
   |                                |──► USER_LEFT → (todos os outros)
   |                                |    remove da lista de conectados
```

---

## 7. Regras e Restrições

1. **Autenticação obrigatória.** O servidor descarta silenciosamente qualquer mensagem que não seja `LOGIN` enquanto o cliente não estiver autenticado.
2. **Um login por usuário.** Se `username` já estiver na lista de conectados, o servidor responde com `LOGIN_FAIL` + `"reason": "ALREADY_CONNECTED"` e encerra a conexão.
3. **Usuários predefinidos.** Apenas os usernames cadastrados no servidor são aceitos (`joao`, `marcelo`, `rubens`). Qualquer outro resulta em `LOGIN_FAIL` + `"reason": "INVALID_USER"`.
4. **Remetente não recebe broadcast.** O servidor nunca envia `BROADCAST` para o mesmo cliente que originou o `CHAT`.
5. **TLS obrigatório.** Conexões sem TLS devem ser recusadas pelo servidor.
6. **Big-endian no cabeçalho.** Os 4 bytes do header são sempre interpretados como inteiro big-endian.
7. **Limite de mensagem.** Payloads com mais de 65.535 bytes devem ser rejeitados com encerramento da conexão.

---

## 8. Usuários Predefinidos

| Username  |
|-----------|
| `joao`    |
| `marcelo` |
| `rubens`  |