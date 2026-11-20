import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

// Feito por Kaiky Pereira

public class Main {

    static char[][] labirinto;
    static int linhas, colunas;

    // Movimentos: cima, baixo, esquerda, direita
    static int[] moveLinha = {-1, 1, 0, 0};
    static int[] moveColuna = {0, 0, -1, 1};

    static boolean[][] visitado;
    static int[][] caminhoAtual = new int[100][2];
    static int[][] melhorCaminho = new int[100][2];
    static int tamanhoMelhor; // 0 = ainda nao achou caminho

    // Caminho ate o tesouro escolhido pelo guloso
    static int[][] trecho = new int[100][2];
    static int tamanhoTrecho;

    // Texto do caminho percorrido
    static String texto;

    // Verifica se a posição esta dentro do labirinto
    public static boolean dentroDosLimites(int linha, int coluna) {
        return linha >= 0 && linha < linhas && coluna >= 0 && coluna < colunas;
    }

    // Transforma as linhas de texto em matriz
    public static char[][] montar(String[] desenho) {
        char[][] matriz = new char[desenho.length][];
        for (int i = 0; i < desenho.length; i++) {
            matriz[i] = desenho[i].toCharArray();
        }
        return matriz;
    }

    // Mostra o labirinto na tela
    public static void exibirLabirinto(char[][] lab) {
        for (int i = 0; i < lab.length; i++) {
            for (int j = 0; j < lab[i].length; j++) {
                System.out.print(lab[i][j] + " ");
            }
            System.out.println();
        }
    }

    // Procura uma letra no labirinto (S ou E)
    public static int[] localizar(char letra) {
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                if (labirinto[i][j] == letra) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    // RECURSIVIDADE: tenta todos os caminhos ate o destino e guarda o menor
    public static void buscar(int linha, int coluna, int destLinha, int destColuna, int tamanho) {

        // Fora do labirinto, parede ou casa ja visitada: volta
        if (!dentroDosLimites(linha, coluna)) return;
        if (labirinto[linha][coluna] == '#') return;
        if (visitado[linha][coluna]) return;

        boolean ehDestino = (linha == destLinha && coluna == destColuna);

        // A saida so conta se ela for o destino
        if (labirinto[linha][coluna] == 'E' && !ehDestino) return;

        // Se ja nao da para ser menor que o melhor, nem continua
        if (tamanhoMelhor > 0 && tamanho + 1 >= tamanhoMelhor) return;

        visitado[linha][coluna] = true;
        caminhoAtual[tamanho][0] = linha;
        caminhoAtual[tamanho][1] = coluna;
        tamanho++;

        if (ehDestino) {
            // Chegou: guarda o caminho
            for (int i = 0; i < tamanho; i++) {
                melhorCaminho[i][0] = caminhoAtual[i][0];
                melhorCaminho[i][1] = caminhoAtual[i][1];
            }
            tamanhoMelhor = tamanho;
        } else {
            // Chama de novo para cima, baixo, esquerda e direita
            for (int d = 0; d < 4; d++) {
                buscar(linha + moveLinha[d], coluna + moveColuna[d], destLinha, destColuna, tamanho);
            }
        }

        // Desmarca a casa para outros caminhos poderem usar (backtracking)
        visitado[linha][coluna] = false;
    }

    // Acha o menor caminho entre dois pontos. Retorna false se nao existir
    public static boolean menorCaminho(int origemLinha, int origemColuna, int destLinha, int destColuna) {
        visitado = new boolean[linhas][colunas];
        tamanhoMelhor = 0;
        buscar(origemLinha, origemColuna, destLinha, destColuna, 0);
        return tamanhoMelhor > 0;
    }

    // GULOSO: escolhe o tesouro mais perto de onde o heroi esta
    public static boolean escolherTesouro(int atualLinha, int atualColuna, int saidaLinha, int saidaColuna) {
        tamanhoTrecho = 0;

        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {

                if (labirinto[i][j] != 'T') continue;

                // So vale se depois do tesouro ainda da para sair
                if (!menorCaminho(i, j, saidaLinha, saidaColuna)) continue;

                // Distancia ate o tesouro
                if (!menorCaminho(atualLinha, atualColuna, i, j)) continue;

                // Fica com o mais perto (no empate fica o primeiro)
                if (tamanhoTrecho == 0 || tamanhoMelhor < tamanhoTrecho) {
                    for (int k = 0; k < tamanhoMelhor; k++) {
                        trecho[k][0] = melhorCaminho[k][0];
                        trecho[k][1] = melhorCaminho[k][1];
                    }
                    tamanhoTrecho = tamanhoMelhor;
                }
            }
        }

        return tamanhoTrecho > 0;
    }

