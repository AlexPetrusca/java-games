import animators.*;
import org.age.panel.AbstractGamePanel;
import org.age.util.String2D;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class BreakoutScreen extends AbstractGamePanel {
  private int levelChangeCounter = 0;
  private boolean willBigScreenShake;
  private boolean willScreenShake;
  private int confusionCounter = 0;
  private boolean isConfused = false;
  BreakoutRun breakoutRun;
  BreakBox[][] boxes = null;
  Paddle paddle = null;
  int score = 0;
  int level = 1;
  int lives = 5;
  ArrayList<Ball> balls;
  ArrayList<PowerUp> powerUps;
  ArrayList<DeadBox> deadBoxes;
  ArrayList<TextParticle> texts;
  ArrayList<Animator> anims;

  public BreakoutScreen(BreakoutRun breakoutRun) {
    this.breakoutRun = breakoutRun;
    deadBoxes = new ArrayList<DeadBox>();
    powerUps = new ArrayList<PowerUp>();
    balls = new ArrayList<Ball>();
    texts = new ArrayList<TextParticle>();
    paddle = new Paddle(breakoutRun.width/2, 7*breakoutRun.height/8);
    balls.add(new Ball(breakoutRun.width/2, 3*breakoutRun.height/4, true));
    try {
      Robot robot = new Robot();
      robot.mouseMove(642 + 133, 407);
    } catch (AWTException e) {
      System.exit(-1);
    }
    breakoutRun.addMouseMotionListener(breakoutRun);
    loadBoxes(BreakoutScreen.class.getResource("assets/levels/level" + level + ".png").getPath());
    addAnims();
    breakoutRun.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        for (int i = 0; i < balls.size(); i++) {
          if (balls.get(i).isStuck()) {
            balls.get(i).unStick();
            balls.get(i).deflect(balls.get(i).getX() - paddle.getX(), paddle.getBounds().getWidth() / 2);
          }
        }
        if(paddle.isGun()) {
          paddle.shootGun();
        }
      }
    });
    breakoutRun.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
          for (Ball ball : balls) {
            if (ball.isStuck()) {
              ball.unStick();
              ball.deflect(ball.getX() - paddle.getX(), paddle.getBounds().getWidth() / 2);
            }
          }
          if(paddle.isGun()) {
            paddle.shootGun();
          }
        }
      }
    });
  }

  private void addAnims() {
    anims = new ArrayList<>();
    anims.add(new InOutFadeAnimator(0.0f, 0.02f, Animator.Fade.FADE_OUT, 50));
    anims.add(new InOutFadeAnimator(0.5f, 0.02f, Animator.Fade.FADE_OUT, 75));
    anims.add(new InOutFadeAnimator(1.0f, 0.02f, Animator.Fade.FADE_OUT, 100));
    anims.add(new RainbowAnimator(0.001f));
    anims.add(new ScreenShakeAnimator(0.0, 0.0, 5.0, 5.0, 3.0, 3.0, 4));
    anims.add(new ScreenShakeAnimator(0.0, 0.0, 1.5, 1.5, 1.0, 1.0, 2));
  }

  public void processInput(boolean[] keys) {
    double keyDisplacement = 45.0;
    if(isConfused) {
      keyDisplacement = -keyDisplacement;
      confusionCounter++;
    }

    if (keys[KeyEvent.VK_RIGHT]) {
      if(paddle.getX() + keyDisplacement < breakoutRun.width - 2.5*paddle.getLengthFactor()) {
        paddle.setPosition(paddle.getX() + keyDisplacement, paddle.getY());
      } else {
        paddle.setPosition(breakoutRun.width - 2.5*paddle.getLengthFactor(), paddle.getY());
      }
    }
    if (keys[KeyEvent.VK_LEFT]) {
      if(paddle.getX() - keyDisplacement > 2.5*paddle.getLengthFactor()) {
        paddle.setPosition(paddle.getX() - keyDisplacement, paddle.getY());
      } else {
        paddle.setPosition(2.5*paddle.getLengthFactor(), paddle.getY());
      }
    }
    if (keys[KeyEvent.VK_ESCAPE]) {
      System.exit(0);
    }
    if (keys[KeyEvent.VK_WINDOWS]) {
      breakoutRun.setCursor(Cursor.getDefaultCursor());
    }

    if(confusionCounter > 250) {
      isConfused = false;
      confusionCounter = 0;
    }
  }

  public void animate() {
    if (breakoutRun.hasFocus()) {
      try {
        Robot robot = new Robot();
        robot.mouseMove(642 + 133, 407);
      } catch (AWTException e) {
        System.exit(-1);
      }
      if(isConfused) {
        breakoutRun.mouseDisplacement.x = -breakoutRun.mouseDisplacement.x;
        confusionCounter++;
      }
      if (breakoutRun.mouseDisplacement.x < 0) {
        if(paddle.getX() + breakoutRun.mouseDisplacement.x > 2.5*25) {
          paddle.setPosition(paddle.getX() + breakoutRun.mouseDisplacement.x, paddle.getY());
        } else {
          paddle.setPosition(2.5*25, paddle.getY());
        }
      } else if (breakoutRun.mouseDisplacement.x > 0) {
        if(paddle.getX() + breakoutRun.mouseDisplacement.x < breakoutRun.width - 2.5*25) {
          paddle.setPosition(paddle.getX() + breakoutRun.mouseDisplacement.x, paddle.getY());
        } else {
          paddle.setPosition(breakoutRun.width - 2.5*25, paddle.getY());
        }
      }
    }
    for (Ball ball : balls) {
      if (ball.isStuck()) {
        ball.updateWithPaddle(paddle);
      }
      ball.moveStep();
    }
    checkCollisions();
    checkGameOver();
    cleanParticles();
    if(isNull(boxes)) {
      levelChangeCounter++;
      if(levelChangeCounter > 10) {
        level++;
        levelChangeCounter = 0;
        loadBoxes(BreakoutScreen.class.getResource("assets/levels/level" + level + ".png").getPath());
        balls.add(new Ball(breakoutRun.width/2, 3*breakoutRun.height/4, true)); //FIXME ------> make better
      }
    }
  }

  private boolean isNull(BreakBox[][] boxes) {
    for (BreakBox[] box : boxes) {
      for (BreakBox breakBox : box) {
        if(breakBox != null && !breakBox.isUnbreakable()) {
          return false;
        }
      }
    }
    return true;
  }

  private void checkGameOver() {
    if(lives == 0) {
      JOptionPane.showMessageDialog(new JFrame(), "              You Suck!");
      System.exit(0);
    }
  }

  private void cleanParticles() {
    for (int i = 0; i < deadBoxes.size(); i++) {
      if(!deadBoxes.get(i).getAnimator().isAnimating()) {
        deadBoxes.remove(i);
      }
    }
    for (int i = 0; i < texts.size(); i++) {
      if(!texts.get(i).isAnimating()) {
        texts.remove(i);
      }
    }
  }

  private void checkCollisions() {
    for (Ball ball : balls) {
      Area areaA = new Area(ball.getBounds());
      Area areaB = new Area(paddle.getBounds());
      areaA.intersect(areaB);
      if (!areaA.isEmpty()) {
        double diffX = ball.getX() - paddle.getX();
        if (paddle.isMagnetic() && !ball.isStuck()) {
          ball.stick(diffX);
        } else if (!paddle.isMagnetic()) {
          ball.unStick();
          ball.deflect(diffX, paddle.getBounds().getWidth() / 2);
        }
      }
    }

    ArrayList<Shape> bullets = paddle.getBullets();
    for (int y = 0; y < boxes.length; y++) {
      for (int x = 0; x < boxes[y].length; x++) {
        for (int i = 0; i < bullets.size(); i++) {
          if(boxes[y][x] != null) {
            Area area1 = new Area(boxes[y][x].getBounds());
            Rectangle2D bullet = bullets.get(i).getBounds2D();
            Area area2 = new Area(new Arc2D.Double(bullet.getX(), bullet.getY() - 15, bullet.getWidth(), bullet.getHeight(), 0, 360, Arc2D.CHORD));
            area1.intersect(area2);
            if (!area1.isEmpty()) {
              paddle.removeBullet(i);
              if(!boxes[y][x].isUnbreakable()) {
                remove(x, y);
              }
              i++;
            }
          }
        }
      }
    }

    for (Ball ball : balls) {
      for (int y = 0; y < boxes.length; y++) {
        for (int x = 0; x < boxes[y].length; x++) {
          if (boxes[y][x] != null) {
            Rectangle2D bounds = boxes[y][x].getBounds();
            Line2D ballPath = new Line2D.Double(ball.getX(), ball.getY(), ball.getNextX(), ball.getNextY());
            Line2D upLine = new Line2D.Double(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMinY());
            Line2D downLine = new Line2D.Double(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxX(), bounds.getMaxY());
            Line2D rightLine = new Line2D.Double(bounds.getMinX(), bounds.getMinY(), bounds.getMinX(), bounds.getMaxY());
            Line2D leftLine = new Line2D.Double(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY());
            if (upLine.intersectsLine(ballPath) || downLine.intersectsLine(ballPath)) {
              if (boxes[y][x].isUnbreakable()) {
                ball.flipVelY();
              } else if (!Boolean.logicalXor(ball.getBallState() != Ball.BallStates.POWER_BALL, ball.getBallState() != Ball.BallStates.DRUNK_POWER_BALL)) {
                ball.flipVelY();
              }
              remove(x, y);
            } else if (rightLine.intersectsLine(ballPath) || leftLine.intersectsLine(ballPath)) {
              if (boxes[y][x].isUnbreakable()) {
                ball.flipVelX();
              } else if (!Boolean.logicalXor(ball.getBallState() != Ball.BallStates.POWER_BALL, ball.getBallState() != Ball.BallStates.DRUNK_POWER_BALL)) {
                ball.flipVelX();
              }
              remove(x, y);
            }
          }
        }
      }
    }

    for (int i = 0; i < powerUps.size(); i++) {
      if(powerUps.get(i) != null) {
        Area bounds = new Area(powerUps.get(i).getBounds());
        Area paddleBounds = new Area(paddle.getBounds());
        bounds.intersect(paddleBounds);
        if (!bounds.isEmpty()) {
          applyPowerUp(powerUps.get(i), bounds.getBounds2D().getX(), bounds.getBounds2D().getY());
          powerUps.remove(i);
          i--;
        }
      }
    }

    for (int i = 0; i < balls.size(); i++) {
      Line2D ballPath = new Line2D.Double(balls.get(i).getX(), balls.get(i).getY(), balls.get(i).getNextX(), balls.get(i).getNextY());
      Line2D lastLastBallPath = new Line2D.Double(balls.get(i).getSecondLastX(), balls.get(i).getSecondLastY(), balls.get(i).getThirdLastX(), balls.get(i).getThirdLastY());
      Line2D upLine = new Line2D.Double(-100, 80, 1920, 80);
      Line2D downLine = new Line2D.Double(-100, breakoutRun.height, 1920, breakoutRun.height);
      Line2D rightLine = new Line2D.Double(breakoutRun.width, -100, breakoutRun.width, 1080);
      Line2D leftLine = new Line2D.Double(0, -100, 0, 1080);
      if (upLine.intersectsLine(ballPath)) {
        balls.get(i).flipVelY();
      } else if (rightLine.intersectsLine(ballPath) || leftLine.intersectsLine(ballPath)) {
        balls.get(i).flipVelX();
      } else if (downLine.intersectsLine(lastLastBallPath)) {
        balls.remove(i);
        i--;
        if(balls.size() == 0) {
          balls = new ArrayList<Ball>();
          balls.add(new Ball(breakoutRun.width / 2, 3 * breakoutRun.height / 4, true));
          powerUps = new ArrayList<PowerUp>();
          addAnims();
          lives--;
        }
      }
    }
  }

  private void remove(int x, int y) {
    if(!boxes[y][x].isUnbreakable()) {
      score += 5;
      deadBoxes.add(new DeadBox(boxes[y][x]));
      maybeSpawnPowerUp(boxes[y][x]);
      boxes[y][x] = null;
      enableScreenShake();
    } else {
      enableBigScreenShake();
    }
  }

  private void enableBigScreenShake() {
    willBigScreenShake = true;
  }

  private void enableScreenShake() {
    willScreenShake = true;
  }

  private void applyPowerUp(PowerUp powerUp, double x, double y) {
    score += 20;
    boolean isDone = false;
    Color startColor = Color.black;
    Color endColor = Color.red;
    String display = normalizeEnumField(powerUp.getPower().toString());
    for (int i = 0; i < balls.size() && !isDone; i++) {
      if (powerUp.getPower() == PowerUp.PowerUps.POWER_BALL) {
        endColor = Color.green;
        if (balls.get(i).getBallState() == Ball.BallStates.DRUNK_BALL) {
          balls.get(i).setBallState(Ball.BallStates.DRUNK_POWER_BALL);
        } else if (balls.get(i).getBallState() == Ball.BallStates.DRUNK_POWER_BALL) {
          balls.get(i).setBallState(Ball.BallStates.DRUNK_POWER_BALL);
        } else {
          balls.get(i).setBallState(Ball.BallStates.POWER_BALL);
        }
      } else if (powerUp.getPower() == PowerUp.PowerUps.SPEED_DOWN) {
        endColor = Color.red;
        balls.get(i).decreaseSpeed();
      } else if (powerUp.getPower() == PowerUp.PowerUps.SPEED_UP) {
        endColor = Color.green;
        balls.get(i).increaseSpeed();
      } else if (powerUp.getPower() == PowerUp.PowerUps.SHORTER_PADDLE) {
        endColor = Color.red;
        paddle.decreaseLength();
        isDone = true;
      } else if (powerUp.getPower() == PowerUp.PowerUps.LONGER_PADDLE) {
        endColor = Color.green;
        paddle.increaseLength();
        isDone = true;
      } else if (powerUp.getPower() == PowerUp.PowerUps.MAGNETIC_PADDLE) {
        endColor = Color.green;
        paddle.magnetize();
        isDone = true;
      } else if (powerUp.getPower() == PowerUp.PowerUps.LIFE_UP) {
        endColor = Color.green;
        lives++;
        isDone = true;
      } else if (powerUp.getPower() == PowerUp.PowerUps.GUN_PADDLE) {
        endColor = Color.green;
        paddle.attachGun();
        isDone = true;
      } else if (powerUp.getPower() == PowerUp.PowerUps.CONFUSION) {
        endColor = Color.red;
        isConfused = true;
        confusionCounter = 0;
        isDone = true;
      } else if (powerUp.getPower() == PowerUp.PowerUps.SPLIT_BALLS) {
        endColor = Color.green;
        splitBalls();
        isDone = true;
      } else if (powerUp.getPower() == PowerUp.PowerUps.DRUNK_BALL) {
        endColor = Color.red;
        if (balls.get(i).getBallState() == Ball.BallStates.POWER_BALL) {
          balls.get(i).setBallState(Ball.BallStates.DRUNK_POWER_BALL);
        } else if (balls.get(i).getBallState() == Ball.BallStates.DRUNK_POWER_BALL) {
          balls.get(i).setBallState(Ball.BallStates.DRUNK_POWER_BALL);
        } else {
          balls.get(i).setBallState(Ball.BallStates.DRUNK_BALL);
        }
      }
    }

    String2D s = new String2D(display, new Font(Font.SERIF, Font.BOLD, 24));
    double time = 100;
    TextParticle particle = new TextParticle(s, x - 23, y, 0.75, 0.75, 0, 0, 0, (int)time);
    particle.setDY(-time/100);
    particle.add(new OutFadeAnimator((float)(time/5000)));
    particle.add(new ColorToColorAnimator(startColor, endColor, time/3));
    texts.add(particle);
  }

  private void splitBalls() {
    int limit = balls.size();
    for (int i = 0; i < limit; i++) {
      Ball ball = balls.get(i);
      balls.add(new Ball(ball.getX(), ball.getY(), ball.getVelX() + Math.pow(2, 0.5), ball.getVelY() + Math.pow(2, 0.5), false));
    }
  }

  private String normalizeEnumField(String badlyFormatted) {
    String wellFormatted = "" + badlyFormatted.charAt(0);
    for(int i = 1; i < badlyFormatted.length(); i++) {
      if(badlyFormatted.charAt(i) == '_') {
        wellFormatted += " ";
      } else if(badlyFormatted.charAt(i - 1) == '_') {
        wellFormatted += badlyFormatted.charAt(i);
      } else {
        wellFormatted += (char)(badlyFormatted.charAt(i) + 32);
      }
    }
    return wellFormatted;
  }

  private void maybeSpawnPowerUp(BreakBox box) {
    int random = (int)(100 * Math.random());
    if(random < 40) {
      PowerUp power = new PowerUp(box);
      powerUps.add(power);
    }
  }

  private void loadBoxes(String str) {
    BufferedImage image = null;
    try {
      image = ImageIO.read(new File(str));
    } catch(IOException e) {
      JOptionPane.showMessageDialog(new JFrame(), "               You Win!");
      System.exit(0);
    }

    boxes = new BreakBox[image.getHeight()][image.getWidth()];
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        int[] rgb = image.getData().getPixel(x, y, (int[])null);
        Color newColor = new Color(rgb[0], rgb[1], rgb[2]);
        if(rgb[0] == 0 || rgb[1] == 0 || rgb[2] == 0) {
          boxes[y][x] = new BreakBox(null, x * 4 * breakoutRun.pixelScale, y * breakoutRun.pixelScale + 77);
          boxes[y][x].setUnbreakable();
          boxes[y][x].setUnbreakable();
        } else if(rgb[0] < 250 || rgb[1] < 250 || rgb[2] < 250) {
          boxes[y][x] = new BreakBox(newColor, x * 4 * breakoutRun.pixelScale, y * breakoutRun.pixelScale + 77);
        }
      }
    }
  }

  public void paint(Graphics2D g) {
    if(willBigScreenShake) {
      anims.get(4).animate(g);
      if(!anims.get(4).isAnimating()) {
        willBigScreenShake = false;
        anims.set(4, new ScreenShakeAnimator(0.0, 0.0, 5.0, 5.0, 3.0, 3.0, 4));
      }
    }else if(willScreenShake) {
      anims.get(5).animate(g);
      if(!anims.get(5).isAnimating()) {
        willScreenShake = false;
        anims.set(5, new ScreenShakeAnimator(0.0, 0.0, 1.5, 1.5, 1.0, 1.0, 2));
      }
    }
    paintParticles(g);
    paintBoard(g);
    paintStats(g);
  }

  double angle = 0;
  private void paintWave(Graphics2D g) {
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
    g.setClip(0, 0, breakoutRun.width, 78);
    g.setStroke(new BasicStroke(10));

    double angleDisplacement = 0.2;
    double angleVel = 0.0003;
    double amplitude = 76/2;
    Shape circle = new Arc2D.Double();
    int i = 0;
    while(!circle.intersects(breakoutRun.width, 0, 10, breakoutRun.height)) {
      double x = i*25;
      double y = amplitude * Math.sin(angle + angleDisplacement * i);
      circle = new Arc2D.Double(x - 24, y + 24, 48, 48, 0, 360, Arc2D.CHORD);
      Color color = ((RainbowAnimator)anims.get(3)).getColor();
      g.setPaint(new Color((255 - color.getRed())/255.0f, (255 - color.getGreen())/255.0f, (255 - color.getBlue())/255.0f, 0.5f));
      g.draw(circle);
      angle += angleVel;
      i++;
    }
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
  }

  private void paintParticles(Graphics2D g) {
    for (int i = 0; i < deadBoxes.size(); i++) {
      deadBoxes.get(i).draw(g);
    }
//    for (int i = 0; i < deadBoxes.size(); i++) {
//      deadBoxes.get(i).draw(g);  // FiXmE ???????????????? Repeat for what?
//    }
    for (int i = 0; i < texts.size(); i++) {
      texts.get(i).animate(g);
    }
  }

  private void paintStats(Graphics2D g) {
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    g.setColor(breakoutRun.getBackground());
    anims.get(3).animate(g); // lol
    g.fillRect(0, 0, breakoutRun.width + 1, 78);
    g.setColor(Color.BLACK);
    g.fillRect(0, 78, breakoutRun.width + 1, 4);
    paintWave(g);
    g.setColor(Color.BLACK);
    g.setFont(new Font(Font.SERIF, Font.BOLD, 76));
    int offset = (int)(breakoutRun.width/15.3);
    anims.get(0).animate(g);
    g.drawString("Level: " + level, 0 + offset, 66);
    anims.get(1).animate(g);
    g.drawString("Lives: " + lives, breakoutRun.width/3 + offset, 66);
    anims.get(2).animate(g);
    g.drawString("Score: " + score, 2*breakoutRun.width/3 + offset, 66);
  }

