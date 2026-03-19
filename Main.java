import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int stones = readStartStones(sc);

        GameState state = new GameState(
                stones,
                0, 0,   // p1Points, p2Points
                0, 0,   // p1Taken, p2Taken
                true    // p1Turn starts
        );

        AI ai = new AI(24);

        while (!state.isTerminal()) {

            // NEW: ja paliek mazāk par 2, nav legāla gājiena (drīkst tikai 2 vai 3)
            if (state.stonesLeft < 2) {
                System.out.println("\nAkmeņi uz galda: " + state.stonesLeft);
                System.out.println("Nav iespējams paņemt 2 vai 3. Spēle beigusies!");
                break;
            }

            System.out.println("\nAkmeņi uz galda: " + state.stonesLeft);
            System.out.println("P1: punkti=" + state.p1Points + " paņemti=" + state.p1Taken +
                    " | P2: punkti=" + state.p2Points + " paņemti=" + state.p2Taken);

            if (state.p1Turn) {
                int move = readHumanMove(sc, state.stonesLeft);
                state = state.applyMove(move);
            } else {
                int move = ai.chooseMove(state);
                System.out.println("AI paņem: " + move);
                state = state.applyMove(move);
            }
        }

        int p1Final = state.finalScoreP1();
        int p2Final = state.finalScoreP2();

        System.out.println("\n=== BEIGAS ===");
        System.out.println("P1 gala punkti: " + p1Final);
        System.out.println("P2 gala punkti: " + p2Final);

        if (p1Final > p2Final) System.out.println("Uzvar P1 (cilvēks)");
        else if (p2Final > p1Final) System.out.println("Uzvar P2 (AI)");
        else System.out.println("Neizšķirts");
    }

    private static int readStartStones(Scanner sc) {
        while (true) {
            System.out.print("Ievadi akmeņu skaitu (50-70): ");
            if (!sc.hasNextInt()) {
                sc.next();
                System.out.println("Ievadi veselu skaitli!");
                continue;
            }
            int stones = sc.nextInt();
            if (stones < 50 || stones > 70) {
                System.out.println("Jābūt intervālā 50..70!");
                continue;
            }
            return stones;
        }
    }

    private static int readHumanMove(Scanner sc, int stonesLeft) {
        while (true) {
            System.out.print("Tavs gājiens (2 vai 3): ");
            if (!sc.hasNextInt()) {
                sc.next();
                System.out.println("Ievadi 2 vai 3!");
                continue;
            }
            int move = sc.nextInt();
            if (move != 2 && move != 3) {
                System.out.println("Atļauts tikai 2 vai 3!");
                continue;
            }
            if (move > stonesLeft) {
                System.out.println("Nevar paņemt vairāk nekā ir uz galda!");
                continue;
            }
            return move;
        }
    }
}
