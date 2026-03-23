public class GameState {
    public final int stonesLeft;

    // points already earned from parity rule (+2 each move)
    public final int p1Points;
    public final int p2Points;

    // stones taken (added to points at the end)
    public final int p1Taken;
    public final int p2Taken;

    // true -> Player 1 to move, false -> Player 2 to move
    public final boolean p1Turn;

    public GameState(int stonesLeft, int p1Points, int p2Points, int p1Taken, int p2Taken, boolean p1Turn) {
        this.stonesLeft = stonesLeft;
        this.p1Points = p1Points;
        this.p2Points = p2Points;
        this.p1Taken = p1Taken;
        this.p2Taken = p2Taken;
        this.p1Turn = p1Turn;
    }

    public boolean isTerminal() {
        return stonesLeft == 0;
    }

    // Final score includes taken stones
    public int finalScoreP1() {
        return p1Points;
    }

    public int finalScoreP2() {
        return p2Points;
    }

    // Apply move (take 2 or 3) and return next state
    public GameState applyMove(int take) {
        int newStones = stonesLeft - take;

        int np1Points = p1Points, np2Points = p2Points;
        int np1Taken = p1Taken, np2Taken = p2Taken;

        if (p1Turn) {
            np1Taken += take;
        } else {
            np2Taken += take;
        }

        // parity rule after taking: check what remains on table
        if (newStones % 2 == 0) {
            // opponent gets +2
            if (p1Turn) np2Points += 2;
            else np1Points += 2;
        } else {
            // current player gets +2
            if (p1Turn) np1Points += 2;
            else np2Points += 2;
        }

        return new GameState(newStones, np1Points, np2Points, np1Taken, np2Taken, !p1Turn);
    }
}
