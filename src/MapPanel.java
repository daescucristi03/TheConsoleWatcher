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
        setBorder(BorderFactory.createLineBorder(Theme.CRT_GREEN));
        
        setupLayout();
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!gameState.isCameraSystemActive()) return;
                
                int clickX = e.getX();
                int clickY = e.getY();
                
                double unscaledX = (clickX - transX) / scale;
                double unscaledY = (clickY - transY) / scale;
                
                Point p = new Point((int)unscaledX, (int)unscaledY);
                
                for (Map.Entry<Integer, Rectangle> entry : roomRects.entrySet()) {
                    if (entry.getValue().contains(p)) {
                        gameState.setCurrentCamera(entry.getKey());
                        SoundManager.playKeyClick();
                        repaint();
                        return;
                    }
                }
                
                if (officeRect.contains(p)) {
                    gameState.setCurrentCamera(0); 
                    SoundManager.playKeyClick();
                    repaint();
                }
            }
        });
    }
    
    private void setupLayout() {
        int w = 60; 
        int h = 40; 
        int cx = 200;
        int cy = 150;
        
        roomRects.put(1, createRect(cx, cy, w, h)); // Main Corridor
        officeRect = createRect(cx, cy + 100, w, h); // Office
        
        roomRects.put(2, createRect(cx - 80, cy, w, h)); // Server Room
        roomRects.put(3, createRect(cx + 80, cy, w, h)); // Cargo Bay
        
        roomRects.put(4, createRect(cx, cy - 70, w, h)); // Research Lab
        roomRects.put(5, createRect(cx, cy - 140, w, h)); // Containment
        
        roomRects.put(6, createRect(cx - 80, cy + 100, w, h)); // Ventilation
        roomRects.put(7, createRect(cx + 80, cy + 100, w, h)); // Maintenance
        
        roomRects.put(8, createRect(cx - 160, cy, w, h)); // Archive
        roomRects.put(9, createRect(cx + 160, cy, w, h)); // Generator
        
        roomRects.put(10, createRect(cx - 80, cy - 70, w, h)); // Med Bay
        roomRects.put(12, createRect(cx - 80, cy - 140, w, h)); // Observation
        
        roomRects.put(11, createRect(cx, cy - 210, w, h)); // Armory
    }
    
    private Rectangle createRect(int cx, int cy, int w, int h) {
        return new Rectangle(cx - w/2, cy - h/2, w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int panelW = getWidth();
        int panelH = getHeight();
        
        int mapMinX = 0;
        int mapMaxX = 400;
        int mapMinY = -80; 
        int mapMaxY = 300; 
        
        int mapWidth = mapMaxX - mapMinX;
        int mapHeight = mapMaxY - mapMinY;
        
        double scaleX = (double) (panelW - 40) / mapWidth; 
        double scaleY = (double) (panelH - 40) / mapHeight;
        scale = Math.min(scaleX, scaleY);
        scale = Math.min(scale, 1.5); 
        
        int scaledMapWidth = (int) (mapWidth * scale);
        int scaledMapHeight = (int) (mapHeight * scale);
        
        transX = (panelW - scaledMapWidth) / 2 - (int)(mapMinX * scale);
        transY = (panelH - scaledMapHeight) / 2 - (int)(mapMinY * scale);
        
        g2.translate(transX, transY);
        g2.scale(scale, scale);

        if (!gameState.isCameraSystemActive()) {
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
        
        drawConnection(g2, 1, 4);
        drawConnection(g2, 4, 5);
        drawConnection(g2, 1, 2);
        drawConnection(g2, 1, 3);
        drawConnection(g2, 2, 8);
        drawConnection(g2, 3, 9);
        drawConnection(g2, 2, 6);
        drawConnection(g2, 3, 7);
        drawConnectionToOffice(g2, 6);
        drawConnectionToOffice(g2, 7);
        drawConnectionToOffice(g2, 1);
        drawConnection(g2, 4, 10);
        drawConnection(g2, 10, 12);
        drawConnection(g2, 5, 12);
        drawConnection(g2, 8, 11);
        drawConnection(g2, 9, 11);
        
        // --- Draw Vents ---
        // Vent 1: Containment (5) <-> Cargo Bay (3)
        // Offset to avoid crossing Main Corridor (1) and Research (4)
        drawVent(g2, 5, 3, 40); 
        
        // Vent 2: Research Lab (4) <-> Server Room (2)
        // Offset to avoid crossing Main Corridor (1)
        drawVent(g2, 4, 2, -30);
        
        // Vent 3: Observation Deck (12) <-> Archive (8)
        // Offset to avoid crossing Med Bay (10) and Server Room (2)
        drawVent(g2, 12, 8, -40);

        // Draw Rooms
        for (Map.Entry<Integer, Rectangle> entry : roomRects.entrySet()) {
            int id = entry.getKey();
            Rectangle r = entry.getValue();
            drawRoom(g2, r, id, "CAM " + id);
        }
        
        drawRoom(g2, officeRect, 0, "YOU");
        
        int assetLoc = gameState.getAssetLocation();
        if (roomRects.containsKey(assetLoc)) {
            Rectangle r = roomRects.get(assetLoc);
            g2.setColor(Color.CYAN);
            g2.fillOval(r.x + r.width - 15, r.y + 5, 10, 10);
        }
        
        g2.scale(1.0/scale, 1.0/scale);
        g2.translate(-transX, -transY);
        
        g2.setColor(new Color(0, 255, 65, 20));
        for (int i = 0; i < panelH; i += 4) {
            g2.drawLine(0, i, panelW, i);
        }
    }
    
    private void drawRoom(Graphics2D g2, Rectangle r, int id, String label) {
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
        g2.drawString(label, r.x + 5, r.y + 15);
        
        boolean locked = (id == 0) ? gameState.isOfficeDoorLocked() : gameState.isRoomLocked(id);
        if (locked) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 9));
            g2.drawString("[L]", r.x + r.width - 20, r.y + 15);
        }
        
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
        
        if (id1 == 11 || id2 == 11) {
            drawOrthogonal(g2, x1, y1, x2, y2, true); 
        } else {
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
        
        drawOrthogonal(g2, x1, y1, x2, y2, false);
    }
    
    private void drawVent(Graphics2D g2, int id1, int id2, int offset) {
        Rectangle r1 = roomRects.get(id1);
        Rectangle r2 = roomRects.get(id2);
        
        g2.setColor(Color.GRAY);
        Stroke originalStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
        
        int x1 = r1.x + r1.width/2;
        int y1 = r1.y + r1.height/2;
        int x2 = r2.x + r2.width/2;
        int y2 = r2.y + r2.height/2;
        
        // Draw with offset to avoid corridors
        // We'll go out horizontally/vertically by 'offset' amount before turning
        
        // Simple logic: Move X or Y by offset, then connect
        // If offset is positive, we might go Right/Down. Negative: Left/Up.
        
        // Strategy: 
        // 1. Move from center of R1 to edge + offset
        // 2. Move parallel to destination
        // 3. Move to center of R2
        
        // Let's try a 3-segment line approach for vents to route around things
        
        if (Math.abs(x1 - x2) > Math.abs(y1 - y2)) {
            // Mostly horizontal separation
            int midY = y1 + offset;
            g2.drawLine(x1, y1, x1, midY);
            g2.drawLine(x1, midY, x2, midY);
            g2.drawLine(x2, midY, x2, y2);
        } else {
            // Mostly vertical separation
            int midX = x1 + offset;
            g2.drawLine(x1, y1, midX, y1);
            g2.drawLine(midX, y1, midX, y2);
            g2.drawLine(midX, y2, x2, y2);
        }
        
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
