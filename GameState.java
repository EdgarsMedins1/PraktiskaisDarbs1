public class GameState {
    public final int stonesLeft;

    public final int p1Points;
    public final int p2Points;

    public final int p1Taken;
    public final int p2Taken;

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
        return stonesLeft < 2;
    }

    public int finalScoreP1() {
        return p1Points + p1Taken;
    }

    public int finalScoreP2() {
        return p2Points + p2Taken;
    }

    public GameState applyMove(int take) {
        if ((take != 2 && take != 3) || take > stonesLeft) {
            throw new IllegalArgumentException("Nederīgs gājiens: " + take);
        }

        int newStones = stonesLeft - take;

        int np1Points = p1Points;
        int np2Points = p2Points;
        int np1Taken = p1Taken;
        int np2Taken = p2Taken;

        if (p1Turn) {
            np1Taken += take;
        } else {
            np2Taken += take;
        }

        if (newStones % 2 == 0) {
            if (p1Turn) {
                np2Points += 2;
            } else {
                np1Points += 2;
            }
        } else {
            if (p1Turn) {
                np1Points += 2;
            } else {
                np2Points += 2;
            }
        }

        return new GameState(newStones, np1Points, np2Points, np1Taken, np2Taken, !p1Turn);
    }
}
