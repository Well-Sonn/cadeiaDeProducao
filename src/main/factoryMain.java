package main;

import buffer.bufferCircular;
import controle.estoquePecas;
import producao.controleFerramentas;
import producao.funcionario;
import java.util.concurrent.Semaphore;

public class factoryMain {

    public static void main(String[] args) {

        bufferCircular esteira = new bufferCircular(40);
        estoquePecas estoque = new estoquePecas(500);
        Semaphore esteiraPecas = new Semaphore(5);

        for (int estacao = 1; estacao <= 4; estacao++) {

            controleFerramentas controle = new controleFerramentas(5);

            for (int func = 0; func < 5; func++) {
                new funcionario(func, estacao, esteira, estoque, controle, esteiraPecas).start();
            }
        }
    }
}