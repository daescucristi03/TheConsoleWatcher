import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class MapPanel extends JPanel {
    private GameState gameState;
    
    // Map ID -> Coordinates
    private Map<Integer, Rectangle> roomRects = new HashMap<>();
    private Rectangle officeRect;
    
    // Translation offsets for centering
    private int transX = 0;
    private int transY = 0;
    
    // Scaling factor
    private double scale = 1.0;

    public MapPanel(GameState gameState) {
        this.gameState = gameState;
        setBackground(Theme.CRT_BLACK);
        // Removed preferred size to allow filling parent
        setBorder(BorderFactory.createLineBorder(Theme.CRT_GREEN));
        
        setupLayout();
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!gameState.isCameraSystemActive()) return;
                
                // Adjust click point by inverse translation and scale
                // p = (click - trans) / scale
                int clickX = e.getX();
                int clickY = e.getY();
                
                double unscaledX = (clickX - transX) / scale;
                double unscaledY = (clickY - transY) / scale;
                
                Point p = new Point((int)unscaledX, (int)unscaledY);
                
                // Check rooms
                for (Map.Entry<Integer, Rectangle> entry : roomRects.entrySet()) {
                    if (entry.getValue().contains(p)) {
                        gameState.setCurrentCamera(entry.getKey());
                        SoundManager.playKeyClick();
                        repaint();
                        return;
                    }
                }
                
                // Check Office
                if (officeRect.contains(p)) {
                    gameState.setCurrentCamera(0); // 0 is Office
                    SoundManager.playKeyClick();
                    repaint();
                }
            }
        });
    }
    
    private void setupLayout() {
        // Custom Layout: 12 Rooms + Office
        // Increased size for easier clicking
        int w = 70; 
        int h = 50; 
        int centerX = 200;
        int centerY = 150;
        
        // Center Hub
        roomRects.put(1, new Rectangle(centerX, centerY, w, h)); // Main Corridor
        
        // Top Path
        roomRects.put(4, new Rectangle(centerX, centerY - 80, w, h)); // Research Lab
        roomRects.put(5, new Rectangle(centerX, centerY - 160, w, h)); // Containment
        
        // Left Wing
        roomRects.put(2, new Rectangle(centerX - 100, centerY, w, h)); // Server Room
        roomRects.put(6, new Rectangle(centerX - 100, centerY + 80, w, h)); // Ventilation
        roomRects.put(8, new Rectangle(centerX - 180, centerY, w, h)); // Archive
        
        // Moved Cam 12 (Observation Deck) above Cam 8 (Archive)
        roomRects.put(12, new Rectangle(centerX - 180, centerY - 80, w, h)); // Observation Deck
        
        // Right Wing
        roomRects.put(3, new Rectangle(centerX + 100, centerY, w, h)); // Cargo Bay
        roomRects.put(7, new Rectangle(centerX + 100, centerY + 80, w, h)); // Maintenance
        roomRects.put(9, new Rectangle(centerX + 180, centerY, w, h)); // Generator Room
        
        // Far Left/Right Extensions
        roomRects.put(10, new Rectangle(centerX - 100, centerY - 80, w, h)); // Med Bay (Left of Research)
        roomRects.put(11, new Rectangle(centerX + 180, centerY - 80, w, h)); // Armory (Right of Research/Above Generator)
        
        // Office (Player)
        officeRect = new Rectangle(centerX, 280, w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int panelW = getWidth();
        int panelH = getHeight();
        
        // Calculate map bounds to determine scale
        // Min/Max X and Y from setupLayout
        // X range: approx 20 - 380 (width ~360)
        // Y range: approx -100 - 330 (height ~430)
        // Let's define a logical bounding box for the map content
        int mapMinX = 0;
        int mapMaxX = 400;
        int mapMinY = -100;
        int mapMaxY = 350;
        
        int mapWidth = mapMaxX - mapMinX;
        int mapHeight = mapMaxY - mapMinY;
        
        // Calculate scale to fit
        double scaleX = (double) (panelW - 40) / mapWidth; // 20px padding
        double scaleY = (double) (panelH - 40) / mapHeight;
        scale = Math.min(scaleX, scaleY);
        
        // Limit max scale to avoid it looking too huge on large screens
        scale = Math.min(scale, 1.5);
        
        // Center the scaled map
        int scaledMapWidth = (int) (mapWidth * scale);
        int scaledMapHeight = (int) (mapHeight * scale);
        
        transX = (panelW - scaledMapWidth) / 2 - (int)(mapMinX * scale);
        transY = (panelH - scaledMapHeight) / 2 - (int)(mapMinY * scale);
        
        // Apply transformations
        g2.translate(transX, transY);
        g2.scale(scale, scale);

        if (!gameState.isCameraSystemActive()) {
            // Reset for full screen fill
            g2.scale(1.0/scale, 1.0/scale);
            g2.translate(-transX, -transY);
            
            g2.setColor(Theme.CRT_BLACK);
            g2.fillRect(0, 0, panelW, panelH);
            g2.setColor(Theme.CRT_GREEN);
            g2.setFont(Theme.RETRO_FONT);
            String msg = "CAMERA SYSTEM OFFLINE";
            int strW = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (panelW - strW) / 2, panelH / 2);
            return;
        }
        
        // Draw Connections (Lines)
        g2.setStroke(new BasicStroke(2));
        
        // 1 -> 4 -> 5 -> 12
        drawConnection(g2, 1, 4);
        drawConnection(g2, 4, 5);
        drawConnection(g2, 5, 12);
        
        // 1 -> 2 -> 6 -> 0
        drawConnection(g2, 1, 2);
        drawConnection(g2, 2, 6);
        drawConnectionToOffice(g2, 6);
        
        // 1 -> 3 -> 7 -> 0
        drawConnection(g2, 1, 3);
        drawConnection(g2, 3, 7);
        drawConnectionToOffice(g2, 7);
        
        // 1 -> 0
        drawConnectionToOffice(g2, 1);
        
        // 2 -> 8 -> 11
        drawConnection(g2, 2, 8);
        drawConnection(g2, 8, 11);
        
        // 3 -> 9 -> 11
        drawConnection(g2, 3, 9);
        drawConnection(g2, 9, 11);
        
        // 4 -> 10 -> 12
        drawConnection(g2, 4, 10);
        drawConnection(g2, 10, 12);
        
        // --- Draw Vents ---
        // Vent 1: Containment (5) <-> Cargo Bay (3)
        drawVent(g2, 5, 3);
        
        // Vent 2: Research Lab (4) <-> Server Room (2)
        drawVent(g2, 4, 2);
        
        // Vent 3: Observation Deck (12) <-> Archive (8)
        drawVent(g2, 12, 8);

        // Draw Rooms
        for (Map.Entry<Integer, Rectangle> entry : roomRects.entrySet()) {
            int id = entry.getKey();
            Rectangle r = entry.getValue();
            
            drawRoom(g2, r, id, "CAM " + id);
        }
        
        // Draw "YOU" (Office)
        drawRoom(g2, officeRect, 0, "YOU");
        
        // Draw Asset Marker (Global visibility)
        int assetLoc = gameState.getAssetLocation();
        if (roomRects.containsKey(assetLoc)) {
            Rectangle r = roomRects.get(assetLoc);
            g2.setColor(Color.CYAN);
            g2.fillOval(r.x + r.width - 15, r.y + 5, 10, 10);
        }
        
        // Reset translation for static overlay
        g2.scale(1.0/scale, 1.0/scale);
        g2.translate(-transX, -transY);
        
        // Static overlay effect
        g2.setColor(new Color(0, 255, 65, 20));
        for (int i = 0; i < panelH; i += 4) {
            g2.drawLine(0, i, panelW, i);
        }
    }
    
    private void drawRoom(Graphics2D g2, Rectangle r, int id, String label) {
        // Highlight selected camera
        if (gameState.getCurrentCamera() == id) {
            g2.setColor(Theme.CRT_AMBER);
            g2.fillRect(r.x, r.y, r.width, r.height);
            g2.setColor(Theme.CRT_BLACK);
        } else {
            g2.setColor(Theme.CRT_BLACK);
            g2.fillRect(r.x, r.y, r.width, r.height);
            g2.setColor(Theme.CRT_GREEN);
        }
        
        g2.drawRect(r.x, r.y, r.width, r.height);
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        
        // Short names
        g2.drawString(label, r.x + 5, r.y + 15);
        
        // Lock Status Icon
        boolean locked = (id == 0) ? gameState.isOfficeDoorLocked() : gameState.isRoomLocked(id);
        if (locked) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 9));
            g2.drawString("[L]", r.x + r.width - 20, r.y + 15);
        }
        
        // Draw Entity
        // Difficulty 3 (Hard): Only visible if on current camera
        // Difficulty 1, 2: Always visible
        boolean visible = true;
        if (gameState.getDifficultyLevel() == 3) {
            visible = (gameState.getCurrentCamera() == id);
        }
        
        if (visible && gameState.getEntityLocation() == id) {
            g2.setColor(Theme.ALERT_RED);
            g2.drawString("! ENTITY !", r.x + 5, r.y + 30);
        }
    }
    
    private void drawConnection(Graphics2D g2, int id1, int id2) {
        Rectangle r1 = roomRects.get(id1);
        Rectangle r2 = roomRects.get(id2);
        
        if (gameState.isRoomLocked(id1) || gameState.isRoomLocked(id2)) {
            g2.setColor(Theme.ALERT_RED);
        } else {
            g2.setColor(Theme.CRT_GREEN);
        }
        
        int x1 = r1.x + r1.width/2;
        int y1 = r1.y + r1.height/2;
        int x2 = r2.x + r2.width/2;
        int y2 = r2.y + r2.height/2;
        
        // Special routing for specific pairs to avoid overlaps
        if ((id1 == 8 && id2 == 11) || (id1 == 11 && id2 == 8)) {
            // U-shape over the top
            int topY = -100; // Above everything
            g2.drawLine(x1, y1, x1, topY);
            g2.drawLine(x1, topY, x2, topY);
            g2.drawLine(x2, topY, x2, y2);
        } else if ((id1 == 10 && id2 == 12) || (id1 == 12 && id2 == 10)) {
            // Vertical first to avoid Room 4
            drawOrthogonal(g2, x1, y1, x2, y2, true);
        } else {
            // Default Horizontal first
            drawOrthogonal(g2, x1, y1, x2, y2, false);
        }
    }
    
    private void drawConnectionToOffice(Graphics2D g2, int id) {
        Rectangle r = roomRects.get(id);
        
        if (gameState.isRoomLocked(id) || gameState.isOfficeDoorLocked()) {
            g2.setColor(Theme.ALERT_RED);
        } else {
            g2.setColor(Theme.CRT_GREEN);
        }
        
        int x1 = r.x + r.width/2;
        int y1 = r.y + r.height/2;
        int x2 = officeRect.x + officeRect.width/2;
        int y2 = officeRect.y + officeRect.height/2;
        
        // Horizontal first (L-shape) works well for 6->0 and 7->0
        drawOrthogonal(g2, x1, y1, x2, y2, false);
    }
    
    private void drawVent(Graphics2D g2, int id1, int id2) {
        Rectangle r1 = roomRects.get(id1);
        Rectangle r2 = roomRects.get(id2);
        
        g2.setColor(Color.GRAY);
        Stroke originalStroke = g2.getStroke();
        
        // Dashed line for vents
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
        
        int x1 = r1.x + r1.width/2;
        int y1 = r1.y + r1.height/2;
        int x2 = r2.x + r2.width/2;
        int y2 = r2.y + r2.height/2;
        
        drawOrthogonal(g2, x1, y1, x2, y2, false);
        
        g2.setStroke(originalStroke);
    }
    
    private void drawOrthogonal(Graphics2D g2, int x1, int y1, int x2, int y2, boolean verticalFirst) {
        if (verticalFirst) {
            g2.drawLine(x1, y1, x1, y2);
            g2.drawLine(x1, y2, x2, y2);
        } else {
            g2.drawLine(x1, y1, x2, y1);
            g2.drawLine(x2, y1, x2, y2);
        }
    }
}
