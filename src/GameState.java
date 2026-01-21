import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class GameState {
    private boolean isOfficeDoorLocked = true; // Main office door
    private int powerLevel = 100;
    private Random random = new Random();
    
    // Difficulty Settings
    private int difficultyLevel = 1; // 1: Easy, 2: Normal, 3: Hard
    
    // Custom Facility Layout IDs (12 Rooms)
    // 0: Security Office (The Player)
    // 1: Main Corridor
    // 2: Server Room
    // 3: Cargo Bay
    // 4: Research Lab
    // 5: Containment Unit
    // 6: Ventilation Shaft
    // 7: Maintenance Tunnel
    // 8: Archive
    // 9: Generator Room
    // 10: Med Bay
    // 11: Armory
    // 12: Observation Deck
    
    private int currentCamera = 1; 
    private int entityLocation = 5; // Starts in Containment
    private int assetLocation = 2; // Starts in Server Room
    private boolean isCameraSystemActive = false;
    private boolean isAssetMoving = false; // Flag to check if asset is currently moving
    
    // Adjacency Map for movement
    private Map<Integer, List<Integer>> mapConnections = new HashMap<>();
    
    // Vent Connections (Special paths)
    private Map<Integer, List<Integer>> ventConnections = new HashMap<>();
    
    // Locked Room (Only one remote room can be locked at a time)
    private int currentlyLockedRoomId = -1; // -1 means no remote room is locked

    public GameState(int difficulty) {
        this.difficultyLevel = difficulty;
        setupMap();
    }
    
    // Default constructor for compatibility if needed
    public GameState() {
        this(2); // Default to Normal
    }

    private void setupMap() {
        // 0: Office <-> 1: Main Corridor
        connect(0, 1);
        
        // 1: Main Corridor <-> 2: Server Room, 3: Cargo Bay, 4: Research Lab
        connect(1, 2);
        connect(1, 3);
        connect(1, 4);
        
        // 2: Server Room <-> 6: Ventilation Shaft, 8: Archive
        connect(2, 6);
        connect(2, 8);
        
        // 3: Cargo Bay <-> 7: Maintenance Tunnel, 9: Generator Room
        connect(3, 7);
        connect(3, 9);
        
        // 4: Research Lab <-> 5: Containment Unit, 10: Med Bay
        connect(4, 5);
        connect(4, 10);
        
        // 5: Containment Unit <-> 12: Observation Deck
        connect(5, 12);
        
        // 6: Ventilation Shaft <-> 0: Office (Sneaky path!)
        connect(6, 0);
        
        // 7: Maintenance Tunnel <-> 0: Office (Sneaky path!)
        connect(7, 0);
        
        // 8: Archive <-> 11: Armory
        connect(8, 11);
        
        // 9: Generator Room <-> 11: Armory (Loop)
        connect(9, 11);
        
        // 10: Med Bay <-> 12: Observation Deck
        connect(10, 12);
        
        // --- VENTS ---
        // Vent 1: Containment (5) <-> Cargo Bay (3)
        connectVent(5, 3);
        
        // Vent 2: Research Lab (4) <-> Server Room (2)
        connectVent(4, 2);
        
        // Vent 3: Observation Deck (12) <-> Archive (8)
        connectVent(12, 8);
    }
    
    private void connect(int a, int b) {
        mapConnections.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
        mapConnections.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
    }
    
    private void connectVent(int a, int b) {
        ventConnections.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
        ventConnections.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
    }

    public boolean isOfficeDoorLocked() {
        return isOfficeDoorLocked;
    }

    public void setOfficeDoorLocked(boolean doorLocked) {
        isOfficeDoorLocked = doorLocked;
    }
    
    public boolean isRoomLocked(int roomId) {
        return currentlyLockedRoomId == roomId;
    }
    
    public void setRoomLocked(int roomId, boolean locked) {
        if (locked) {
            currentlyLockedRoomId = roomId;
        } else {
            if (currentlyLockedRoomId == roomId) {
                currentlyLockedRoomId = -1;
            }
        }
    }
    
    public int getLockedRoomCount() {
        return (currentlyLockedRoomId != -1) ? 1 : 0;
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public void decreasePower(int amount) {
        this.powerLevel = Math.max(0, this.powerLevel - amount);
    }

    public boolean isEntityNear() {
        // Entity is near if in adjacent rooms to 0 (1, 6, 7)
        return entityLocation == 1 || entityLocation == 6 || entityLocation == 7;
    }

    public Random getRandom() {
        return random;
    }
    
    public int getCurrentCamera() {
        return currentCamera;
    }
    
    public void setCurrentCamera(int cam) {
        if (cam >= 1 && cam <= 12) {
            this.currentCamera = cam;
        }
    }
    
    public int getEntityLocation() {
        return entityLocation;
    }
    
    public void moveEntity() {
        // If asset is moving, entity pauses
        if (isAssetMoving) return;
        
        List<Integer> neighbors = mapConnections.get(entityLocation);
        List<Integer> vents = ventConnections.get(entityLocation);
        
        List<Integer> allPossibleMoves = new ArrayList<>();
        if (neighbors != null) allPossibleMoves.addAll(neighbors);
        if (vents != null) allPossibleMoves.addAll(vents);
        
        if (!allPossibleMoves.isEmpty()) {
            // Aggression based on difficulty
            double moveChance = 0.7; // Easy
            if (difficultyLevel == 2) moveChance = 0.9;
            if (difficultyLevel == 3) moveChance = 1.0;
            
            if (random.nextDouble() < moveChance) {
                // Try to find a valid move
                List<Integer> validMoves = new ArrayList<>();
                for (int neighbor : allPossibleMoves) {
                    // Check if the door to that room is locked
                    boolean isBlocked = false;
                    if (neighbor == 0) {
                        if (isOfficeDoorLocked) isBlocked = true;
                    } else {
                        if (isRoomLocked(neighbor)) isBlocked = true;
                    }
                    
                    if (!isBlocked) {
                        validMoves.add(neighbor);
                    }
                }
                
                if (!validMoves.isEmpty()) {
                    int next = validMoves.get(random.nextInt(validMoves.size()));
                    
                    // Stronger Bias towards Office on higher difficulties
                    if (validMoves.contains(0)) {
                        double officeBias = 0.5;
                        if (difficultyLevel == 2) officeBias = 0.8;
                        if (difficultyLevel == 3) officeBias = 1.0;
                        
                        if (random.nextDouble() < officeBias) {
                            next = 0;
                        }
                    }
                    
                    entityLocation = next;
                }
            }
        }
    }
    
    // Helper to check if entity attempted to breach
    public boolean attemptBreach() {
        if (isEntityNear() && isOfficeDoorLocked) {
            double breachChance = 0.2 * difficultyLevel;
            return random.nextDouble() < breachChance;
        }
        return false;
    }
    
    public void resetEntity() {
        entityLocation = 5; // Back to Containment
    }
    
    public int getAssetLocation() {
        return assetLocation;
    }
    
    // BFS to find path for asset
    public boolean moveAsset(int targetRoom) {
        // Asset cannot move to 0 (Office) for safety reasons? Or maybe it can?
        // Let's assume asset stays in facility.
        if (targetRoom == 0) return false;
        
        // Find path using BFS
        List<Integer> path = findPath(assetLocation, targetRoom);
        
        if (path == null || path.isEmpty()) {
            return false; // No valid path (blocked by locks or disconnected)
        }
        
        // Check if entity is on the path
        for (int room : path) {
            if (room == entityLocation) {
                // Entity encountered! Game Over logic handled by caller checking isAssetDead
                // But we need to move asset to that room to trigger the death condition visually/logically
                assetLocation = room; 
                return true; // Move successful (but fatal)
            }
        }
        
        // Move asset to target
        assetLocation = targetRoom;
        return true;
    }
    
    private List<Integer> findPath(int start, int end) {
        if (start == end) return new ArrayList<>();
        
        Queue<Integer> queue = new LinkedList<>();
        Map<Integer, Integer> parentMap = new HashMap<>();
        Set<Integer> visited = new HashSet<>();
        
        queue.add(start);
        visited.add(start);
        parentMap.put(start, null);
        
        boolean found = false;
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (current == end) {
                found = true;
                break;
            }
            
            List<Integer> neighbors = mapConnections.get(current);
            if (neighbors != null) {
                for (int neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        // Check if locked
                        boolean locked = (neighbor == 0) ? isOfficeDoorLocked : isRoomLocked(neighbor);
                        if (!locked) {
                            visited.add(neighbor);
                            parentMap.put(neighbor, current);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
        
        if (found) {
            List<Integer> path = new ArrayList<>();
            Integer curr = end;
            while (curr != null) {
                path.add(curr);
                curr = parentMap.get(curr);
            }
            Collections.reverse(path);
            // Remove start node from path as we are already there
            if (!path.isEmpty()) path.remove(0);
            return path;
        }
        
        return null;
    }
    
    public void setAssetMoving(boolean moving) {
        this.isAssetMoving = moving;
    }
    
    public boolean isAssetDead() {
        return entityLocation == assetLocation && entityLocation != 0;
    }
    
    public boolean isCameraSystemActive() {
        return isCameraSystemActive;
    }
    
    public void setCameraSystemActive(boolean active) {
        isCameraSystemActive = active;
    }
    
    public String getRoomName(int id) {
        switch(id) {
            case 1: return "MAIN CORRIDOR";
            case 2: return "SERVER ROOM";
            case 3: return "CARGO BAY";
            case 4: return "RESEARCH LAB";
            case 5: return "CONTAINMENT";
            case 6: return "VENTILATION";
            case 7: return "MAINTENANCE";
            case 8: return "ARCHIVE";
            case 9: return "GENERATOR";
            case 10: return "MED BAY";
            case 11: return "ARMORY";
            case 12: return "OBSERVATION";
            case 0: return "SECURITY OFFICE";
            default: return "UNKNOWN";
        }
    }
    
    public boolean hasVentConnection(int a, int b) {
        List<Integer> vents = ventConnections.get(a);
        return vents != null && vents.contains(b);
    }
    
    public int getDifficultyLevel() {
        return difficultyLevel;
    }
}
