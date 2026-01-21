import javax.swing.*;
import java.awt.*;

public class DifficultyPanel extends JPanel {
    private Main mainFrame;

    public DifficultyPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(Theme.CRT_BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 20, 0);

        JLabel titleLabel = new JLabel("SELECT DIFFICULTY");
        titleLabel.setFont(Theme.TITLE_FONT);
        titleLabel.setForeground(Theme.CRT_GREEN);
        add(titleLabel, gbc);

        // Easy Button
        JButton easyButton = createRetroButton("EASY");
        easyButton.addActionListener(e -> selectDifficulty(1));
        add(easyButton, gbc);
        
        // Normal Button
        JButton normalButton = createRetroButton("NORMAL");
        normalButton.addActionListener(e -> selectDifficulty(2));
        add(normalButton, gbc);
        
        // Hard Button
        JButton hardButton = createRetroButton("HARD");
        hardButton.addActionListener(e -> selectDifficulty(3));
        add(hardButton, gbc);
        
        // Back Button
        JButton backButton = createRetroButton("BACK");
        backButton.addActionListener(e -> mainFrame.showMainMenu());
        add(backButton, gbc);
    }
    
    private void selectDifficulty(int diff) {
        mainFrame.setDifficulty(diff);
        mainFrame.showTutorial();
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
        button.setPreferredSize(new Dimension(200, 50));
        
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
        });
        
        return button;
    }
}
