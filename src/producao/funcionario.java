package producao;

import buffer.bufferCircular;
import controle.estoquePecas;
import logs.logProducao;
import model.veiculo;
import java.util.concurrent.Semaphore;

public class funcionario extends Thread {

    private int id;
    private int idEstacao;
    private bufferCircular buffer;
    private estoquePecas estoque;
    private controleFerramentas controle;
    private Semaphore esteiraPecas;

    public funcionario(int id, int idEstacao,
                       bufferCircular buffer,
                       estoquePecas estoque,
                       controleFerramentas controle,
                       Semaphore esteiraPecas) {

        this.id = id;
        this.idEstacao = idEstacao;
        this.buffer = buffer;
        this.estoque = estoque;
        this.controle = controle;
        this.esteiraPecas = esteiraPecas;
    }

    @Override
    public void run() {
        try {
            while (true) {

                ferramentas esquerda = controle.getEsquerda(id);
                ferramentas direita = controle.getDireita(id);

                //EVITAR DEADLOCK
                ferramentas primeira = (esquerda.getId() < direita.getId()) ? esquerda : direita;
                ferramentas segunda = (esquerda.getId() < direita.getId()) ? direita : esquerda;

                // pega ferramentas
                primeira.pegar();
                segunda.pegar();

                // controla acesso à "esteira de peças" (máximo 5 funcionários simultâneos)
                esteiraPecas.acquire();
                try {
                    estoque.usarPeca();
                } finally {
                    esteiraPecas.release();
                }

                veiculo v = new veiculo(idEstacao, id);
                int posicaoEsteira = buffer.produzir(v);
                v.setPosicaoEsteiraProducao(posicaoEsteira);

                logProducao log = new logProducao(
                        v.getId(),
                        v.getCor(),
                        v.getTipo(),
                        v.getIdEstacao(),
                        v.getIdFuncionario(),
                        posicaoEsteira);
                log.salvarJson();

                System.out.println("Funcionario " + id + " | Estação " + idEstacao + " produziu veículo " + v.getId());
                System.out.println();

                // libera ferramentas
                segunda.soltar();
                primeira.soltar();

                sleep(1000);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}