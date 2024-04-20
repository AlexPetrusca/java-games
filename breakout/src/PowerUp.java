import animators.InFadeAnimator;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Created by PetruscaFamily on 3/11/2016.
 */
public class PowerUp {
  private Point2D point;
  private Shape bounds;
  private PowerUps power;
  private double fallSpeed;
  private InFadeAnimator anim;
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

  public PowerUp(BreakBox box) {
    point = new Point2D.Double(box.getPoint().getX() + 1.25*25, box.getPoint().getY() + 0.15*25);
    bounds = new RoundRectangle2D.Double(point.getX(), point.getY(), 25.0 * 3/2, 25.0*0.7, 10, 10);

    int random = (int)(Math.random() * PowerUp.PowerUps.values().length);
    power = PowerUp.PowerUps.values()[random];

//    power = PowerUps.SPLIT_BALLS;

//    if(random > PowerUps.values().length/2) {
//      power = PowerUps.GUN_PADDLE;
//    } else {
//      power = PowerUps.MAGNETIC_PADDLE;
//    }

    fallSpeed = 5;
    anim = new InFadeAnimator(0.05f);
  }

  public void draw(Graphics2D g) {
    Image lel = Toolkit.getDefaultToolkit().getImage(getClass().getResource("assets/powerUpIcons/" + power.name() +".png"));
    anim.animate(g);
    g.setClip(bounds);
    g.drawImage(lel, (int)(point.getX()), (int)(point.getY()), null);
    g.setClip(0, 0, 1920, 1080);
    g.setColor(Color.BLACK);
    g.draw(bounds);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
  }

  public void animate() {
    point = new Point2D.Double(point.getX(), point.getY() + fallSpeed);
    bounds = new RoundRectangle2D.Double(point.getX(), point.getY(), 25.0 *3/2, 25.0*0.7, 10, 10);
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
