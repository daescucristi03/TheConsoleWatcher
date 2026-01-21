import javax.swing.*;
import java.awt.*;

public class TutorialPanel extends JPanel {
    private Main mainFrame;

    public TutorialPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.CRT_BLACK);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("SYSTEM MANUAL", SwingConstants.CENTER);
        titleLabel.setFont(Theme.TITLE_FONT);
        titleLabel.setForeground(Theme.CRT_GREEN);
        add(titleLabel, BorderLayout.NORTH);

        // Instructions Text
        JTextArea instructions = new JTextArea();
        instructions.setEditable(false);
        instructions.setBackground(Theme.CRT_BLACK);
        instructions.setForeground(Theme.CRT_GREEN);
        instructions.setFont(Theme.RETRO_FONT);
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setMargin(new Insets(20, 20, 20, 20));
        
        instructions.setText(
            "WELCOME, OPERATOR.\n\n" +
            "YOUR OBJECTIVE: SURVIVE UNTIL 6 AM AND PROTECT THE ASSET.\n\n" +
            "1. THE ENTITY:\n" +
            "   - A hostile entity is roaming the facility.\n" +
            "   - Track it using the CAMERA SYSTEM.\n" +
            "   - It can move through HALLWAYS and VENTS (dashed lines).\n\n" +
            "2. DEFENSE:\n" +
            "   - LOCK DOORS to block the entity's path.\n" +
            "   - WARNING: You can only lock ONE remote room at a time.\n" +
            "   - Your OFFICE DOOR can be locked independently.\n\n" +
            "3. THE ASSET:\n" +
            "   - Located in the facility (Cyan Marker).\n" +
            "   - If the entity reaches the asset, the mission fails.\n" +
            "   - Use 'MOVE ASSET [ROOM_ID]' to guide it to safety.\n\n" +
            "4. POWER MANAGEMENT:\n" +
            "   - Locking doors and using cameras drains POWER.\n" +
            "   - If power reaches 0%, life support fails.\n\n" +
            "COMMANDS:\n" +
            "   - 'cameras': Toggle map view.\n" +
            "   - 'lock/unlock door': Control office door.\n" +
            "   - 'lock/unlock room': Control remote doors.\n" +
            "   - 'move asset [id]': Relocate the asset.\n" +
            "   - 'status': Check system status.\n\n" +
            "GOOD LUCK."
        );

        JScrollPane scrollPane = new JScrollPane(instructions);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.CRT_GREEN));
        scrollPane.getViewport().setBackground(Theme.CRT_BLACK);
        add(scrollPane, BorderLayout.CENTER);

        // Start Button
        JButton startButton = new JButton("INITIATE SEQUENCE");
        startButton.setFont(Theme.RETRO_FONT);
        startButton.setForeground(Theme.CRT_GREEN);
        startButton.setBackground(Theme.CRT_BLACK);
        startButton.setBorder(BorderFactory.createLineBorder(Theme.CRT_GREEN, 2));
        startButton.setFocusPainted(false);
        startButton.setContentAreaFilled(false);
        startButton.setOpaque(true);
        startButton.setPreferredSize(new Dimension(200, 50));
        
        startButton.addActionListener(e -> mainFrame.startGame());
        
        startButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startButton.setBackground(Theme.CRT_GREEN);
                startButton.setForeground(Theme.CRT_BLACK);
                SoundManager.playKeyClick();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                startButton.setBackground(Theme.CRT_BLACK);
                startButton.setForeground(Theme.CRT_GREEN);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Theme.CRT_BLACK);
        buttonPanel.add(startButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
