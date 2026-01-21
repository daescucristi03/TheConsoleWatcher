import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {

    private JTextPane logArea; 
    private JTextField inputField;
    private MonitorPanel monitorPanel;
    private MapPanel mapPanel;
    private JPanel commandPanel;
    private GameState gameState;
    private Timer gameLoopTimer;
    private Main mainFrame;
    
    // Layout components for switching views
    private JPanel centerContainer;
    private JPanel viewPanel;
    private CardLayout viewLayout;
    private JLabel lastCmdLabel;
    
    // To store the last command for map view
    private String lastCommandText = "";
    
    // Timer for the 5-second grace period
    private Timer breachTimer;
    private boolean breachPending = false;
    
    // Timer for the 2-second kill delay
    private Timer killTimer;
    private boolean killPending = false; 
    private String pendingKillCause = ""; 
    
    // Asset Movement Animation
    private Timer assetMoveTimer;
    private List<Integer> assetMovePath;
    private int assetMoveIndex;
    
    // Pause State
    private boolean isPaused = false;
    private JButton pauseButton;
    
    // Night Progression
    private int currentNight;
    private int currentHour = 12; // 12 AM
    private int currentMinute = 0;
    private Timer timeTimer; // Renamed from hourTimer
    private boolean nightTransition = false;

    public GamePanel(Main mainFrame, int difficulty, int night) {
        this.mainFrame = mainFrame;
        this.currentNight = night;
        this.gameState = new GameState(difficulty); 
        
        setLayout(new BorderLayout());
        setBackground(Theme.CRT_BLACK);

        // --- Center: The Log & Map ---
        centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(Theme.CRT_BLACK);
        
        // View Panel (Cards)
        viewLayout = new CardLayout();
        viewPanel = new JPanel(viewLayout);
        viewPanel.setBackground(Theme.CRT_BLACK);
        
        // Log View
        logArea = new JTextPane();
        logArea.setEditable(false);
        logArea.setBackground(Theme.CRT_BLACK);
        logArea.setForeground(Theme.CRT_GREEN);
        logArea.setFont(Theme.RETRO_FONT);
        
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.CRT_GREEN));
        scrollPane.getViewport().setBackground(Theme.CRT_BLACK);
        
        // Map View
        mapPanel = new MapPanel(gameState);
        
        // Night Transition Panel
        JPanel transitionPanel = new JPanel(new GridBagLayout());
        transitionPanel.setBackground(Theme.CRT_BLACK);
        JLabel nightLabel = new JLabel("NIGHT " + currentNight);
        nightLabel.setFont(new Font("Monospaced", Font.BOLD, 48));
        nightLabel.setForeground(Theme.CRT_GREEN);
        transitionPanel.add(nightLabel);
        
        viewPanel.add(transitionPanel, "TRANSITION");
        viewPanel.add(scrollPane, "LOG");
        viewPanel.add(mapPanel, "MAP");
        
        // Last Command Label (Hidden by default)
        lastCmdLabel = new JLabel();
        lastCmdLabel.setForeground(Theme.CRT_GREEN);
        lastCmdLabel.setFont(Theme.RETRO_FONT);
        lastCmdLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lastCmdLabel.setVisible(false);
        
        centerContainer.add(lastCmdLabel, BorderLayout.NORTH);
        centerContainer.add(viewPanel, BorderLayout.CENTER);
        
        add(centerContainer, BorderLayout.CENTER);

        // --- Right: The Monitor ---
        monitorPanel = new MonitorPanel(gameState);
        add(monitorPanel, BorderLayout.EAST);

        // --- Bottom: Input Field & Context Commands ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Theme.CRT_BLACK);
        
        commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        commandPanel.setBackground(Theme.CRT_BLACK);
        commandPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Add Pause Button to Command Panel
        pauseButton = new JButton("PAUSE");
        styleButton(pauseButton);
        pauseButton.addActionListener(e -> togglePause());
        commandPanel.add(pauseButton);
        
        updateCommandButtons(); // Initial buttons
        
        inputField = new JTextField();
        inputField.setBackground(Theme.CRT_BLACK);
        inputField.setForeground(Theme.CRT_GREEN);
        inputField.setFont(Theme.RETRO_FONT);
        inputField.setCaretColor(Theme.CRT_GREEN);
        inputField.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.CRT_GREEN), "COMMAND INPUT", 
                0, 0, Theme.RETRO_FONT, Theme.CRT_GREEN));
        
        inputField.addActionListener(e -> processCommand(inputField.getText()));
        
        bottomPanel.add(commandPanel, BorderLayout.NORTH);
        bottomPanel.add(inputField, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Game Loop Timer ---
        int moveInterval = 5000; // Easy
        if (difficulty == 2) moveInterval = 4000; // Normal
        if (difficulty == 3) moveInterval = 3000; // Hard
        
        gameLoopTimer = new Timer(moveInterval, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameTick();
            }
        });
        
        // --- Time Timer (Updates every ~1 second real time to simulate minutes) ---
        // 1 hour = 60 seconds real time.
        // 60 minutes = 60 seconds -> 1 minute = 1 second.
        timeTimer = new Timer(1000, e -> advanceTime());
        
        // Start Sequence
        startNightSequence();
    }
    
    private void startNightSequence() {
        // Show "NIGHT X" screen
        viewLayout.show(viewPanel, "TRANSITION");
        inputField.setEnabled(false);
        commandPanel.setVisible(false);
        
        Timer startTimer = new Timer(3000, e -> {
            viewLayout.show(viewPanel, "LOG");
            inputField.setEnabled(true);
            commandPanel.setVisible(true);
            
            // Start Game Logic
            gameLoopTimer.start();
            timeTimer.start();
            
            // Initial Messages
            log("SYSTEM BOOT SEQUENCE INITIATED...", Theme.CRT_GREEN);
            log("NIGHT " + currentNight + " STARTED.", Theme.CRT_GREEN);
            log("TIME: 12:00 AM", Theme.CRT_GREEN);
            log("CONNECTION ESTABLISHED.", Theme.CRT_GREEN);
            log("WARNING: UNKNOWN ENTITY DETECTED IN CONTAINMENT.", Theme.CRT_AMBER);
            log("MISSION: PROTECT THE ASSET. MOVE IT TO SAFETY.", Theme.CRT_GREEN);
            SoundManager.playStartup();
        });
        startTimer.setRepeats(false);
        startTimer.start();
    }
    
    private void advanceTime() {
        currentMinute++;
        if (currentMinute >= 60) {
            currentMinute = 0;
            currentHour++;
            if (currentHour > 12) currentHour = 1;
            
            // Log hour change
            String timeStr = String.format("%d:00 AM", currentHour);
            log("TIME UPDATE: " + timeStr, Theme.CRT_GREEN);
        }
        
        gameState.setCurrentHour(currentHour);
        gameState.setCurrentMinute(currentMinute);
        monitorPanel.repaint(); // Update monitor to show new time
        
        if (currentHour == 6 && currentMinute == 0) {
            endNight();
        }
    }
    
    private void endNight() {
        gameLoopTimer.stop();
        timeTimer.stop();
        inputField.setEnabled(false);
        commandPanel.setVisible(false);
        
        // Show 6:00 AM screen
        JPanel transitionPanel = (JPanel) viewPanel.getComponent(0); // Assuming index 0 is TRANSITION
        JLabel label = (JLabel) transitionPanel.getComponent(0);
        label.setText("6:00 AM");
        viewLayout.show(viewPanel, "TRANSITION");
        
        Timer endTimer = new Timer(3000, e -> {
            mainFrame.completeNight();
        });
        endTimer.setRepeats(false);
        endTimer.start();
    }
    
    private String getDifficultyString(int diff) {
        switch (diff) {
            case 1: return "EASY";
            case 2: return "NORMAL";
            case 3: return "HARD";
            default: return "UNKNOWN";
        }
    }
    
    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            gameLoopTimer.stop();
            timeTimer.stop();
            if (breachTimer != null && breachTimer.isRunning()) breachTimer.stop();
            if (killTimer != null && killTimer.isRunning()) killTimer.stop(); 
            if (assetMoveTimer != null && assetMoveTimer.isRunning()) assetMoveTimer.stop(); 
            log("SYSTEM PAUSED.", Theme.CRT_AMBER);
            inputField.setEnabled(false);
            pauseButton.setText("RESUME");
            pauseButton.setForeground(Theme.CRT_AMBER);
        } else {
            gameLoopTimer.start();
            timeTimer.start();
            if (breachPending && breachTimer != null) breachTimer.start();
            if (killPending && killTimer != null) killTimer.start(); 
            if (assetMoveTimer != null && assetMovePath != null) assetMoveTimer.start(); 
            log("SYSTEM RESUMED.", Theme.CRT_GREEN);
            inputField.setEnabled(true);
            pauseButton.setText("PAUSE");
            pauseButton.setForeground(Theme.CRT_GREEN);
        }
    }

    private void log(String message, Color color) {
        lastCommandText = message;
        
        StyledDocument doc = logArea.getStyledDocument();
        Style style = logArea.addStyle("Style", null);
        StyleConstants.setForeground(style, color);
        
        try {
            doc.insertString(doc.getLength(), "> " + message + "\n", style);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (gameState.isCameraSystemActive()) {
             lastCmdLabel.setText("> " + lastCommandText);
        }
    }
    
    private void log(String message) {
        log(message, Theme.CRT_GREEN);
    }
    
    private void updateCommandButtons() {
        // Keep Pause Button
        commandPanel.removeAll();
        commandPanel.add(pauseButton);
        
        addCommandButton("CAMERAS", "cameras");
        addCommandButton("STATUS", "status");
        
        if (gameState.isCameraSystemActive()) {
            int cam = gameState.getCurrentCamera();
            if (cam == 0) { 
                 if (gameState.isOfficeDoorLocked()) {
                    addCommandButton("UNLOCK DOOR", "unlock door");
                } else {
                    addCommandButton("LOCK DOOR", "lock door");
                }
            } else {
                if (gameState.isRoomLocked(cam)) {
                    addCommandButton("UNLOCK ROOM", "unlock room");
                } else {
                    addCommandButton("LOCK ROOM", "lock room");
                }
                // Add Move Asset Button always when in camera view (except office)
                addCommandButton("MOVE ASSET", "move asset");
            }
        } else {
            if (gameState.isOfficeDoorLocked()) {
                addCommandButton("UNLOCK DOOR", "unlock door");
            } else {
                addCommandButton("LOCK DOOR", "lock door");
            }
            addCommandButton("CHECK POWER", "check power");
        }
        
        addCommandButton("CLEAR LOG", "clear");
        addCommandButton("QUIT", "quit");
        
        commandPanel.revalidate();
        commandPanel.repaint();
    }
    
    private void styleButton(JButton btn) {
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setForeground(Theme.CRT_GREEN);
        btn.setBackground(Theme.CRT_BLACK);
        btn.setBorder(BorderFactory.createLineBorder(Theme.CRT_GREEN));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setMargin(new Insets(2, 5, 2, 5));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) {
                    btn.setBackground(Theme.CRT_GREEN);
                    btn.setForeground(Theme.CRT_BLACK);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) {
                    btn.setBackground(Theme.CRT_BLACK);
                    // Special case for pause button state color
                    if (btn == pauseButton && isPaused) btn.setForeground(Theme.CRT_AMBER);
                    else btn.setForeground(Theme.CRT_GREEN);
                }
            }
        });
    }
    
    private void addCommandButton(String label, String command) {
        JButton btn = new JButton(label);
        styleButton(btn);
        btn.addActionListener(e -> processCommand(command));
        commandPanel.add(btn);
    }

    private void processCommand(String command) {
        if (isPaused) return; // Ignore commands while paused
        
        inputField.setText("");
        command = command.trim().toLowerCase();
        
        // Handle "move asset" special case (needs argument usually, but we can do context)
        if (command.equals("move asset")) {
            log("USAGE: move asset [room_id]", Theme.CRT_AMBER);
            return;
        }
        
        if (command.startsWith("move asset ")) {
            try {
                int targetRoom = Integer.parseInt(command.substring(11).trim());
                
                // Find path
                List<Integer> path = gameState.findPath(gameState.getAssetLocation(), targetRoom);
                
                if (path != null && !path.isEmpty()) {
                    log("INITIATING ASSET TRANSFER TO ROOM " + targetRoom + "...", Theme.CRT_GREEN);
                    SoundManager.playKeyClick();
                    startAssetMoveAnimation(path);
                } else {
                    log("CANNOT MOVE ASSET THERE (BLOCKED/INVALID).", Theme.ALERT_RED);
                }
            } catch (NumberFormatException e) {
                log("INVALID ROOM ID.", Theme.ALERT_RED);
            }
            monitorPanel.repaint();
            mapPanel.repaint();
            return;
        }
        
        log(command.toUpperCase(), Theme.CRT_GREEN);
        SoundManager.playKeyClick();

        switch (command) {
            case "help":
                log("COMMANDS: lock/unlock door/room, move asset [id], check power, status, cameras, clear, quit");
                break;
            case "lock door":
                if (!gameState.isOfficeDoorLocked()) {
                    gameState.setOfficeDoorLocked(true);
                    log("OFFICE DOOR SECURED.");
                    SoundManager.playKeyClick();
                    checkBreachResolution(); // Check if this saves the player
                } else {
                    log("OFFICE DOOR IS ALREADY LOCKED.");
                }
                break;
            case "unlock door":
                if (gameState.isOfficeDoorLocked()) {
                    gameState.setOfficeDoorLocked(false);
                    log("WARNING: OFFICE DOOR UNLOCKED.", Theme.CRT_AMBER);
                    SoundManager.playAlarm();
                } else {
                    log("OFFICE DOOR IS ALREADY OPEN.");
                }
                break;
            case "lock room":
                if (gameState.isCameraSystemActive()) {
                    int cam = gameState.getCurrentCamera();
                    if (cam == 0) { 
                         processCommand("lock door");
                         return;
                    }
                    
                    // Check if another room is already locked
                    if (gameState.getLockedRoomCount() > 0 && !gameState.isRoomLocked(cam)) {
                        log("ERROR: ONLY ONE ROOM CAN BE LOCKED AT A TIME.", Theme.ALERT_RED);
                        return;
                    }
                    
                    if (!gameState.isRoomLocked(cam)) {
                        gameState.setRoomLocked(cam, true);
                        log("ROOM " + cam + " LOCKED.");
                        SoundManager.playKeyClick();
                    } else {
                        log("ROOM " + cam + " IS ALREADY LOCKED.");
                    }
                } else {
                    log("ERROR: CAMERA SYSTEM MUST BE ACTIVE.", Theme.ALERT_RED);
                }
                break;
            case "unlock room":
                if (gameState.isCameraSystemActive()) {
                    int cam = gameState.getCurrentCamera();
                    if (cam == 0) { 
                         processCommand("unlock door");
                         return;
                    }
                    
                    if (gameState.isRoomLocked(cam)) {
                        gameState.setRoomLocked(cam, false);
                        log("ROOM " + cam + " UNLOCKED.", Theme.CRT_AMBER);
                    } else {
                        log("ROOM " + cam + " IS ALREADY UNLOCKED.");
                    }
                } else {
                    log("ERROR: CAMERA SYSTEM MUST BE ACTIVE.", Theme.ALERT_RED);
                }
                break;
            case "check power":
                log("POWER LEVEL: " + gameState.getPowerLevel() + "%");
                break;
            case "status":
                log("OFFICE DOOR: " + (gameState.isOfficeDoorLocked() ? "SECURED" : "OPEN"));
                log("CURRENT VIEW: " + (gameState.isCameraSystemActive() ? gameState.getRoomName(gameState.getCurrentCamera()) : "OFFICE"));
                log("ASSET LOCATION: ROOM " + gameState.getAssetLocation(), Theme.CRT_AMBER);
                break;
            case "cameras":
                boolean active = !gameState.isCameraSystemActive();
                gameState.setCameraSystemActive(active);
                
                if (active) {
                    viewLayout.show(viewPanel, "MAP");
                    lastCmdLabel.setText("> " + lastCommandText);
                    lastCmdLabel.setVisible(true);
                    SoundManager.playStatic(200);
                } else {
                    viewLayout.show(viewPanel, "LOG");
                    lastCmdLabel.setVisible(false);
                }
                
                log("CAMERA SYSTEM " + (active ? "ONLINE" : "OFFLINE"));
                break;
            case "clear":
                logArea.setText("");
                break;
            case "quit":
                gameLoopTimer.stop();
                mainFrame.showMainMenu();
                break;
            default:
                log("UNKNOWN COMMAND.", Theme.ALERT_RED);
                break;
        }
        
        updateCommandButtons(); 
        monitorPanel.repaint();
        mapPanel.repaint();
    }

    private void startAssetMoveAnimation(List<Integer> path) {
        assetMovePath = path;
        assetMoveIndex = 0;
        gameState.setAssetMoving(true);

        // Disable input during move
        inputField.setEnabled(false);
        gameLoopTimer.stop(); // Pause game loop so entity doesn't move

        assetMoveTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (assetMoveIndex < assetMovePath.size()) {
                    int nextRoom = assetMovePath.get(assetMoveIndex);
                    gameState.setAssetLocation(nextRoom);

                    // Check if ran into entity
                    if (gameState.isAssetDead()) {
                        assetMoveTimer.stop();
                        log("CRITICAL ERROR: CONTACT WITH ASSET LOST.", Theme.ALERT_RED);
                        SoundManager.playAlarm();
                        pendingKillCause = "asset";
                        startKillTimer(2000);
                        return;
                    }

                    SoundManager.playKeyClick(); // Beep for each step
                    mapPanel.repaint();
                    monitorPanel.repaint();
                    assetMoveIndex++;
                } else {
                    // Done
                    assetMoveTimer.stop();
                    gameState.setAssetMoving(false);
                    inputField.setEnabled(true);
                    gameLoopTimer.start(); // Resume game loop
                    log("ASSET TRANSFER COMPLETE.", Theme.CRT_GREEN);
                }
            }
        });
        assetMoveTimer.start();
    }
    
    private void checkBreachResolution() {
        if (breachPending && gameState.isOfficeDoorLocked()) {
            // Player saved themselves!
            breachPending = false;
            if (breachTimer != null) breachTimer.stop();
            
            log("ENTITY REPELLED AT THE LAST SECOND.", Theme.CRT_GREEN);
            SoundManager.playAlarm(); // Or a relief sound?
            gameState.resetEntity();
        }
    }

    private void gameTick() {
        if (isPaused || killPending) return; // Don't tick normal logic if paused or kill is pending
        if (breachPending) return; // Don't tick normal logic if breach is pending
        
        // Power drain based on difficulty
        int drain = 0;
        int diff = gameState.getDifficultyLevel();
        
        if (gameState.isOfficeDoorLocked()) drain += 1;
        drain += gameState.getLockedRoomCount();
        if (gameState.isCameraSystemActive()) drain += 1;
        
        // Apply difficulty multiplier to drain
        // Easy: x1, Normal: x1.5, Hard: x2
        double drainMultiplier = 1.0;
        if (diff == 2) drainMultiplier = 1.5;
        if (diff == 3) drainMultiplier = 2.0;
        
        drain = (int) Math.ceil(drain * drainMultiplier);
        
        if (gameState.getPowerLevel() > 0) {
            gameState.decreasePower(drain);
        } else {
            log("CRITICAL FAILURE: POWER DEPLETED.", Theme.ALERT_RED);
            SoundManager.playStatic(500);
            gameLoopTimer.stop();
            inputField.setEnabled(false);
            commandPanel.setVisible(false); 
            
            // Start kill timer for power out death
            pendingKillCause = "power";
            startKillTimer(2000);
            return;
        }

        boolean watched = gameState.isCameraSystemActive() && 
                          (gameState.getCurrentCamera() == gameState.getEntityLocation());
        
        if (!watched) {
             gameState.moveEntity();
        } else {
            if (gameState.getRandom().nextInt(10) > 8) {
                gameState.moveEntity();
                log("WARNING: ENTITY IGNORED SURVEILLANCE.", Theme.CRT_AMBER);
            }
        }
        
        // Check for attempted breach (Entity banging on door)
        if (gameState.attemptBreach()) {
            log("LOUD BANGING ON THE DOOR!", Theme.ALERT_RED);
            SoundManager.playAlarm();
            gameState.resetEntity(); // It leaves after failing to enter
            log("ENTITY REPELLED.", Theme.CRT_GREEN);
        }
        
        // Check Asset Death
        if (gameState.isAssetDead()) {
            log("CRITICAL ERROR: CONTACT WITH ASSET LOST.", Theme.ALERT_RED);
            SoundManager.playAlarm(); // Play alarm for asset breach too
            
            pendingKillCause = "asset";
            startKillTimer(2000);
            return;
        }
        
        // If entity reaches 0 (The Office)
        if (gameState.getEntityLocation() == 0) {
             if (gameState.isOfficeDoorLocked()) {
                 log("LOUD BANGING ON THE DOOR!", Theme.ALERT_RED);
                 SoundManager.playAlarm();
                 gameState.resetEntity(); 
                 log("ENTITY REPELLED.", Theme.CRT_GREEN);
             } else {
                 // BREACH IMMINENT - Start 5 second timer
                 log("!!! PROXIMITY ALERT - BREACH IMMINENT !!!", Theme.ALERT_RED);
                 log("LOCK THE DOOR IMMEDIATELY!", Theme.ALERT_RED);
                 SoundManager.playAlarm();
                 
                 breachPending = true;
                 breachTimer = new Timer(5000, e -> {
                     if (!gameState.isOfficeDoorLocked()) {
                         // 5 seconds passed, door still unlocked, player is dead.
                         log("ENTITY HAS BREACHED YOUR LOCATION!", Theme.ALERT_RED);
                         SoundManager.playScream(); // Jumpscare sound
                         pendingKillCause = "player";
                         startKillTimer(2000);
                     } else {
                         // Player locked the door in time, breach resolved.
                         breachPending = false; // Reset flag
                     }
                 });
                 breachTimer.setRepeats(false);
                 breachTimer.start();
             }
        }

        monitorPanel.repaint();
        mapPanel.repaint();
    }
    
    private void startKillTimer(int delay) {
        if (killPending) return; // Already in kill sequence
        
        killPending = true;
        gameLoopTimer.stop(); // Stop main game loop
        inputField.setEnabled(false);
        commandPanel.setVisible(false);
        
        killTimer = new Timer(delay, e -> {
            if (pendingKillCause.equals("player")) {
                mainFrame.triggerGameOver(); // Jumpscare then death screen
            } else if (pendingKillCause.equals("asset")) {
                mainFrame.showDeathScreen("THE ASSET HAS BEEN COMPROMISED"); // Direct to death screen
            } else if (pendingKillCause.equals("power")) {
                mainFrame.showDeathScreen("POWER FAILURE"); // Direct to death screen
            }
        });
        killTimer.setRepeats(false);
        killTimer.start();
    }
}
