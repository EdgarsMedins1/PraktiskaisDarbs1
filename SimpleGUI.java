import javax.swing.*;

public class SimpleGUI {

    GameState state;
    AI ai = new AI(24);

    JLabel label = new JLabel();

    public SimpleGUI() {
        int stones = askStones();

                state = new GameState(stones, 0, 0, 0, 0, true);

                JFrame frame = new JFrame("Akmentiņu spēle");
                frame.setSize(400, 200);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JButton b2 = new JButton("Paņemt 2");
                JButton b3 = new JButton("Paņemt 3");
                

                JPanel panel = new JPanel();
                panel.add(b2);
                panel.add(b3);

                frame.add(label, "North");
                frame.add(panel, "Center");

                update(0, 0);

                b2.addActionListener(e -> move(2));
                b3.addActionListener(e -> move(3));

                frame.setVisible(true);
    }

    int askStones() {
    while (true) {
        String input = JOptionPane.showInputDialog("Ievadi akmeņu skaitu (50-70):");

        if (input == null) System.exit(0);

        try {
            int stones = Integer.parseInt(input);

            if (stones < 50 || stones > 70) {
                JOptionPane.showMessageDialog(null, "Jābūt intervālā 50..70!");
                continue;
            }

            return stones;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ievadi veselu skaitli!");
        }
    }
}
    void move(int m) {
        if (state.stonesLeft < m) return;

        state = state.applyMove(m);
        if (checkEnd()) return;

        int aiMove = ai.chooseMove(state);
        state = state.applyMove(aiMove);
;

        checkEnd();
        update(m, aiMove);
    }

    void update(int lastPlayerMove, int lastAIMove) {
        label.setText(
            "Akmeņi: " + state.stonesLeft +
            "  Tu: " + state.p1Points +
            " | AI: " + state.p2Points +
            "  Pēdējais gājiens:" + " Tu= " + lastPlayerMove +
            ", AI=" + lastAIMove

        );
    }

    boolean checkEnd() {
        if (state.stonesLeft <= 1) {
            int p1 = state.finalScoreP1();
            int p2 = state.finalScoreP2();

            String msg;
            if (p1 > p2) msg = "Tu uzvarēji";
            else if (p2 > p1) msg = "AI uzvarēja";
            else msg = "Neizšķirts";

            JOptionPane.showMessageDialog(null,
                    "Beigas\nTu: " + p1 + "\nAI: " + p2 + "\n" + msg);

            System.exit(0);
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        new SimpleGUI();
    }
}
