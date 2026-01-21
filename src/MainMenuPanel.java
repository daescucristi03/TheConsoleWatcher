import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {
    private Main mainFrame;

    public MainMenuPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(Theme.CRT_BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 20, 0);

        JLabel titleLabel = new JLabel("THE CONSOLE WATCHER");
        titleLabel.setFont(Theme.TITLE_FONT);
        titleLabel.setForeground(Theme.CRT_GREEN);
        add(titleLabel, gbc);

        JButton startButton = createRetroButton("START SYSTEM");
        startButton.addActionListener(e -> mainFrame.showDifficultySelection());
        add(startButton, gbc);
        
        JButton optionsButton = createRetroButton("CONFIGURATION");
        optionsButton.addActionListener(e -> mainFrame.showOptions());
        add(optionsButton, gbc);

        JButton exitButton = createRetroButton("SHUTDOWN");
        exitButton.addActionListener(e -> System.exit(0));
        add(exitButton, gbc);
    }

    private JButton createRetroButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.RETRO_FONT);
        button.setForeground(Theme.CRT_GREEN);
        button.setBackground(Theme.CRT_BLACK);
        button.setBorder(BorderFactory.createLineBorder(Theme.CRT_GREEN, 2));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        
        // Add hover effect and sound
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Theme.CRT_GREEN);
                button.setForeground(Theme.CRT_BLACK);
                SoundManager.playKeyClick();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Theme.CRT_BLACK);
                button.setForeground(Theme.CRT_GREEN);
            }
            
            public void mousePressed(java.awt.event.MouseEvent evt) {
                SoundManager.playKeyClick();
            }
        });
        
        return button;
    }
}
