import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class SimpleGUI {

    // ── Persistent win counters (survive restarts) ─────────────────────────────
    private static int totalHumanWins = 0;
    private static int totalAiWins = 0;
    private static int totalDraws = 0;

    // Per-session experiment stats
    private static int sessionHumanWins = 0;
    private static int sessionAiWins = 0;
    private static int sessionDraws = 0;
    private static long sessionTotalNodes = 0;
    private static long sessionTotalEvaluated = 0;
    private static long sessionTotalTime = 0;
    private static int sessionMoves = 0;
    private static AI.Algorithm sessionAlgorithm = AI.Algorithm.ALPHABETA;

    // ── Current game state ─────────────────────────────────────────────────────
    GameState state;
    AI ai;

    // ── UI components ──────────────────────────────────────────────────────────
    JLabel stonesLabel     = new JLabel();
    JLabel scoreLabel      = new JLabel();
    JLabel infoLabel       = new JLabel("Spēle sākta");
    JLabel statsLabel      = new JLabel(" ");
    JLabel winTrackerLabel = new JLabel();
    JButton b2             = new JButton("Paņemt 2");
    JButton b3             = new JButton("Paņemt 3");

    // Algorithm toggle
    JRadioButton rbMinimax   = new JRadioButton("Minimax");
    JRadioButton rbAlphaBeta = new JRadioButton("Alpha-Beta", true);

    public SimpleGUI() {
        buildUI();
    }

    private void buildUI() {
        // ── Algorithm selection ──────────────────────────────────────────────
        AI.Algorithm chosenAlgo = chooseAlgorithmDialog();
        sessionAlgorithm = chosenAlgo;
        int stones = askStones();

        ai = new AI(24, chosenAlgo);
        state = new GameState(stones, 0, 0, 0, 0, true);

        // ── Frame ────────────────────────────────────────────────────────────
        JFrame frame = new JFrame("Akmentiņu spēle  [" + ai.getAlgorithmName() + "]");
        frame.setSize(520, 420);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));
        frame.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── TOP: stones + score ──────────────────────────────────────────────
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        stonesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        stonesLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        scoreLabel.setFont(new Font("Monospaced", Font.PLAIN, 15));
        topPanel.add(stonesLabel);
        topPanel.add(scoreLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        // ── CENTER: buttons ──────────────────────────────────────────────────
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));

        b2.setFont(new Font("SansSerif", Font.BOLD, 14));
        b3.setFont(new Font("SansSerif", Font.BOLD, 14));
        b2.setPreferredSize(new Dimension(120, 42));
        b3.setPreferredSize(new Dimension(120, 42));

        JButton restart = new JButton("Restartēt");
        restart.setFont(new Font("SansSerif", Font.PLAIN, 13));

        centerPanel.add(b2);
        centerPanel.add(b3);
        centerPanel.add(restart);
        frame.add(centerPanel, BorderLayout.CENTER);

        // ── SOUTH: info + stats + win tracker ───────────────────────────────
        JPanel southPanel = new JPanel(new GridLayout(4, 1, 2, 2));

        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        winTrackerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statsLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statsLabel.setForeground(new Color(60, 100, 180));
        winTrackerLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Algorithm radio buttons in a panel
        JPanel algoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        algoPanel.add(new JLabel("Algoritms nākamajā spēlē:"));
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbMinimax);
        bg.add(rbAlphaBeta);
        if (chosenAlgo == AI.Algorithm.MINIMAX) rbMinimax.setSelected(true);
        else rbAlphaBeta.setSelected(true);
        algoPanel.add(rbMinimax);
        algoPanel.add(rbAlphaBeta);

        southPanel.add(infoLabel);
        southPanel.add(statsLabel);
        southPanel.add(winTrackerLabel);
        southPanel.add(algoPanel);

        frame.add(southPanel, BorderLayout.SOUTH);

        // ── Listeners ────────────────────────────────────────────────────────
        b2.addActionListener(e -> move(2));
        b3.addActionListener(e -> move(3));

        restart.addActionListener(e -> {
            AI.Algorithm next = rbAlphaBeta.isSelected() ? AI.Algorithm.ALPHABETA : AI.Algorithm.MINIMAX;
            frame.dispose();
            // Pass next algo via a new dialog bypass: just open new GUI
            // We keep session stats across restarts by using static fields above
            new SimpleGUI(next);
        });

        update();
        updateWinTracker();
        frame.setVisible(true);
    }

    // Constructor that skips algorithm dialog (used on restart)
    public SimpleGUI(AI.Algorithm algo) {
        int stones = askStones();
        sessionAlgorithm = algo;
        ai = new AI(24, algo);
        state = new GameState(stones, 0, 0, 0, 0, true);
        buildUIWithAlgo(algo, stones);
    }

    private void buildUIWithAlgo(AI.Algorithm chosenAlgo, int stones) {
        JFrame frame = new JFrame("Akmentiņu spēle  [" + ai.getAlgorithmName() + "]");
        frame.setSize(520, 420);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));
        frame.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        stonesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        stonesLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        scoreLabel.setFont(new Font("Monospaced", Font.PLAIN, 15));
        topPanel.add(stonesLabel);
        topPanel.add(scoreLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        b2.setFont(new Font("SansSerif", Font.BOLD, 14));
        b3.setFont(new Font("SansSerif", Font.BOLD, 14));
        b2.setPreferredSize(new Dimension(120, 42));
        b3.setPreferredSize(new Dimension(120, 42));
        b2.setEnabled(true);
        b3.setEnabled(true);

        JButton restart = new JButton("Restartēt");
        restart.setFont(new Font("SansSerif", Font.PLAIN, 13));
        centerPanel.add(b2);
        centerPanel.add(b3);
        centerPanel.add(restart);
        frame.add(centerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new GridLayout(4, 1, 2, 2));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        winTrackerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statsLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statsLabel.setForeground(new Color(60, 100, 180));
        winTrackerLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        infoLabel.setText("Spēle sākta  [" + ai.getAlgorithmName() + "]");
        statsLabel.setText(" ");

        JPanel algoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        algoPanel.add(new JLabel("Algoritms nākamajā spēlē:"));
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbMinimax);
        bg.add(rbAlphaBeta);
        if (chosenAlgo == AI.Algorithm.MINIMAX) { rbMinimax.setSelected(true); }
        else { rbAlphaBeta.setSelected(true); }
        algoPanel.add(rbMinimax);
        algoPanel.add(rbAlphaBeta);

        southPanel.add(infoLabel);
        southPanel.add(statsLabel);
        southPanel.add(winTrackerLabel);
        southPanel.add(algoPanel);
        frame.add(southPanel, BorderLayout.SOUTH);

        b2.addActionListener(e -> move(2));
        b3.addActionListener(e -> move(3));

        restart.addActionListener(e -> {
            AI.Algorithm next = rbAlphaBeta.isSelected() ? AI.Algorithm.ALPHABETA : AI.Algorithm.MINIMAX;
            frame.dispose();
            new SimpleGUI(next);
        });

        update();
        updateWinTracker();
        frame.setVisible(true);
    }

    // ── Algorithm dialog ───────────────────────────────────────────────────────
    private AI.Algorithm chooseAlgorithmDialog() {
        Object[] options = {"Alpha-Beta", "Minimax"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Izvēlies AI algoritmu:",
                "Algoritma izvēle",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        return (choice == 1) ? AI.Algorithm.MINIMAX : AI.Algorithm.ALPHABETA;
    }

    // ── Stones input ───────────────────────────────────────────────────────────
    int askStones() {
        while (true) {
            String input = JOptionPane.showInputDialog("Ievadi akmeņu skaitu (50-70):");
            if (input == null) System.exit(0);
            try {
                int s = Integer.parseInt(input.trim());
                if (s < 50 || s > 70) {
                    JOptionPane.showMessageDialog(null, "Jābūt intervālā 50..70!");
                    continue;
                }
                return s;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Ievadi veselu skaitli!");
            }
        }
    }

    // ── Game logic ─────────────────────────────────────────────────────────────
    void move(int m) {
        if (state.stonesLeft < m) return;

        state = state.applyMove(m);
        if (checkEnd()) return;

        int aiMove = ai.chooseMove(state);
        state = state.applyMove(aiMove);

        // Accumulate session stats
        sessionTotalNodes    += ai.lastGeneratedNodes;
        sessionTotalEvaluated+= ai.lastEvaluatedNodes;
        sessionTotalTime     += ai.lastMoveTimeNs;
        sessionMoves++;

        String statsText = String.format(
                "[%s]  Ģen.: %,d  Nov.: %,d  Laiks: %s",
                ai.getAlgorithmName(),
                ai.lastGeneratedNodes,
                ai.lastEvaluatedNodes,
                formatTime(ai.lastMoveTimeNs)
        );
        statsLabel.setText(statsText);
        infoLabel.setText("Tu paņēmi " + m + ", AI paņēma " + aiMove);

        if (checkEnd()) return;
        update();
    }

    void update() {
        stonesLabel.setText("🪨  Akmeņi: " + state.stonesLeft);
        scoreLabel.setText("Tu: " + state.p1Points + " pts  (+paņemts " + state.p1Taken
                + ")    |    AI: " + state.p2Points + " pts  (+paņemts " + state.p2Taken + ")");
    }

    void updateWinTracker() {
        winTrackerLabel.setText(String.format(
                "Uzvaru kopsavilkums  —  Tu: %d  |  AI: %d  |  Neizšķirts: %d",
                totalHumanWins, totalAiWins, totalDraws
        ));
    }

    boolean checkEnd() {
        if (state.stonesLeft <= 1) {
            int p1 = state.finalScoreP1();
            int p2 = state.finalScoreP2();

            String msg;
            if (p1 > p2) {
                msg = "🏆 Tu uzvarēji!";
                totalHumanWins++;
                sessionHumanWins++;
            } else if (p2 > p1) {
                msg = "🤖 AI uzvarēja!";
                totalAiWins++;
                sessionAiWins++;
            } else {
                msg = "🤝 Neizšķirts";
                totalDraws++;
                sessionDraws++;
            }

            stonesLabel.setText("═══  SPĒLE BEIGUSIES  ═══");
            scoreLabel.setText("Tu: " + p1 + " gala punkti    |    AI: " + p2 + " gala punkti");
            infoLabel.setText(msg);

            // Show session summary
            long avgTimeNs = sessionMoves > 0 ? sessionTotalTime / sessionMoves : 0;
            long avgNodes = sessionMoves > 0 ? sessionTotalNodes / sessionMoves : 0;
            long avgEval  = sessionMoves > 0 ? sessionTotalEvaluated / sessionMoves : 0;
            statsLabel.setText(String.format(
                    "Sesija [%s]: Tu %d | AI %d | Vid. ģen.: %,d  Nov.: %,d | Vid. laiks: %s",
                    sessionAlgorithm == AI.Algorithm.MINIMAX ? "Minimax" : "Alpha-Beta",
                    sessionHumanWins, sessionAiWins,
                    avgNodes, avgEval, formatTime(avgTimeNs)
            ));

            updateWinTracker();

            b2.setEnabled(false);
            b3.setEnabled(false);
            return true;
        }
        return false;
    }

    /** Formats nanoseconds into a human-readable string: µs or ms depending on magnitude. */
    private static String formatTime(long ns) {
        if (ns < 1_000_000L) {
            // sub-millisecond: show microseconds with 1 decimal
            return String.format("%.1f µs", ns / 1_000.0);
        } else {
            // milliseconds with 2 decimals
            return String.format("%.2f ms", ns / 1_000_000.0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SimpleGUI::new);
    }
}
