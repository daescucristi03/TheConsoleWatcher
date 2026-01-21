import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class JumpscarePanel extends JPanel {
    private Main mainFrame;
    private Timer animTimer;
    private Random random = new Random();
    private int frameCount = 0;

    public JumpscarePanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Color.BLACK);
        
        animTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frameCount++;
                repaint();
                
                // End jumpscare after 2 seconds
                if (frameCount > 40) {
                    animTimer.stop();
                    mainFrame.showDeathScreen();
                }
            }
        });
    }
    
    public void start() {
        frameCount = 0;
        SoundManager.playScream();
        animTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();
        
        // Flashing Red vs Black
        if (frameCount % 4 < 2) {
            g2.setColor(new Color(150, 0, 0)); // Dark Red
            g2.fillRect(0, 0, w, h);
            
            // Draw the "Black Entity"
            drawEntity(g2, w, h);
            
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, w, h);
            
            // In the dark, just 2 white dots for eyes
            g2.setColor(Color.WHITE);
            int shakeX = random.nextInt(20) - 10;
            int shakeY = random.nextInt(20) - 10;
            
            // Simple white dots
            g2.fillOval(w/2 - 40 + shakeX, h/2 - 50 + shakeY, 10, 10);
            g2.fillOval(w/2 + 30 + shakeX, h/2 - 50 + shakeY, 10, 10);
        }
        
        // No Text
    }
    
    private void drawEntity(Graphics2D g2, int w, int h) {
        g2.setColor(Color.BLACK);
        
        int centerX = w / 2;
        int centerY = h / 2;
        
        // Shake effect
        centerX += random.nextInt(40) - 20;
        centerY += random.nextInt(40) - 20;
        
        // Draw Fuller Body (Mass of darkness)
        // Main torso mass
        g2.fillOval(centerX - 60, centerY - 50, 120, 300);
        
        // Head mass
        g2.fillOval(centerX - 70, centerY - 180, 140, 160);
        
        // Shoulders/Arms mass
        g2.fillOval(centerX - 120, centerY - 80, 80, 200); // Left
        g2.fillOval(centerX + 40, centerY - 80, 80, 200); // Right
        
        g2.setStroke(new BasicStroke(3));
        
        // Scribbles on top for texture
        for (int i = 0; i < 100; i++) {
            int x1 = centerX + random.nextInt(200) - 100;
            int y1 = centerY - 200 + random.nextInt(400);
            int x2 = centerX + random.nextInt(200) - 100;
            int y2 = centerY - 200 + random.nextInt(400);
            
            g2.drawLine(x1, y1, x2, y2);
        }
        
        int headY = centerY - 150;
        
        // Eyes (2 White Dots)
        g2.setColor(Color.WHITE);
        g2.fillOval(centerX - 40, headY + 80, 10, 10);
        g2.fillOval(centerX + 30, headY + 80, 10, 10);
        
        // Mouth (Gaping Scream)
        g2.setColor(Color.BLACK);
        g2.fillOval(centerX - 40, headY + 120, 80, 100); // Base
        g2.setColor(Color.WHITE); // Teeth/Noise
        for(int i=0; i<10; i++) {
             g2.drawLine(centerX - 30 + random.nextInt(60), headY + 120, 
                         centerX - 30 + random.nextInt(60), headY + 220);
        }
        
        // Long Arms reaching out
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(15)); // Thicker arms
        g2.drawLine(centerX - 80, centerY, 0, h); // Left arm
        g2.drawLine(centerX + 80, centerY, w, h); // Right arm
    }
}
