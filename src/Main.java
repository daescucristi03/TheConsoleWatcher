import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JumpscarePanel jumpscarePanel;
    private DeathScreenPanel deathScreenPanel;
    private int selectedDifficulty = 2; // Default Normal
    
    // Game Progression
    private int currentNight = 1;
    private final int MAX_NIGHTS = 5;

    public Main() {
        setTitle("The Console Watcher");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        jumpscarePanel = new JumpscarePanel(this);
        deathScreenPanel = new DeathScreenPanel(this);
        
        mainPanel.add(new MainMenuPanel(this), "MENU");
        mainPanel.add(new DifficultyPanel(this), "DIFFICULTY");
        mainPanel.add(new OptionsPanel(this), "OPTIONS");
        mainPanel.add(new TutorialPanel(this), "TUTORIAL");
        mainPanel.add(jumpscarePanel, "JUMPSCARE");
        mainPanel.add(deathScreenPanel, "DEATH");
        mainPanel.add(new WinPanel(this), "WIN");
        
        add(mainPanel);
        
        showMainMenu();
        SoundManager.playStartup();
    }
    
    public void setDifficulty(int diff) {
        this.selectedDifficulty = diff;
    }
    
    public int getDifficulty() {
        return selectedDifficulty;
    }
    
    public int getCurrentNight() {
        return currentNight;
    }

    public void showMainMenu() {
        currentNight = 1; // Reset progress on menu
        cardLayout.show(mainPanel, "MENU");
    }
    
    public void showDifficultySelection() {
        cardLayout.show(mainPanel, "DIFFICULTY");
    }
    
    public void showOptions() {
        cardLayout.show(mainPanel, "OPTIONS");
    }
    
    public void showTutorial() {
        cardLayout.show(mainPanel, "TUTORIAL");
    }
    
    public void showDeathScreen(String cause) {
        deathScreenPanel.setCause(cause);
        cardLayout.show(mainPanel, "DEATH");
    }
    
    public void showDeathScreen() {
        showDeathScreen("CONNECTION TERMINATED");
    }
    
    public void showWinScreen() {
        cardLayout.show(mainPanel, "WIN");
    }

    public void startGame() {
        // Start Night 1 or continue
        startNight();
    }
    
    public void startNight() {
        // Create a new GamePanel for the current night
        mainPanel.add(new GamePanel(this, selectedDifficulty, currentNight), "GAME");
        cardLayout.show(mainPanel, "GAME");
    }
    
    public void completeNight() {
        if (currentNight < MAX_NIGHTS) {
            currentNight++;
            startNight(); // Immediately start next night (GamePanel handles the transition screen)
        } else {
            showWinScreen();
        }
    }
    
    public void triggerGameOver() {
        cardLayout.show(mainPanel, "JUMPSCARE");
        jumpscarePanel.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main game = new Main();
            game.setVisible(true);
        });
    }
}
