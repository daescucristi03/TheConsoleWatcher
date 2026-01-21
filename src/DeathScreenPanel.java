import javax.swing.*;
import java.awt.*;

public class DeathScreenPanel extends JPanel {
    private Main mainFrame;
    private JLabel causeLabel;

    public DeathScreenPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 20, 0);

        JLabel titleLabel = new JLabel("YOU ARE DEAD");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 48));
        titleLabel.setForeground(Color.RED);
        add(titleLabel, gbc);
        
        causeLabel = new JLabel("CONNECTION TERMINATED");
        causeLabel.setFont(Theme.RETRO_FONT);
        causeLabel.setForeground(Color.RED);
        add(causeLabel, gbc);

        JButton retryButton = createRetroButton("REBOOT SYSTEM");
        retryButton.addActionListener(e -> mainFrame.startGame());
        add(retryButton, gbc);

        JButton menuButton = createRetroButton("RETURN TO MENU");
        menuButton.addActionListener(e -> mainFrame.showMainMenu());
        add(menuButton, gbc);
    }
    
    public void setCause(String cause) {
        causeLabel.setText(cause);
    }

    private JButton createRetroButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.RETRO_FONT);
        button.setForeground(Color.RED);
        button.setBackground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.RED);
                button.setForeground(Color.BLACK);
                SoundManager.playKeyClick();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.BLACK);
                button.setForeground(Color.RED);
            }
        });
        
        return button;
    }
}
