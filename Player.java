import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;

public class Player {
    // Constants for player velocity and acceleration
    private static final float BASE_MAX_VELOCITY = 1f / 3;
    private static final float BASE_ACCELERATION = 1f / 1024;
    private static final float DECEL_FACTOR = 0.993f;
    private static final float FIRING_MULTI = 1f / 4;
    // Width denotes hitbox size
    private static final float WIDTH = 8;
    // Size is used for rendering the player shape
    private static final float SIZE = 10;
    private Outline shape;
    // Mouse position
    private Vector mousePos;
    // Position and Velocity vectors.
    private Vector pos;
    private Vector vel;
    private Vector acc;

    private float max_velocity;
    // Booleans for whether a key for movement is pressed
    private boolean rightPressed;
    private boolean leftPressed;
    private boolean upPressed;
    private boolean downPressed;
    // Is the laser being fired
    private boolean firingLaser;
    // Front tip of player
    private Vector tip;
    // Variables for spread of laser and narrowing speed
    private Outline laser;
    private static final double MINIMUM_LASER_DMG = 0.5;
    private static final double MAXIMUM_LASER_DMG = 7.5;
    private static final double INITIAL_SPREAD = 32;
    private static final double WARMUP_SPEED = 1 / 50f;
    private static final double FINAL_SPREAD = 2;
    private double currentSpread;

    private static final int[][] colorRotation = new int[][] {
                                                        new int[] {255, 255, 0},
                                                        new int[] {255, 0, 0},
                                                        new int[] {255, 0, 255},
                                                        new int[] {0, 0, 255},
                                                        new int[] {0, 255, 255},
                                                        new int[] {0, 255, 0}
                                                    };
    private int targetColorIndex;
    private float[] currentColor;
    
    public Player(Float x, Float y) {
        pos = new Vector(x, y);
        vel = new Vector(0, 0);
        acc = new Vector(0, 0);

        max_velocity = BASE_MAX_VELOCITY;

        tip = new Vector(0, 0);

        this.mousePos = new Vector();

        rightPressed = false;
        leftPressed = false;
        upPressed = false;
        downPressed = false;

        firingLaser = false;

        currentSpread = INITIAL_SPREAD;

        targetColorIndex = 0;
        currentColor = new float[] {255, 255, 0};
    }

    private PathIterator pi = null;
    private float[] coords = new float[2];

    /**
     * Draws the player along with firing trajectory and beam if firing
     * @param g Graphics object to draw to canvas with
     */
    public void draw(Graphics2D g, Outline bossShape) {
        // Create semi-normalized vector from position to mouse
        Vector dMouse = new Vector(mousePos.x - pos.x, mousePos.y - pos.y);
        float length = (float) Math.sqrt(dMouse.x*dMouse.x + dMouse.y*dMouse.y);
        dMouse.x = dMouse.x * SIZE / length;
        dMouse.y = dMouse.y * SIZE / length;

        shape = new Outline();
        tip = new Vector(pos.x + dMouse.x * 2, pos.y + dMouse.y * 2);
        shape.moveTo(tip.x, tip.y);
        shape.lineTo(pos.x + dMouse.y, pos.y - dMouse.x);
        shape.lineTo(pos.x - dMouse.x, pos.y - dMouse.y);
        shape.lineTo(pos.x - dMouse.y, pos.y + dMouse.x);
        shape.closePath();
        g.setColor(Color.black);
        g.fill(shape);

        // g.draw(shape.getBounds());

        int red;
        int blue;
        double halfSpread = (INITIAL_SPREAD + FINAL_SPREAD) / 2;
        if (this.currentSpread > halfSpread) {
            blue = 255;
            red = (int) (255 * (INITIAL_SPREAD - this.currentSpread)/(INITIAL_SPREAD - halfSpread));
        } else {
            red = 255;
            blue = (int) (255 * (this.currentSpread - FINAL_SPREAD)/(halfSpread - FINAL_SPREAD));
        }
        red = limitRange(red, 0, 255);
        blue = limitRange(blue, 0, 255);

        g.setColor(new Color((int) currentColor[0], (int) currentColor[1], (int) currentColor[2]));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(shape);
        g.setStroke(new BasicStroke());

        if (firingLaser) { 
            drawLaser(g, bossShape);
        } 
    }

