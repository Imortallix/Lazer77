import javax.swing.*;

/**
 * Main class that runs the game application and handles the window
 */
public class App {
    
    public static void initWindow() {
        // Initialize window and set title
        JFrame window = new JFrame("Lazer77");
        // Stop app when the window closes
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize arena, acts as JPanel canvas
        Arena arena = new Arena();
        
        arena.setSize(Arena.width, Arena.height);
        // Add arena to window
        window.add(arena);
        // pass keyboard inputs to arena
        window.addKeyListener(arena);
        window.addMouseListener(arena);

        // Set size of canvas
        // canvas.setPreferredSize(new Dimension(900, 600));
        // canvas.setBackground(Color.white);

        // Make user unable to resize window
        window.setResizable(false);
        // Wrap window around all components
        window.pack();
        // Center window in middle of screen
        window.setLocationRelativeTo(null);
        
        // Make window visible
        window.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initWindow();
            }
        });
    }
}