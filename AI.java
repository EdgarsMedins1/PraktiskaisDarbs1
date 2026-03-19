public class AI {

    // Depth limit: drošs, ātrs, un aizstāvams
    // Šai spēlei var arī spēlēt "līdz galam" (depthLimit ~ 40), bet 24 parasti pietiek.
    private final int depthLimit;

    public AI(int depthLimit) {
        this.depthLimit = depthLimit;
    }

    // Choose best move for current player in state (paredzēts, ka tas ir AI gājiens)
    public int chooseMove(GameState state) {
        int bestMove = -1;
        int bestValue = Integer.MIN_VALUE;

        for (int move : possibleMoves(state.stonesLeft)) {
            GameState next = state.applyMove(move);
            int value = alphabeta(next, depthLimit - 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }
        return bestMove;
    }

    // Alpha-beta on value = (P2 - P1) perspective
    private int alphabeta(GameState state, int depth, int alpha, int beta) {
        if (state.isTerminal()) {
            return terminalValue(state);
        }
        if (depth == 0) {
            return heuristicValue(state);
        }

        boolean maximizing = !state.p1Turn; // if it's P2 turn -> maximize, else minimize

        if (maximizing) {
            int value = Integer.MIN_VALUE;
            for (int move : possibleMoves(state.stonesLeft)) {
                GameState next = state.applyMove(move);
                value = Math.max(value, alphabeta(next, depth - 1, alpha, beta));
                alpha = Math.max(alpha, value);
                if (alpha >= beta) break; // prune
            }
            return value;
        } else {
            int value = Integer.MAX_VALUE;
            for (int move : possibleMoves(state.stonesLeft)) {
                GameState next = state.applyMove(move);
                value = Math.min(value, alphabeta(next, depth - 1, alpha, beta));
                beta = Math.min(beta, value);
                if (alpha >= beta) break; // prune
            }
            return value;
        }
    }

    private int terminalValue(GameState s) {
        // final = points + taken stones
        return s.finalScoreP2() - s.finalScoreP1();
    }

    private int heuristicValue(GameState s) {
        // Viegls, aizstāvams novērtējums:
        // - pašreizējie punkti + savāktie (jo tie arī ir vērtība)
        // - vērtējam no P2 perspektīvas
        int p1 = s.p1Points + s.p1Taken;
        int p2 = s.p2Points + s.p2Taken;

        // neliels bonuss par kontroli / gājiena kārtu (nav obligāts, bet palīdz)
        int turnBias = s.p1Turn ? -1 : 1;

        return (p2 - p1) * 10 + turnBias;
    }

    private int[] possibleMoves(int stonesLeft) {
        if (stonesLeft >= 3) return new int[]{2, 3};
        // ja palikuši tikai 2 (vai 1, kas īsti nevar sanākt, bet drošībai)
        if (stonesLeft == 2) return new int[]{2};
        return new int[]{}; // terminal
    }
}