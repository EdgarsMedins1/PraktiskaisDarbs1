public class AI {

    public enum Algorithm { MINIMAX, ALPHABETA }

    private final int depthLimit;
    private final Algorithm algorithm;

    // Stats for last move
    public long lastGeneratedNodes = 0;
    public long lastEvaluatedNodes = 0;
    public long lastMoveTimeNs = 0;   // nanoseconds — use µs for display

    public AI(int depthLimit, Algorithm algorithm) {
        this.depthLimit = depthLimit;
        this.algorithm = algorithm;
    }

    public int chooseMove(GameState state) {
        lastGeneratedNodes = 0;
        lastEvaluatedNodes = 0;

        long startTime = System.nanoTime();

        int bestMove = -1;
        int bestValue = Integer.MIN_VALUE;

        for (int move : possibleMoves(state.stonesLeft)) {
            lastGeneratedNodes++;
            GameState next = state.applyMove(move);
            int value;
            if (algorithm == Algorithm.MINIMAX) {
                value = minimax(next, depthLimit - 1);
            } else {
                value = alphabeta(next, depthLimit - 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            }
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        lastMoveTimeNs = System.nanoTime() - startTime;
        return bestMove;
    }

    // ── Pure Minimax (no pruning) ──────────────────────────────────────────────
    private int minimax(GameState state, int depth) {
        lastGeneratedNodes++;

        if (state.isTerminal()) {
            lastEvaluatedNodes++;
            return terminalValue(state);
        }
        if (depth == 0) {
            lastEvaluatedNodes++;
            return heuristicValue(state);
        }

        // maximizing = AI (P2) turn; minimizing = human (P1) turn
        boolean maximizing = !state.p1Turn;

        if (maximizing) {
            int value = Integer.MIN_VALUE;
            for (int move : possibleMoves(state.stonesLeft)) {
                lastGeneratedNodes++;
                GameState next = state.applyMove(move);
                value = Math.max(value, minimax(next, depth - 1));
            }
            return value;
        } else {
            int value = Integer.MAX_VALUE;
            for (int move : possibleMoves(state.stonesLeft)) {
                lastGeneratedNodes++;
                GameState next = state.applyMove(move);
                value = Math.min(value, minimax(next, depth - 1));
            }
            return value;
        }
    }

    // ── Alpha-Beta pruning ─────────────────────────────────────────────────────
    private int alphabeta(GameState state, int depth, int alpha, int beta) {
        lastGeneratedNodes++;

        if (state.isTerminal()) {
            lastEvaluatedNodes++;
            return terminalValue(state);
        }
        if (depth == 0) {
            lastEvaluatedNodes++;
            return heuristicValue(state);
        }

        boolean maximizing = !state.p1Turn;

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
        return s.finalScoreP2() - s.finalScoreP1();
    }

    private int heuristicValue(GameState s) {
        int p1 = s.p1Points + s.p1Taken;
        int p2 = s.p2Points + s.p2Taken;
        int turnBias = s.p1Turn ? -1 : 1;
        return (p2 - p1) * 10 + turnBias;
    }

    private int[] possibleMoves(int stonesLeft) {
        if (stonesLeft >= 3) return new int[]{2, 3};
        if (stonesLeft == 2) return new int[]{2};
        return new int[]{};
    }

    public String getAlgorithmName() {
        return algorithm == Algorithm.MINIMAX ? "Minimax" : "Alpha-Beta";
    }
}
