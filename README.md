# The Console Watcher

**The Console Watcher** is a retro-style horror strategy game built with Java Swing. You play as a security operator tasked with surviving the night in a mysterious facility while protecting a valuable asset from a hostile entity.

## 🎮 Gameplay Features

*   **Retro CRT Aesthetic**: Immerse yourself in a high-contrast, text-based interface reminiscent of old-school terminals.
*   **Surveillance System**: Monitor 12 different rooms using a schematic map. Track the entity's movement and keep an eye on the asset.
*   **Strategic Defense**:
    *   **Lock Doors**: Secure remote rooms to block the entity's path. You can only lock one remote room at a time.
    *   **Office Security**: Lock your own office door to prevent the entity from breaching your location.
*   **Asset Protection**: Guide the asset to safety by issuing move commands. Be careful—if the asset runs into the entity, the mission fails.
*   **Power Management**: Every action drains power. If you run out, the systems fail, and you are defenseless.
*   **Dynamic AI**: The entity becomes more aggressive on higher difficulties and can use vents to bypass corridors.

## 🕹️ Controls & Commands

The game is played using a combination of mouse clicks and text commands.

### Mouse
*   **Map Interaction**: Click on rooms in the map view to switch cameras. Click on "YOU" to view your office status.
*   **Buttons**: Use the on-screen buttons for quick actions like locking doors or checking status.

### Text Commands
Type these into the command input field:

*   `cameras`: Toggle the map/camera view.
*   `lock door` / `unlock door`: Lock or unlock the main office door.
*   `lock room` / `unlock room`: Lock or unlock the currently viewed remote room.
*   `move asset [room_id]`: Order the asset to move to a specific room (e.g., `move asset 12`).
*   `check power`: Display current power level.
*   `status`: Show system status overview.
*   `clear`: Clear the text log.
*   `quit`: Return to the main menu.

## ⚠️ Difficulty Levels

*   **EASY**: The entity moves slower and is less aggressive. Power drains at a normal rate.
*   **NORMAL**: Standard challenge. The entity is smarter and faster. Power drains faster.
*   **HARD**: The ultimate test. The entity moves constantly and is **only visible on the map if you are looking at its current room**. Power drains rapidly.

## 🛠️ Installation & Running

### Prerequisites
*   Java Development Kit (JDK) 8 or higher.

### How to Run
1.  Compile the Java files:
    ```bash
    javac src/*.java
    ```
2.  Run the main class:
    ```bash
    java -cp src Main
    ```

## 📝 License
This project is a personal creation for educational and entertainment purposes.

---
*System Boot Sequence Complete. Good luck, Operator.*
