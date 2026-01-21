import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JumpscarePanel jumpscarePanel;
    private DeathScreenPanel deathScreenPanel;
    private int selectedDifficulty = 2; // Default Normal

    public Main() {
        setTitle("The Console Watcher");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        jumpscarePanel = new JumpscarePanel(this);
        deathScreenPanel = new DeathScreenPanel(this);
        
        mainPanel.add(new MainMenuPanel(this), "MENU");
        mainPanel.add(new DifficultyPanel(this), "DIFFICULTY"); // Add difficulty panel
        mainPanel.add(new OptionsPanel(this), "OPTIONS");
        mainPanel.add(new TutorialPanel(this), "TUTORIAL");
        mainPanel.add(jumpscarePanel, "JUMPSCARE");
        mainPanel.add(deathScreenPanel, "DEATH");
        
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

    public void showMainMenu() {
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

    public void startGame() {
        // Create a new GamePanel each time to reset state, passing difficulty
        mainPanel.add(new GamePanel(this, selectedDifficulty), "GAME");
        cardLayout.show(mainPanel, "GAME");
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
