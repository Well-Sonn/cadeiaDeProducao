package producao;

public class controleFerramentas {

    private ferramentas[] ferramentas;

    public controleFerramentas(int quantidade) {
        ferramentas = new ferramentas[quantidade];

        for (int i = 0; i < quantidade; i++) {
            ferramentas[i] = new ferramentas(i);
        }
    }

    public ferramentas getEsquerda(int idFuncionario) {
        return ferramentas[idFuncionario];
    }

    public ferramentas getDireita(int idFuncionario) {
        return ferramentas[(idFuncionario + 1) % ferramentas.length];
    }
}