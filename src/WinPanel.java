import javax.swing.*;
import java.awt.*;

public class WinPanel extends JPanel {
    private Main mainFrame;

    public WinPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 20, 0);

        JLabel titleLabel = new JLabel("MISSION ACCOMPLISHED");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 48));
        titleLabel.setForeground(Theme.CRT_GREEN);
        add(titleLabel, gbc);
        
        JTextArea details = new JTextArea(
            "THE ENTITY HAS BEEN CAPTURED BY SECURITY TEAMS.\n" +
            "CONTAINMENT PROTOCOLS RESTORED.\n" +
            "THE ASSET IS SECURE.\n\n" +
            "EXCELLENT WORK, OPERATOR."
        );
        details.setFont(Theme.RETRO_FONT);
        details.setForeground(Theme.CRT_GREEN);
        details.setBackground(Color.BLACK);
        details.setEditable(false);
        details.setHighlighter(null);
        add(details, gbc);

        JButton menuButton = createRetroButton("RETURN TO MENU");
        menuButton.addActionListener(e -> mainFrame.showMainMenu());
        add(menuButton, gbc);
    }

    private JButton createRetroButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.RETRO_FONT);
        button.setForeground(Theme.CRT_GREEN);
        button.setBackground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(Theme.CRT_GREEN, 2));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Theme.CRT_GREEN);
                button.setForeground(Color.BLACK);
                SoundManager.playKeyClick();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.BLACK);
                button.setForeground(Theme.CRT_GREEN);
            }
        });
        
        return button;
    }
}
