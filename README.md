# 🏗️ Arquitetura do Sistema Distribuído

## Simulação de Fábrica, Lojas e Clientes (Java + Sockets)

---

# 📌 Visão Geral

O sistema simula uma cadeia de produção e comercialização de veículos distribuída em dois programas principais:

* 🏭 **FÁBRICA** → Servidor (responsável pela produção)
* 🏪 **LOJAS** → Clientes (responsáveis pela venda)

A comunicação será feita via **Sockets TCP**, seguindo o modelo **cliente-servidor**.

---

# 🧭 Arquitetura Geral

```
              ┌────────────────────┐
              │      LOJA 1        │
              └────────┬───────────┘
                       │
              ┌────────▼───────────┐
              │                    │
              │      FÁBRICA       │
              │     (SERVER)       │
              │                    │
              └────────▲───────────┘
                       │
              ┌────────┴───────────┐
              │      LOJA 2        │
              └────────────────────┘
```

* A fábrica fica **sempre ativa**, aguardando conexões.
* As lojas conectam e fazem requisições sob demanda.

---

# 🏭 FÁBRICA (Servidor)

## 📌 Responsabilidades

* Produzir veículos
* Gerenciar estoque de peças
* Controlar esteiras
* Atender requisições das lojas
* Enviar veículos via socket
* Gerar logs

---

## 🔧 Componentes Internos

### 1. Controle de Peças

* Capacidade máxima: **500 peças**
* Controle via **Semáforo**

---

### 2. Estações de Produção

* 4 estações
* Cada estação possui:

  * 5 funcionários
  * Estrutura circular (problema dos filósofos)

#### ⚠️ Regra:

Um funcionário precisa de **2 ferramentas (esquerda + direita)** para produzir.

---

### 3. Esteira de Produção

* Buffer circular
* Capacidade: **40 veículos**
* Controle com semáforos:

  * `empty` (espaço disponível)
  * `full` (itens disponíveis)
  * `mutex` (exclusão mútua)

---

### 4. Servidor Socket

* Porta definida (ex: `12345`)
* Aceita múltiplas conexões
* Cada loja = 1 thread

---

## 🔁 Fluxo da Fábrica

```
[Produção] → [Esteira] → [Requisição Loja] → [Envio via Socket]
```

1. Funcionário produz veículo
2. Veículo entra na esteira
3. Loja solicita veículo
4. Fábrica remove da esteira
5. Envia dados via socket

---

## 📦 Estrutura de Dados

```java
class Veiculo {
    int id;
    String cor;        // RED, GREEN, BLUE
    String tipo;       // SUV, SEDAN
    int idEstacao;
    int idFuncionario;
}
```

---

## 🧾 Logs da Fábrica

### Log de Produção

* ID veículo
* Cor
* Tipo
* Estação
* Funcionário
* Posição na esteira

---

### Log de Venda

* Todos os dados acima +
* ID da loja
* Posição na esteira da loja

---

# 🏪 LOJAS (Cliente)

## 📌 Responsabilidades

* Conectar na fábrica
* Solicitar veículos
* Armazenar veículos
* Atender clientes
* Gerar logs

---

## 🔧 Componentes Internos

### 1. Cliente Socket

* Conecta na fábrica
* Envia requisições
* Recebe veículos

---

### 2. Esteira da Loja

* Buffer circular próprio
* Controle com semáforos

---

### 3. Clientes (Threads)

* Total: **20 threads**
* Cada cliente:

  * Escolhe loja aleatoriamente
  * Compra múltiplos veículos

---

## 🔁 Fluxo da Loja

```
[Cliente] → [Loja] → [Fábrica] → [Loja] → [Cliente]
```

1. Cliente solicita compra
2. Loja verifica estoque
3. Se vazio → solicita à fábrica
4. Recebe veículo
5. Armazena na esteira
6. Entrega ao cliente

---

## 🧾 Logs da Loja

### Log de Recebimento

* Dados completos do veículo
* Origem da fábrica

---

### Log de Venda

* Dados do veículo
* ID do cliente

---

# 🔌 Protocolo de Comunicação (ESSENCIAL)

## 📡 Padrão: JSON

---

## 📤 Loja → Fábrica

```json
{
  "action": "REQUEST_VEHICLE"
}
```

---

## 📥 Fábrica → Loja

### Sucesso

```json
{
  "status": "OK",
  "veiculo": {
    "id": 1,
    "cor": "RED",
    "tipo": "SUV",
    "idEstacao": 2,
    "idFuncionario": 4
  }
}
```

---

### Sem estoque

```json
{
  "status": "WAIT"
}
```

---

# 🔒 Controle de Concorrência

## ✔️ Obrigatório: usar Semáforos

---

## 🏭 Fábrica

* Controle de peças
* Controle da esteira
* Controle das ferramentas (filósofos)

---

## 🏪 Loja

* Controle da esteira
* Controle de acesso dos clientes

---

# ⚠️ Problemas Clássicos

## 🍽️ Jantar dos Filósofos (Adaptado)

Solução recomendada:

* Sempre pegar ferramenta **menor ID primeiro**
* Evita deadlock

---

## 🚫 Evitar

* Deadlock
* Starvation
* Race conditions

---

# 🤝 Contrato entre Desenvolvedores

## 🔑 Definir antes de integrar:

* Porta (ex: 12345)
* Estrutura JSON
* Nome das ações:

  * `REQUEST_VEHICLE`
  * `OK`
  * `WAIT`
* Estrutura da classe `Veiculo`

---

# 🚀 Etapas de Desenvolvimento

## 🏭 FÁBRICA

1. Implementar produção (threads)
2. Implementar semáforos
3. Implementar esteira
4. Implementar socket server
5. Integrar tudo

---

## 🏪 LOJA

1. Implementar cliente socket
2. Implementar esteira
3. Implementar clientes (threads)
4. Integrar tudo

---

# 🧪 Estratégia de Teste

## ✔️ Etapa 1 (Mock)

* Fábrica envia veículo fixo
* Loja recebe e imprime

---

## ✔️ Etapa 2

* Testar múltiplas lojas

---

## ✔️ Etapa 3

* Integrar clientes (threads)

---

# 🔥 Boas Práticas

* Usar logs detalhados
* Validar dados recebidos
* Tratar exceções de socket
* Separar bem responsabilidades (SRP)

---

# 📌 Resumo Final

* Arquitetura distribuída
* Comunicação via socket
* Concorrência com semáforos
* Problemas clássicos resolvidos
* Integração baseada em contrato

---
