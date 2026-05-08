Loja (Store) - implementação

Arquivos criados (pasta src/loja):

- LojaMain.java — ponto de entrada da loja
- ServerSocketClientes.java — aceita conexões de clientes (Pedido)
- ClientSocketFactory.java — conecta/solicita veículos à fábrica
- EsteiraLoja.java — buffer circular (estoque da loja)
- GerenciadorPedidos.java — coordena pedidos e estoques
- Vehicle.java, Pedido.java, FactoryRequest.java — modelos serializáveis
- LoggerUtil.java — grava logs de recebimento e venda
- MockFactory.java — servidor de fábrica de teste (opcional)
- TesteCliente.java — cliente de teste para comprar veículo

Como compilar e testar (exemplo local):

1) Compile tudo:

```bash
javac -d out src/loja/*.java
```

2) Executar uma fábrica de teste (opcional):

```bash
java -cp out loja.socket.TesteFabrica 9000
```

3) Executar 3 lojas (exemplo):

```bash
java -cp out loja.main.LojaMain Loja1 10001 localhost 9000 5
java -cp out loja.main.LojaMain Loja2 10002 localhost 9000 5
java -cp out loja.main.LojaMain Loja3 10003 localhost 9000 5
```

4) Rodar um cliente de teste que compra um veículo de Loja1:

```bash
java -cp out loja.teste.TesteCliente localhost 10001
```

Logs serão criados como loja-<lojaId>-recebimento.log e loja-<lojaId>-venda.log.

Protocolo resumido:
- Loja <-> Fábrica: objetos Java serializáveis; a loja envia FactoryRequest(lojaId, quantity) e a fábrica responde com objetos Vehicle seguidos por "END" ou "NONE".
- Cliente <-> Store: o cliente envia um Pedido (serializável) e a loja responde com um Vehicle (ou null se não disponível).

Observações:
- Esta implementação usa Java serialization; certifique-se de que fábrica e clientes Java usem as mesmas classes/package.
- Adapte protocolo se a fábrica for implementada em outra linguagem.