    public void drawLaser(Graphics2D g, Outline bossShape) {
        Double tipToMouseAngle = Math.atan2(mousePos.y - tip.y, mousePos.x - tip.x);

        Double rightAngle = tipToMouseAngle + currentSpread * (Math.PI / 180) / 2;
        Vector rightDir = new Vector((float) Math.cos(rightAngle), (float) Math.sin(rightAngle));

        Double leftAngle = tipToMouseAngle - currentSpread * (Math.PI / 180) / 2;
        Vector leftDir = new Vector((float) Math.cos(leftAngle), (float) Math.sin(leftAngle));

        // Initialize laser triangle as path starting at tip
        laser = new Outline();
        laser.moveTo(tip.x, tip.y);
        // Right side of spread
        Vector rightStop = findWall(tip, rightDir);
        // Left side of spread
        Vector leftStop = findWall(tip, leftDir);
        // Connect from tip to right side
        laser.lineTo(rightStop.x, rightStop.y);
        // Check if spread covers a corner that needs to be included
        // Use int rounded versions of stops to avoid imperfect precision of floats causing equality errors
        Point rightStopInt = new Point(Math.round(rightStop.x), Math.round(rightStop.y));
        Point leftStopInt = new Point(Math.round(leftStop.x), Math.round(leftStop.y));
        if ((rightStopInt.x != leftStopInt.x) && (rightStopInt.y != leftStopInt.y)) {
            float middleX = 0;
            float middleY = 0;
            if ((rightStopInt.x == Arena.border) || (leftStopInt.x == Arena.border)) {
                middleX = Arena.border;
            } else if ((rightStopInt.x == Arena.width - Arena.border) || (leftStopInt.x == Arena.width - Arena.border)) {
                middleX = Arena.width - Arena.border;
            }

            if ((rightStopInt.y == Arena.border) || (leftStopInt.y == Arena.border)) {
                middleY = Arena.border;
            } else if ((rightStopInt.y == Arena.height - Arena.border) || (leftStopInt.y == Arena.height - Arena.border)) {
                middleY = Arena.height - Arena.border;
            }

            laser.lineTo(middleX, middleY);
        }
        laser.lineTo(leftStop.x, leftStop.y);
        laser.closePath();
        g.fill(laser);
    }

