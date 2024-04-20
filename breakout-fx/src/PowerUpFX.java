import animationsFX.InFadeAnimatorFX;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

public class PowerUpFX {
  private Point2D point;
  private RoundRectangle2D bounds;
  private PowerUps power;
  private double fallSpeed;
  private InFadeAnimatorFX anim;
  private Image image;
  public static enum PowerUps {
    POWER_BALL,
    SPEED_UP,
    SPEED_DOWN,
    MAGNETIC_PADDLE,
    LONGER_PADDLE,
    SHORTER_PADDLE,
    LIFE_UP,
    GUN_PADDLE,
    DRUNK_BALL,
    CONFUSION,
    SPLIT_BALLS
  }

  public PowerUpFX(BreakBoxFX box) {
    point = new Point2D.Double(box.getPoint().getX() + 1.25*25, box.getPoint().getY() + 0.15*25);
    bounds = new RoundRectangle2D.Double(point.getX(), point.getY(), 25.0 * 3/2, 25.0*0.7, 10, 10);

    int random = (int)(Math.random() * PowerUps.values().length);
    power = PowerUps.values()[random];

    image = BreakoutScreenFX.loadFXImage("assets/powerUpIcons/" + power.name() +".png");

    fallSpeed = 5;
    anim = new InFadeAnimatorFX(0.05f);
  }

  public void draw(GraphicsContext g) {
    anim.animate(g);
    g.drawImage(image, point.getX(), point.getY());
    g.setStroke(Color.BLACK);
    g.strokeRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    g.setGlobalAlpha(1.0f);
  }

  public void animate() {
    point = new Point2D.Double(point.getX(), point.getY() + fallSpeed);
    bounds = new RoundRectangle2D.Double(point.getX(), point.getY(), 25.0*3/2, 25.0*0.7, 10, 10);
  }

  public Shape getBounds() {
    return bounds;
  }

  public Point2D getPoint() {
    return point;
  }

  public PowerUps getPower() {
    return power;
  }
}
