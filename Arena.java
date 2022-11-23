import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * Arena class that controls all game elements and display
 */
public class Arena extends JPanel implements ActionListener, KeyListener, MouseListener {

    private static final Color ARENA_COLOR = new Color(230, 230, 230);
    private static final Color BACKGROUND_COLOR = new Color(120, 120, 120);
    private static final Color MOUSE_COLOR = new Color(0, 0, 0);
    
    private final int DELAY = 10;

    public static final int width = 900;
    public static final int height = 600;
    public static final int border = 20;

    // private Robot robot;

    private int tickNumber;
    private long startTime;
    private long currentTime;
    private long timeSinceTick;
    private long[] lastNTicks;
    private int n = 100;

    private Timer timer;

    private Vector mousePos;
    private Player player;
    private Boss boss;

    private Cursor blankCursor;

    public Arena() {
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.DARK_GRAY);

        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        this.setCursor(blankCursor);

        player = new Player(width / 2f, height / 2f);
        mousePos = new Vector(0, 0);

        boss = new Boss(600, 250);

        /**
        try {
            robot = new Robot();
        } catch (Exception e) {
            robot = null;
        }
        */

        startTime = System.currentTimeMillis();
        currentTime = startTime;
        timeSinceTick = 0;
        lastNTicks = new long[n];
        tickNumber = -1;

        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tickNumber++;
        long newTime = System.currentTimeMillis();
        timeSinceTick = newTime - currentTime;
        lastNTicks[tickNumber % n] = timeSinceTick;
        currentTime = newTime;

        try {
            mousePos = new Vector(this.getMousePosition().x, this.getMousePosition().y);
        } catch (Exception exc) {

        }

        float damage = player.tick(timeSinceTick, mousePos, boss.shape);
        /**
        if (player.firingLaser()) {
            this.setCursor(blankCursor);
        } else {
            this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        }
        */
        boss.tick(timeSinceTick, damage);

        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);
        drawArena(g2);
        boss.draw(g2);
        player.draw(g2, boss.shape);
        drawMouse(g2);

        float runTime = Math.round((currentTime - startTime) / 100f) / 10f;
        float avgTST = 0;
        for (int i = 0; i < n; i++) {
            if (lastNTicks[i] != 0) {
                avgTST += lastNTicks[i];
            }
        }
        avgTST /= n;
        float ticksPerSecond = Math.round(10000f / avgTST) / 10f;
        g2.drawString("Runtime: " + runTime + "   Ticks/Sec: " + ticksPerSecond, 5, 15);

        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        player.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.keyReleased(e);
    }

    private void drawBackground(Graphics2D g) {
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0,
            0, 
            width, 
            height);
    }

    private void drawArena(Graphics2D g) {
        g.setColor(ARENA_COLOR);
        g.fillRect(border, 
                border, 
                width - 2 * border, 
                height - 2 * border);
    }

    private void drawMouse(Graphics2D g) {
        float radius = 2;
        Shape circle = new Ellipse2D.Double(mousePos.x - radius, mousePos.y - radius, radius * 2.0, radius * 2.0);
        g.setColor(MOUSE_COLOR);
        g.fill(circle);
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        player.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        player.mouseReleased(e);
    }
}
