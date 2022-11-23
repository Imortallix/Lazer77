import java.awt.*;
import java.awt.geom.*;

public class Boss {

    private static final int SIZE = 80;
    private static final float MAX_HEALTH = 100;
    private static final int HEALTH_WIDTH = 400;
    private static final int HEALTH_HEIGHT = 25;
    private static final int HEALTH_Y_DROP = 20;

    private static final Color HEALTH_COLOR = new Color(255, 0, 0);
    private static final Color HEALTH_BORDER_COLOR = new Color(80, 0, 0);

    public Vector pos;
    private Vector vel;
    // Angle in degrees from first point vertical
    private float rotation;
    private float rotVel;

    public Outline shape;
    private float health;

    private long totalTime;

    public Boss(float x, float y) {
        pos = new Vector(x, y);
        vel = new Vector(0, 0);
        rotation = 0;
        rotVel = 1;
        health = MAX_HEALTH;
        totalTime = 0;
    }
    
    public void draw(Graphics2D g) {
        shape = new Outline();
        float sharpness = 0.5f;
        double sin = Math.sin(rotation * Math.PI / 180);
        double sin2 = Math.sin((rotation + 45) * Math.PI / 180);
        double cos = Math.cos(rotation * Math.PI / 180);
        double cos2 = Math.cos((rotation + 45) * Math.PI / 180);
        shape.moveTo(pos.x + SIZE * sin, pos.y - SIZE * cos);
        shape.lineTo(pos.x + SIZE * sharpness * sin2, pos.y - SIZE * sharpness * cos2);
        shape.lineTo(pos.x + SIZE * cos, pos.y + SIZE * sin);
        shape.lineTo(pos.x + SIZE * sharpness * cos2, pos.y + SIZE * sharpness * sin2);
        shape.lineTo(pos.x - SIZE * sin, pos.y + SIZE * cos);
        shape.lineTo(pos.x - SIZE * sharpness * sin2, pos.y + SIZE * sharpness * cos2);
        shape.lineTo(pos.x - SIZE * cos, pos.y - SIZE * sin);
        shape.lineTo(pos.x - SIZE * sharpness * cos2, pos.y - SIZE * sharpness * sin2);
        shape.closePath();

        g.setColor(Color.black);
        g.fill(shape);

        Outline healthOutline = new Outline();
        healthOutline.moveTo(Arena.width/2 - HEALTH_WIDTH/2, Arena.border + HEALTH_Y_DROP);
        healthOutline.lineTo(Arena.width/2 + HEALTH_WIDTH/2, Arena.border + HEALTH_Y_DROP);
        healthOutline.lineTo(Arena.width/2 + HEALTH_WIDTH/2, Arena.border + HEALTH_Y_DROP + HEALTH_HEIGHT);
        healthOutline.lineTo(Arena.width/2 - HEALTH_WIDTH/2, Arena.border + HEALTH_Y_DROP + HEALTH_HEIGHT);
        healthOutline.closePath();

        Outline healthBar = new Outline();
        healthBar.moveTo(Arena.width/2 - HEALTH_WIDTH/2, Arena.border + HEALTH_Y_DROP);
        healthBar.lineTo(Arena.width/2 - HEALTH_WIDTH/2 + this.health/MAX_HEALTH * HEALTH_WIDTH, Arena.border + HEALTH_Y_DROP);
        healthBar.lineTo(Arena.width/2 - HEALTH_WIDTH/2 + this.health/MAX_HEALTH * HEALTH_WIDTH, Arena.border + HEALTH_Y_DROP + HEALTH_HEIGHT);
        healthBar.lineTo(Arena.width/2 - HEALTH_WIDTH/2, Arena.border + HEALTH_Y_DROP + HEALTH_HEIGHT);
        healthBar.closePath();

        g.setColor(HEALTH_COLOR);
        g.fill(healthBar);
        g.setColor(HEALTH_BORDER_COLOR);
        g.setStroke(new BasicStroke(3));
        g.draw(healthOutline);
        g.setStroke(new BasicStroke());
    }

    public void tick(long timeSinceTick, float damage) {
        this.health = Math.max(this.health - damage * timeSinceTick / 1000, 0);

        this.totalTime += timeSinceTick;
        this.rotVel = 0.3f + (float) Math.sin(totalTime / 500f);
        this.rotation += rotVel * timeSinceTick / 10;

        this.vel.x = (float) Math.sin(totalTime / 200f);
        this.vel.y = 1.5f * (float) Math.sin(totalTime / 900f + Math.PI / 3);
        this.pos = this.pos.shift(this.vel.scale(timeSinceTick / 10f));
    }
}
