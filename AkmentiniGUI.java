import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * GUI priekšgals spēlei "Akmentiņi".
 * Atkarīgs no: GameState.java, AI.java
 * Kompilēšana: javac GameState.java AI.java AkmentiņiGUI.java
 * Palaišana:   java AkmentiņiGUI
 */
public class AkmentiniGUI {

    // ─── Colours ──────────────────────────────────────────────────────────────
    static final Color BG         = new Color(245, 245, 245);
    static final Color CARD_BG    = new Color(235, 235, 235);
    static final Color ACTIVE_BDR = new Color(24,  95, 165);
    static final Color BLUE_TXT   = new Color(24,  95, 165);
    static final Color ORANGE_TXT = new Color(184, 90,  48);
    static final Color MUTED      = new Color(100, 100, 100);
    static final Color STONE_CLR  = new Color(136, 135, 128);

    // ─── State ────────────────────────────────────────────────────────────────
    static GameState state;
    static AI ai = new AI(16);

    // ─── UI refs ──────────────────────────────────────────────────────────────
    static JFrame frame;
    static CardLayout cards;
    static JPanel root;

    static JSpinner stoneSpinner;

    static JLabel stonesCountLabel, statusLabel;
    static JLabel p1PtsLabel, p1TakenLabel;
    static JLabel p2PtsLabel, p2TakenLabel;
    static JPanel p1Card, p2Card;
    static JPanel stonesVisual;
    static JButton btn2, btn3;
    static JTextArea logArea;

    static JLabel resultTitle, resultScores;

    // ─── main ─────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Akmentiņi");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(620, 700);
            frame.setMinimumSize(new Dimension(480, 600));
            frame.setLocationRelativeTo(null);

            cards = new CardLayout();
            root  = new JPanel(cards);
            root.setBackground(BG);

            root.add(buildSetupPanel(),  "setup");
            root.add(buildGamePanel(),   "game");
            root.add(buildResultPanel(), "result");

