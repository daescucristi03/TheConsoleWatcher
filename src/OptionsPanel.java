import javax.swing.*;
import java.awt.*;

public class OptionsPanel extends JPanel {
    private Main mainFrame;
    private JCheckBox muteCheckBox;
    private JSlider volumeSlider;

    public OptionsPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(Theme.CRT_BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 20, 0);

        JLabel titleLabel = new JLabel("SYSTEM CONFIGURATION");
        titleLabel.setFont(Theme.TITLE_FONT);
        titleLabel.setForeground(Theme.CRT_GREEN);
        add(titleLabel, gbc);

        // Volume Slider
        JLabel volLabel = new JLabel("AUDIO OUTPUT LEVEL");
        volLabel.setFont(Theme.RETRO_FONT);
        volLabel.setForeground(Theme.CRT_GREEN);
        add(volLabel, gbc);

        volumeSlider = new JSlider(0, 100, (int)(SoundManager.getVolume() * 100));
        volumeSlider.setBackground(Theme.CRT_BLACK);
        volumeSlider.setForeground(Theme.CRT_GREEN);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintTicks(true);
        volumeSlider.addChangeListener(e -> {
            SoundManager.setVolume(volumeSlider.getValue() / 100.0f);
        });
        add(volumeSlider, gbc);

        // Mute Checkbox
        muteCheckBox = new JCheckBox("MUTE AUDIO");
        muteCheckBox.setFont(Theme.RETRO_FONT);
        muteCheckBox.setForeground(Theme.CRT_GREEN);
        muteCheckBox.setBackground(Theme.CRT_BLACK);
        muteCheckBox.setFocusPainted(false);
        muteCheckBox.setSelected(SoundManager.isMuted());
        muteCheckBox.addActionListener(e -> {
            SoundManager.setMuted(muteCheckBox.isSelected());
        });
        add(muteCheckBox, gbc);

        // Back Button
        JButton backButton = createRetroButton("RETURN TO MAIN MENU");
        backButton.addActionListener(e -> mainFrame.showMainMenu());
        add(backButton, gbc);
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