    /**
     * Ensures value stays within appropriate range
     * @param value current value to check
     * @param min minimum of range
     * @param max maximum of range
     * @return appropriate number within range
     */
    public static int limitRange(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Finds the first point on an arena wall that will be hit given a starting position and direction
     * @param pos Initial position to draw line from
     * @param dir Direction in which to draw line to wall
     * @return Point at which line hits wall
     */
    public static Vector findWall(Vector pos, Vector dir) {
        Vector wallPoint = new Vector(0, 0);
        float qX = 0;
        float qY = 0;
        if (dir.x > 0) {
            qX = (Arena.width - Arena.border - pos.x) / dir.x;
        } else {
            qX = (pos.x - Arena.border) / dir.x;
        }
        qX = Math.abs(qX);

        if (dir.y > 0) {
            qY = (Arena.height - Arena.border - pos.y) / dir.y;
        } else {
            qY = (pos.y - Arena.border) / dir.y;
        }
        qY = Math.abs(qY);

        if (qX < qY) {
            wallPoint = new Vector(dir.x * qX, dir.y * qX);
        } else {
            wallPoint = new Vector(dir.x * qY, dir.y * qY);
        }
        wallPoint.x += pos.x;
        wallPoint.y += pos.y;
        return wallPoint;
    }

    /**
     * Updates movement parameters on key press
     * @param e Key pressed event
     */
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            upPressed = true;
        }
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            rightPressed = true;
        }
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            downPressed = true;
        }
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            leftPressed = true;
        }
    }

    /**
     * Updates movement parameters on key release
     * @param e Key released event
     */
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            upPressed = false;
        }
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            rightPressed = false;
        }
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            downPressed = false;
        }
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            leftPressed = false;
        }
    }

    /**
     * Updates the status of the player including pos, vel, and mouse positioning
     * @param timeSinceTick time in milliseconds since last tick update
     * @param mousePos current mouse position at time of update
     * @param bossShape boss outline to check hit
     * @return damage dealt to boss
     */
    public float tick(long timeSinceTick, Vector mousePos, Outline bossShape) {
        this.mousePos = mousePos;

        boolean hitBoss = false;

        // Slow down if firing laser and adjust laser spread/color
        float chargingMulti = (float) (255 / ((INITIAL_SPREAD - FINAL_SPREAD) / WARMUP_SPEED));
        if (firingLaser) {
            if (this.currentSpread != FINAL_SPREAD) {
                this.currentSpread = Math.max(this.currentSpread - WARMUP_SPEED * timeSinceTick, FINAL_SPREAD);
            } else {
                chargingMulti = 0.7f;
            }
            boolean colorReached = true;
            for (int i = 0; i < 3; i++) {
                if (currentColor[i] != colorRotation[targetColorIndex][i]) {
                    colorReached = false;
                }
            }
            if (colorReached) {
                targetColorIndex++;
                if (targetColorIndex >= colorRotation.length) {
                    targetColorIndex = 0;
                }
            }

            Double tipToMouseAngle = Math.atan2(mousePos.y - tip.y, mousePos.x - tip.x);

            Double rightAngle = tipToMouseAngle + currentSpread * (Math.PI / 180) / 2;
            Vector rightDir = new Vector((float) Math.cos(rightAngle), (float) Math.sin(rightAngle));

            Double leftAngle = tipToMouseAngle - currentSpread * (Math.PI / 180) / 2;
            Vector leftDir = new Vector((float) Math.cos(leftAngle), (float) Math.sin(leftAngle));

            Vector bossCenter = bossShape.center();

            Double bossAngle = Math.atan2(bossCenter.y - this.tip.y, bossCenter.x - this.tip.x);

            if (bossAngle >= leftAngle && bossAngle <= rightAngle) {
                hitBoss = true;
            } else {
                Vector bossIntersectR = Vector.closestIntersect(tip, rightDir, bossShape);
                Vector bossIntersectL = Vector.closestIntersect(tip, leftDir, bossShape);
                if (bossIntersectL.x != -1 || bossIntersectR.x != -1) {
                    hitBoss = true;
                }
            }

            acc.x = BASE_ACCELERATION * FIRING_MULTI;
            acc.y = BASE_ACCELERATION * FIRING_MULTI;
            max_velocity = BASE_MAX_VELOCITY * FIRING_MULTI;
        } else {
            targetColorIndex = 0;
            this.currentSpread = Math.min(this.currentSpread + WARMUP_SPEED * timeSinceTick * 1.5, INITIAL_SPREAD);

            acc.x = BASE_ACCELERATION;
            acc.y = BASE_ACCELERATION;
            max_velocity = BASE_MAX_VELOCITY;
        }

        float dColor = 0;
        for (int i = 0; i < 3; i++) {
            dColor = colorRotation[targetColorIndex][i] - currentColor[i];
            if (dColor != 0) {
                dColor = dColor / Math.abs(dColor) * timeSinceTick * chargingMulti;
            }
            currentColor[i] = limitRange((int) (currentColor[i] + dColor), 0, 255);
        }
        
        // Update velocity based on movement keys
        if (upPressed) {
            vel.y -= acc.y * timeSinceTick;
        }
        if (downPressed) {
            vel.y += acc.y * timeSinceTick;
        }
        if (rightPressed) {
            vel.x += acc.x * timeSinceTick;
        }
        if (leftPressed) {
            vel.x -= acc.x * timeSinceTick;
        }

        
        if (Math.abs(vel.x) > max_velocity) {
            for (int i = 0; i < (int) timeSinceTick; i++) {
                vel.x *= DECEL_FACTOR;
            }
        }
        if (Math.abs(vel.y) > max_velocity) {
            for (int i = 0; i < (int) timeSinceTick; i++) {
                vel.y *= DECEL_FACTOR;
            }
        }

        if (upPressed == downPressed) {
            for (int i = 0; i < (int) timeSinceTick; i++) {
                vel.y *= DECEL_FACTOR;
            }
        }
        if (rightPressed == leftPressed) {
            for (int i = 0; i < (int) timeSinceTick; i++) {
                vel.x *= DECEL_FACTOR;
            }
        }

        // Update position based on velocity and keep player within bounds
        pos.x += vel.x * timeSinceTick;
        if (pos.x - WIDTH < Arena.border) {
            pos.x = Arena.border + WIDTH;
        } else if (pos.x + WIDTH > Arena.width - Arena.border) {
            pos.x = Arena.width - Arena.border - WIDTH;
        }
        pos.y += vel.y * timeSinceTick;
        if (pos.y - WIDTH < Arena.border) {
            pos.y = Arena.border + WIDTH;
        } else if (pos.y + WIDTH > Arena.height - Arena.border) {
            pos.y = Arena.height - Arena.border - WIDTH;
        }

        if (hitBoss) {
            double percentCharged = (this.currentSpread - INITIAL_SPREAD) / (FINAL_SPREAD - INITIAL_SPREAD);
            return((float) (MINIMUM_LASER_DMG + (MAXIMUM_LASER_DMG - MINIMUM_LASER_DMG) * (percentCharged * percentCharged)));
        } else {
            return(0);
        }
    }

    public void mousePressed(MouseEvent e) {
        firingLaser = true;
    }

    public void mouseReleased(MouseEvent e) {
        firingLaser = false;
    }

    public boolean firingLaser() {
        return firingLaser;
    }
}