    // Anda pelo caminho, escreve no texto e conta os tesouros coletados
    public static int percorrer(int[][] caminho, int tamanho) {
        int coletados = 0;

        // Comeca no 1 porque o 0 e onde o heroi ja esta
        for (int i = 1; i < tamanho; i++) {
            int linha = caminho[i][0];
            int coluna = caminho[i][1];

            if (labirinto[linha][coluna] == 'E') {
                texto = texto + " → E";
            } else {
                texto = texto + " → (" + linha + "," + coluna + ")";

                if (labirinto[linha][coluna] == 'T') {
                    texto = texto + " → T";
                    labirinto[linha][coluna] = '.'; // tesouro coletado
                    coletados++;
                }
            }
        }
        return coletados;
    }

    // Funcao principal da missao
    public static void acharCaminho(char[][] lab) {

        // Copia para nao estragar o labirinto original
        linhas = lab.length;
        colunas = lab[0].length;
        labirinto = new char[linhas][colunas];
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                labirinto[i][j] = lab[i][j];
            }
        }

        int[] inicio = localizar('S');
        int[] saida = localizar('E');

        // Primeiro ve se existe caminho ate a saida
        if (inicio == null || saida == null || !menorCaminho(inicio[0], inicio[1], saida[0], saida[1])) {
            System.out.println("Não há saída possível!");
            return;
        }

        texto = "S";
        int atualLinha = inicio[0];
        int atualColuna = inicio[1];
        int tesouros = 0;

        // Enquanto tiver tesouro, vai no mais perto
        while (escolherTesouro(atualLinha, atualColuna, saida[0], saida[1])) {
            tesouros += percorrer(trecho, tamanhoTrecho);
            atualLinha = trecho[tamanhoTrecho - 1][0];
            atualColuna = trecho[tamanhoTrecho - 1][1];
        }

        // Acabaram os tesouros: vai para a saida
        menorCaminho(atualLinha, atualColuna, saida[0], saida[1]);
        tesouros += percorrer(melhorCaminho, tamanhoMelhor);

        System.out.println("Caminho percorrido: " + texto);
        System.out.println("Tesouros coletados: " + tesouros);
    }

    public static void main(String[] args) {

        // Para aparecer a seta e os acentos no console
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        // Caso 1: caminho direto, sem tesouro
        String[] desenho1 = {
            "S..#..",
            "##.#.#",
            "....#.",
            "###.#.",
            "###..E",
            "######"
        };

        // Caso 2: dois tesouros
        String[] desenho2 = {
            "S..#..",
            "##.#.#",
            "....T.",
            ".###.#",
            "..T..#",
            ".#####",
            "E#####"
        };

        // Caso 3: saida cercada por paredes
        String[] desenho3 = {
            "S..#..",
            "##.#.#",
            "....T.",
            ".###.#",
            "..T.#E",
            "######"
        };

        // Labirinto do enunciado (o mesmo do teste de mesa)
        String[] desenho4 = {
            "S..#..",
            "##.#.#",
            "....T.",
            ".###.#",
            "..T..E",
            "######"
        };

        System.out.println("Caso 1");
        acharCaminho(montar(desenho1));

        System.out.println("\nCaso 2");
        acharCaminho(montar(desenho2));

        System.out.println("\nCaso 3");
        acharCaminho(montar(desenho3));

        System.out.println("\nLabirinto do enunciado");
        exibirLabirinto(montar(desenho4));
        acharCaminho(montar(desenho4));
    }
}