//  float counter = 0.1f;
  private void paintBoard(Graphics2D g) {
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    for (BreakBox[] box : boxes) {
      for (BreakBox breakBox : box) {
        if(breakBox != null) {
          breakBox.draw(g);
        }
      }
    }
    for (int i = 0; i < powerUps.size(); i++) {
      powerUps.get(i).animate();
      powerUps.get(i).draw(g);
    }
    paddle.draw(g); //FixMe ---> make it so that bullets are painted behind blocks
    for (int i = 0; i < balls.size(); i++) {
      balls.get(i).draw(g);
    }

//    if(display != null) {
//      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
//      Font font = new Font(Font.SERIF, Font.BOLD, 24);
//      String2D string = new String2D(display, font);
//      double x = (float) (breakoutRun.width / 2 - string.getWidth() / 2);
//      double y = (float) (breakoutRun.height / 2 + string.getHeight() / 2);
//      if(counter < 12 * Math.PI) {
//        string.paint(g, x, y, counter/20, counter/20, counter/2, breakoutRun.width/2, breakoutRun.height/2);
//      } else {
//        string.paint(g, x, y, counter/20, counter/20, 0, 0, 0);
//      }
//      if(counter >= 60.0f) {
//        display = null;
//        counter = 0.1f;
//      }
//      counter += .5f;
//    }
  }
}
