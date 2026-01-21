import javax.swing.*;
import java.awt.*;

public class MonitorPanel extends JPanel {
    private GameState gameState;

    public MonitorPanel(GameState gameState) {
        this.gameState = gameState;
        setBackground(Theme.CRT_BLACK);
        setPreferredSize(new Dimension(300, 0));
        setBorder(BorderFactory.createLineBorder(Theme.CRT_GREEN));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Anti-aliasing for cleaner lines
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int padding = 20;

        // Draw Title
        g2.setColor(Theme.CRT_GREEN);
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2.drawString("SYSTEM MONITOR", padding, 30);

        // Draw Door Status Box (Main Office Door)
        int boxY = 60;
        int boxHeight = 100;
        g2.drawRect(padding, boxY, width - (padding * 2), boxHeight);
        
        g2.drawString("OFFICE DOOR:", padding + 10, boxY + 30);
        
        if (gameState.isOfficeDoorLocked()) {
            g2.setColor(Theme.CRT_GREEN);
            g2.fillRect(padding + 10, boxY + 45, width - (padding * 2) - 20, 40);
            g2.setColor(Theme.CRT_BLACK);
            g2.drawString("[ SECURED ]", padding + 60, boxY + 70);
        } else {
            g2.setColor(Theme.ALERT_RED); // Flashing Red if open
            if (System.currentTimeMillis() % 1000 < 500) { // Simple blink logic based on repaint
                 g2.fillRect(padding + 10, boxY + 45, width - (padding * 2) - 20, 40);
                 g2.setColor(Theme.CRT_BLACK);
                 g2.drawString("[ ! OPEN ! ]", padding + 60, boxY + 70);
            } else {
                 g2.drawRect(padding + 10, boxY + 45, width - (padding * 2) - 20, 40);
                 g2.drawString("[ ! OPEN ! ]", padding + 60, boxY + 70);
            }
        }
        
        // Draw Time
        int timeY = 200;
        g2.setColor(Theme.CRT_GREEN);
        g2.setFont(new Font("Monospaced", Font.BOLD, 24));
        String timeStr = String.format("%d:%02d AM", gameState.getCurrentHour(), gameState.getCurrentMinute());
        g2.drawString(timeStr, padding, timeY);

        // Draw Power Level
        int powerY = 240;
        g2.setFont(Theme.RETRO_FONT);
        g2.setColor(Theme.CRT_GREEN);
        g2.drawString("POWER LEVEL:", padding, powerY);
        
        g2.drawRect(padding, powerY + 10, width - (padding * 2), 30);
        
        // Power Bar Fill
        int powerLevel = gameState.getPowerLevel();
        if (powerLevel < 20) g2.setColor(Theme.ALERT_RED);
        else if (powerLevel < 50) g2.setColor(Theme.CRT_AMBER);
        else g2.setColor(Theme.CRT_GREEN);

        int barWidth = (int) ((width - (padding * 2)) * (powerLevel / 100.0));
        g2.fillRect(padding, powerY + 10, barWidth, 30);
        
        // Entity Warning
        if (gameState.isEntityNear()) {
            g2.setColor(Theme.ALERT_RED);
            g2.setFont(new Font("Monospaced", Font.BOLD, 24));
            g2.drawString("! PROXIMITY ALERT !", padding, 340);
        }
        
        // Current Camera Lock Status
        if (gameState.isCameraSystemActive()) {
            int cam = gameState.getCurrentCamera();
            g2.setColor(Theme.CRT_GREEN);
            g2.setFont(Theme.RETRO_FONT);
            g2.drawString("CAM " + cam + " LOCK STATUS:", padding, 390);
            
            boolean locked = gameState.isRoomLocked(cam);
            if (locked) {
                g2.setColor(Theme.CRT_GREEN);
                g2.drawString("[ LOCKED ]", padding, 410);
            } else {
                g2.setColor(Theme.CRT_AMBER);
                g2.drawString("[ UNLOCKED ]", padding, 410);
            }
        }
    }
}