            frame.setContentPane(root);
            frame.setVisible(true);
        });
    }

    // ─── Setup panel ──────────────────────────────────────────────────────────
    static JPanel buildSetupPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(BG);
        inner.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        JLabel title = new JLabel("Akmentiņi");
        title.setFont(new Font("SansSerif", Font.PLAIN, 28));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Paņem 2 vai 3 akmentiņus. Pāra skaits galda — pretiniekam +2 pts.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setBackground(BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel("Akmentiņu skaits (50–70):  ");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lbl.setForeground(MUTED);

        SpinnerNumberModel model = new SpinnerNumberModel(60, 50, 70, 1);
        stoneSpinner = new JSpinner(model);
        stoneSpinner.setPreferredSize(new Dimension(70, 32));
        ((JSpinner.DefaultEditor) stoneSpinner.getEditor()).getTextField()
                .setFont(new Font("SansSerif", Font.PLAIN, 15));

        JButton start = styledButton("Sākt spēli", ACTIVE_BDR, Color.WHITE);
        start.setPreferredSize(new Dimension(120, 32));
        start.addActionListener(e -> startGame());

        row.add(lbl);
        row.add(stoneSpinner);
        row.add(Box.createHorizontalStrut(12));
        row.add(start);

        inner.add(title);
        inner.add(Box.createVerticalStrut(6));
        inner.add(sub);
        inner.add(Box.createVerticalStrut(24));
        inner.add(row);

        p.add(inner);
        return p;
    }

    // ─── Game panel ───────────────────────────────────────────────────────────
    static JPanel buildGamePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel scoreRow = new JPanel(new GridLayout(1, 2, 12, 0));
        scoreRow.setBackground(BG);
        scoreRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        scoreRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        p1Card = buildScoreCard("Tu (P1)", "Cilvēks");
        p2Card = buildScoreCard("Pretinieks (P2)", "AI");
        p1PtsLabel   = findLabel(p1Card, "pts");
        p1TakenLabel = findLabel(p1Card, "taken");
        p2PtsLabel   = findLabel(p2Card, "pts");
        p2TakenLabel = findLabel(p2Card, "taken");
        scoreRow.add(p1Card);
        scoreRow.add(p2Card);

        JPanel stonesArea = new JPanel();
        stonesArea.setLayout(new BoxLayout(stonesArea, BoxLayout.Y_AXIS));
        stonesArea.setBackground(CARD_BG);
        stonesArea.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(12, CARD_BG),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        stonesArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        stonesArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        stonesCountLabel = new JLabel("60", SwingConstants.CENTER);
        stonesCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));
        stonesCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel stonesLbl = new JLabel("akmentiņi uz galda", SwingConstants.CENTER);
        stonesLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        stonesLbl.setForeground(MUTED);
        stonesLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        stonesVisual = new JPanel(new WrapLayout(FlowLayout.CENTER, 6, 4));
        stonesVisual.setBackground(CARD_BG);
        stonesVisual.setAlignmentX(Component.CENTER_ALIGNMENT);

        stonesArea.add(stonesCountLabel);
        stonesArea.add(Box.createVerticalStrut(2));
        stonesArea.add(stonesLbl);
        stonesArea.add(Box.createVerticalStrut(8));
        stonesArea.add(stonesVisual);

        statusLabel = new JLabel("Tavs gājiens. Paņem 2 vai 3.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setForeground(MUTED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setBackground(BG);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn2 = styledButton("Paņemt 2", new Color(60, 60, 60), new Color(60, 60, 60));
        btn3 = styledButton("Paņemt 3", new Color(60, 60, 60), new Color(60, 60, 60));
        btn2.setPreferredSize(new Dimension(120, 38));
        btn3.setPreferredSize(new Dimension(120, 38));
        btn2.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn3.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn2.addActionListener(e -> humanMove(2));
        btn3.addActionListener(e -> humanMove(3));
        btnRow.add(btn2);
        btnRow.add(btn3);

        logArea = new JTextArea(7, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(CARD_BG);
        logArea.setForeground(new Color(80, 80, 80));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(new RoundedBorder(8, CARD_BG));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        p.add(scoreRow);
        p.add(Box.createVerticalStrut(14));
        p.add(stonesArea);
        p.add(Box.createVerticalStrut(12));
        p.add(statusLabel);
        p.add(Box.createVerticalStrut(10));
        p.add(btnRow);
        p.add(Box.createVerticalStrut(12));
        p.add(scroll);

        return p;
    }

    // ─── Result panel ─────────────────────────────────────────────────────────
    static JPanel buildResultPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(CARD_BG);
        box.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(12, CARD_BG),
                BorderFactory.createEmptyBorder(32, 40, 32, 40)));

        resultTitle = new JLabel("", SwingConstants.CENTER);
        resultTitle.setFont(new Font("SansSerif", Font.PLAIN, 26));
        resultTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultScores = new JLabel("", SwingConstants.CENTER);
        resultScores.setFont(new Font("SansSerif", Font.PLAIN, 15));
        resultScores.setForeground(MUTED);
        resultScores.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton again = styledButton("Spēlēt vēlreiz", ACTIVE_BDR, Color.WHITE);
        again.setPreferredSize(new Dimension(160, 36));
        again.setAlignmentX(Component.CENTER_ALIGNMENT);
        again.addActionListener(e -> resetGame());

        box.add(resultTitle);
        box.add(Box.createVerticalStrut(8));
        box.add(resultScores);
        box.add(Box.createVerticalStrut(20));
        box.add(again);

        p.add(box);
        return p;
    }

    // ─── Score card helper ────────────────────────────────────────────────────
    static JPanel buildScoreCard(String labelText, String nameText) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, CARD_BG),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(MUTED);

        JLabel name = new JLabel(nameText);
        name.setFont(new Font("SansSerif", Font.PLAIN, 15));
        name.putClientProperty("role", "name");

        JLabel pts = new JLabel("0");
        pts.setFont(new Font("SansSerif", Font.PLAIN, 28));
        pts.putClientProperty("role", "pts");

        JLabel taken = new JLabel("Paņemti: 0");
        taken.setFont(new Font("SansSerif", Font.PLAIN, 12));
        taken.setForeground(MUTED);
        taken.putClientProperty("role", "taken");

        card.add(lbl);
        card.add(Box.createVerticalStrut(2));
        card.add(name);
        card.add(Box.createVerticalStrut(4));
        card.add(pts);
        card.add(Box.createVerticalStrut(2));
        card.add(taken);
        return card;
    }

    static JLabel findLabel(JPanel card, String role) {
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel) {
                Object r = ((JLabel) c).getClientProperty("role");
                if (role.equals(r)) return (JLabel) c;
            }
        }
        return new JLabel();
    }

    // ─── Button helper ────────────────────────────────────────────────────────
    static JButton styledButton(String text, Color borderColor, Color textColor) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(getBackground().darker());
                else if (getModel().isRollover()) g2.setColor(getBackground().brighter());
                else g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(true);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
        b.setForeground(textColor);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ─── Game logic ───────────────────────────────────────────────────────────
    static void startGame() {
        int n = (Integer) stoneSpinner.getValue();
        // GameState no GameState.java
        state = new GameState(n, 0, 0, 0, 0, true);
        logArea.setText("");
        addLog("Spēle sākta ar " + n + " akmentiņiem.", "system");
        updateUI();
        cards.show(root, "game");
    }

    static void humanMove(int take) {
        if (!state.p1Turn || take > state.stonesLeft) return;
        int after = state.stonesLeft - take;
        String parity = after % 2 == 0 ? "Pāra skaits — AI +2 pts" : "Nepāra skaits — tev +2 pts";
        // applyMove no GameState.java
        state = state.applyMove(take);
        addLog("Tu paņēmi " + take + ". " + parity, "p1");
        updateUI();
        if (checkEnd()) return;

        statusLabel.setText("AI domā...");
        btn2.setEnabled(false);
        btn3.setEnabled(false);

        javax.swing.Timer t = new javax.swing.Timer(400, e -> {
            // chooseMove no AI.java
            int m = ai.chooseMove(state);
            int after2 = state.stonesLeft - m;
            String p2parity = after2 % 2 == 0 ? "Pāra skaits — tev +2 pts" : "Nepāra skaits — AI +2 pts";
            state = state.applyMove(m);
            addLog("AI paņēma " + m + ". " + p2parity, "p2");
            updateUI();
            if (!checkEnd()) {
                statusLabel.setText("Tavs gājiens. Paņem 2 vai 3.");
                btn2.setEnabled(true);
                btn3.setEnabled(state.stonesLeft >= 3);
            }
        });
        t.setRepeats(false);
        t.start();
    }

    static boolean checkEnd() {
        // isTerminal, finalScoreP1, finalScoreP2 no GameState.java
        if (!state.isTerminal() && state.stonesLeft >= 2) return false;

        int f1 = state.finalScoreP1(), f2 = state.finalScoreP2();
        if (f1 > f2) { resultTitle.setText("Tu uzvarēji!"); resultTitle.setForeground(BLUE_TXT); }
        else if (f2 > f1) { resultTitle.setText("AI uzvarēja."); resultTitle.setForeground(ORANGE_TXT); }
        else { resultTitle.setText("Neizšķirts!"); resultTitle.setForeground(Color.DARK_GRAY); }
        resultScores.setText("Tu: " + f1 + " pts  |  AI: " + f2 + " pts  (punkti + paņemtie akmentiņi)");
        cards.show(root, "result");
        return true;
    }

    static void resetGame() {
        cards.show(root, "setup");
        state = null;
    }

    static void updateUI() {
        stonesCountLabel.setText(String.valueOf(state.stonesLeft));
        // lauki no GameState.java: p1Points, p2Points, p1Taken, p2Taken, p1Turn
        p1PtsLabel.setText(String.valueOf(state.p1Points));
        p2PtsLabel.setText(String.valueOf(state.p2Points));
        p1TakenLabel.setText("Paņemti: " + state.p1Taken);
        p2TakenLabel.setText("Paņemti: " + state.p2Taken);

        p1Card.setBorder(BorderFactory.createCompoundBorder(
                state.p1Turn ? new RoundedBorder(8, ACTIVE_BDR) : new RoundedBorder(8, CARD_BG),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        p2Card.setBorder(BorderFactory.createCompoundBorder(
                !state.p1Turn ? new RoundedBorder(8, ACTIVE_BDR) : new RoundedBorder(8, CARD_BG),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        stonesVisual.removeAll();
        int show = Math.min(state.stonesLeft, 70);
        for (int i = 0; i < show; i++) {
            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(STONE_CLR);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            dot.setPreferredSize(new Dimension(14, 14));
            dot.setOpaque(false);
            stonesVisual.add(dot);
        }
        stonesVisual.revalidate();
        stonesVisual.repaint();

        btn3.setEnabled(state.stonesLeft >= 3);
        frame.revalidate();
        frame.repaint();
    }

    static void addLog(String msg, String type) {
        String prefix = type.equals("p1") ? "[Tu]  " : type.equals("p2") ? "[AI]  " : "[--]  ";
        logArea.append(prefix + msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // ─── Rounded border ───────────────────────────────────────────────────────
    static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color  = color;
        }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override public Insets getBorderInsets(Component c, Insets i) {
            i.set(radius / 2, radius / 2, radius / 2, radius / 2);
            return i;
        }
    }

    // ─── WrapLayout ───────────────────────────────────────────────────────────
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target)   { return layoutSize(target, false); }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - insets.left - insets.right - hgap * 2;
                int x = 0, rowHeight = 0, totalHeight = insets.top + insets.bottom + vgap * 2;
                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component c = target.getComponent(i);
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (x + d.width > maxWidth && x > 0) { x = 0; totalHeight += rowHeight + vgap; rowHeight = 0; }
                    x += d.width + hgap;
                    rowHeight = Math.max(rowHeight, d.height);
                }
                totalHeight += rowHeight;
                return new Dimension(targetWidth, totalHeight);
            }
        }
    }
}
