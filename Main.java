import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("=== AKMENTIŅU SPĒLE ===");
        System.out.println("1) Spēlēt pret AI");
        System.out.println("2) Izpildīt 10 eksperimentus (Minimax vs Alpha-Beta)");
        System.out.print("Izvēle: ");

        int choice = sc.hasNextInt() ? sc.nextInt() : 1;

        if (choice == 2) {
            runExperiments(sc);
        } else {
            playSingleGame(sc);
        }
    }

    // ── Single game ────────────────────────────────────────────────────────────
    private static void playSingleGame(Scanner sc) {
        System.out.println("\nAlgoritms: 1) Minimax  2) Alpha-Beta");
        System.out.print("Izvēle: ");
        int algChoice = sc.hasNextInt() ? sc.nextInt() : 2;
        AI.Algorithm algo = (algChoice == 1) ? AI.Algorithm.MINIMAX : AI.Algorithm.ALPHABETA;

        int stones = readStartStones(sc);
        GameState state = new GameState(stones, 0, 0, 0, 0, true);
        AI ai = new AI(24, algo);

        System.out.println("Algoritms: " + ai.getAlgorithmName());

        while (!state.isTerminal()) {
            if (state.stonesLeft < 2) {
                System.out.println("\nNav legālu gājienu. Spēle beigusies.");
                break;
            }

            System.out.println("\nAkmeņi: " + state.stonesLeft +
                    " | P1: " + state.p1Points + "pts +" + state.p1Taken + "ak" +
                    " | P2: " + state.p2Points + "pts +" + state.p2Taken + "ak");

            if (state.p1Turn) {
                int move = readHumanMove(sc, state.stonesLeft);
                state = state.applyMove(move);
            } else {
                int move = ai.chooseMove(state);
                System.out.printf("AI paņem: %d  [ģen: %,d, nov: %,d, laiks: %s]%n",
                        move, ai.lastGeneratedNodes, ai.lastEvaluatedNodes, formatTime(ai.lastMoveTimeNs));
                state = state.applyMove(move);
            }
        }

        printResult(state);
    }

    // ── 10 experiments for each algorithm ─────────────────────────────────────
    private static void runExperiments(Scanner sc) {
        int[] stoneCounts = {50, 52, 54, 56, 58, 60, 62, 64, 66, 68};

        System.out.println("\n=== 10 EKSPERIMENTI: MINIMAX ===");
        runExperimentSet(stoneCounts, AI.Algorithm.MINIMAX);

        System.out.println("\n=== 10 EKSPERIMENTI: ALPHA-BETA ===");
        runExperimentSet(stoneCounts, AI.Algorithm.ALPHABETA);
    }

    private static void runExperimentSet(int[] stoneCounts, AI.Algorithm algo) {
        int humanWins = 0, aiWins = 0, draws = 0;
        long totalNodes = 0, totalEvaluated = 0, totalTime = 0;
        int totalMoves = 0;

        System.out.printf("%-4s %-7s %-10s %-14s %-14s %-10s %-12s %-12s%n",
                "Exp", "Akmeņi", "Uzvarētājs", "Ģen.virsotnes", "Nov.virsotnes",
                "Gājieni", "Kop.laiks", "Vid.laiks");
        System.out.println("-".repeat(80));

        for (int i = 0; i < stoneCounts.length; i++) {
            int stones = stoneCounts[i];
            GameState state = new GameState(stones, 0, 0, 0, 0, true);
            AI ai = new AI(24, algo);

            long expNodes = 0, expEval = 0, expTime = 0;
            int moves = 0;

            // Simulate: P1 = random (always takes 2 for repeatability), P2 = AI
            // For experiments we simulate P1 as a simple heuristic: take 3 if possible
            while (!state.isTerminal() && state.stonesLeft >= 2) {
                if (state.p1Turn) {
                    // Simulate human: alternating 2/3 for variety
                    int humanMove = (moves % 2 == 0 && state.stonesLeft >= 3) ? 3 : 2;
                    if (humanMove > state.stonesLeft) humanMove = 2;
                    state = state.applyMove(humanMove);
                } else {
                    int aiMove = ai.chooseMove(state);
                    expNodes += ai.lastGeneratedNodes;
                    expEval  += ai.lastEvaluatedNodes;
                    expTime  += ai.lastMoveTimeNs;
                    moves++;
                    state = state.applyMove(aiMove);
                }
            }

            int p1 = state.finalScoreP1();
            int p2 = state.finalScoreP2();
            String winner;
            if (p1 > p2) { winner = "Cilvēks"; humanWins++; }
            else if (p2 > p1) { winner = "Dators"; aiWins++; }
            else { winner = "Neizšķirts"; draws++; }

            totalNodes    += expNodes;
            totalEvaluated+= expEval;
            totalTime     += expTime;
            totalMoves    += moves;

            long avgT = moves > 0 ? expTime / moves : 0;

            System.out.printf("%-4d %-7d %-10s %-14d %-14d %-10d %-12s %-12s%n",
                    i + 1, stones, winner, expNodes, expEval, moves,
                    formatTime(expTime), formatTime(avgT));
        }

        System.out.println("-".repeat(80));
        long avgNodes = totalMoves > 0 ? totalNodes / totalMoves : 0;
        long avgEval  = totalMoves > 0 ? totalEvaluated / totalMoves : 0;
        long avgTimeNs= totalMoves > 0 ? totalTime / totalMoves : 0;

        System.out.printf("KOPSAVILKUMS [%s]:%n", algo == AI.Algorithm.MINIMAX ? "Minimax" : "Alpha-Beta");
        System.out.printf("  Cilvēks uzvarēja: %d  |  Dators uzvarēja: %d  |  Neizšķirts: %d%n",
                humanWins, aiWins, draws);
        System.out.printf("  Vid. ģen. virsotnes/gājienā: %,d%n", avgNodes);
        System.out.printf("  Vid. nov. virsotnes/gājienā: %,d%n", avgEval);
        System.out.printf("  Vid. laiks/gājienā: %s%n", formatTime(avgTimeNs));
        System.out.println();
    }

    private static void printResult(GameState state) {
        int p1 = state.finalScoreP1();
        int p2 = state.finalScoreP2();
        System.out.println("\n=== BEIGAS ===");
        System.out.println("P1 (cilvēks): " + p1);
        System.out.println("P2 (AI):      " + p2);
        if (p1 > p2) System.out.println("Uzvar P1 (cilvēks)!");
        else if (p2 > p1) System.out.println("Uzvar P2 (AI)!");
        else System.out.println("Neizšķirts.");
    }

    private static String formatTime(long ns) {
        if (ns < 1_000_000L) {
            return String.format("%.1f µs", ns / 1_000.0);
        } else {
            return String.format("%.2f ms", ns / 1_000_000.0);
        }
    }

    private static int readStartStones(Scanner sc) {
        while (true) {
            System.out.print("Ievadi akmeņu skaitu (50-70): ");
            if (!sc.hasNextInt()) { sc.next(); System.out.println("Vesels skaitlis!"); continue; }
            int s = sc.nextInt();
            if (s < 50 || s > 70) { System.out.println("Jābūt 50..70!"); continue; }
            return s;
        }
    }

    private static int readHumanMove(Scanner sc, int stonesLeft) {
        while (true) {
            System.out.print("Tavs gājiens (2 vai 3): ");
            if (!sc.hasNextInt()) { sc.next(); System.out.println("Ievadi 2 vai 3!"); continue; }
            int m = sc.nextInt();
            if (m != 2 && m != 3) { System.out.println("Tikai 2 vai 3!"); continue; }
            if (m > stonesLeft) { System.out.println("Nav tik daudz akmeņu!"); continue; }
            return m;
        }
    }
}
