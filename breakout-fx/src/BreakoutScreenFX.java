import animationsFX.AnimatorFX;
import animationsFX.ColorToColorAnimatorFX;
import animationsFX.InOutFadeAnimatorFX;
import animationsFX.OutFadeAnimatorFX;
import animationsFX.RainbowAnimatorFX;
import animationsFX.ScreenShakeAnimatorFX;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Affine;

import javax.imageio.ImageIO;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BreakoutScreenFX extends StackPane {
  public static Image transferImg;
  public static boolean shouldRun;
  public static boolean isNeon;
  private boolean willBigScreenShake;
  private boolean willScreenShake;
  private boolean isConfused;
  private static boolean gameOver;
  private boolean changedState;
  private int changedCounter;
  private BreakoutRunFX runFX;
  private ContinueScreenFX continueFX;
  private IntroScreenFX introFX;
  private MenuScreenFX menuFX;
  private PaddleFX paddle;
  public static double mouseDisplacement;
  public static Point2D mousePosition;
  public static Point2D mouseClickedPos;
  Image image = loadFXImage("assets/background/background1.jpg");
  private BreakBoxFX[][] boxes;
  List<BallFX> balls;
  List<DeadBoxFX> deadBoxes;
  List<AnimatorFX> anims;
  List<PowerUpFX> powerUps;
  List<TextParticleFX> texts;
  List<DotLightFX> dotlights;
  List<ShatterParticleFX> shatters;
  List<LifeFX> lifeParticles;
  private int score = 0;
  public int level = 1;
  private int lives = 5;
  private int confusionCounter;
  private double blur = 205;
  private double scaleX = 2.3;
  private double scaleY = 2.3;
  private int levelChangeCounter;
  private boolean isTransitioningLevel;
  private boolean isIntroducingBoxes;
  private boolean isCleaningBoard; // fixme implement a cleanup system
  private boolean cleaningIsDone;
  public static boolean gamePaused;

  public BreakoutScreenFX(BreakoutRunFX runFX) {
    this.runFX = runFX;
    paddle = new PaddleFX(runFX.width / 2, 7 * runFX.height / 8);
    balls = new ArrayList<BallFX>();
    deadBoxes = new ArrayList<DeadBoxFX>();
    anims = new ArrayList<AnimatorFX>();
    powerUps = new ArrayList<PowerUpFX>();
    shatters = new ArrayList<ShatterParticleFX>();
    texts = new ArrayList<TextParticleFX>();
    dotlights = new ArrayList<DotLightFX>();
    balls.add(new BallFX(runFX.width / 2, 3 * runFX.height / 4, true));
    lifeParticles = new ArrayList<LifeFX>();
    for (int i = 0; i < lives; i++) {
      lifeParticles.add(new LifeFX(i));
    }

    introFX = new IntroScreenFX(runFX, this);
    continueFX = new ContinueScreenFX(runFX);
    menuFX = new MenuScreenFX(runFX, this);
    mousePosition = new Point2D.Double(0, 0);
    mouseClickedPos = new Point2D.Double(0, 0);
    loadBoxes("assets/levels/level" + level + ".png");
    addAnims();
    isNeon = true;
  }

  public void runGame(double t) {
    runFX.getUpperG().save();
    runFX.getLowerG().save();
    runFX.getUIG().save();
    runFX.getGlassG().save();
    {
      runFX.getUpperG().clearRect(0, 0, 10000, 10000);
      runFX.getLowerG().clearRect(0, 0, 10000, 10000);
      runFX.getUIG().clearRect(0, 0, 10000, 10000);
      runFX.getGlassG().clearRect(0, 0, 10000, 10000);
      if (changedCounter < 51 && runFX.state == BreakoutRunFX.BreakoutState.GAME) {
        if (changedCounter < 50) {
          blur -= 5;
          Effect effect = new BoxBlur(blur, blur, (int) blur);
          runFX.getUpperG().setEffect(effect);
          runFX.getLowerG().setEffect(effect);
          runFX.getUIG().setEffect(effect);
          Affine affine = new Affine();
          affine.appendTranslation(0, 0);
          affine.appendRotation(0, 0, 0);
          affine.appendScale(scaleX, scaleY, runFX.width / 2, runFX.height / 2);
          runFX.getUpperG().setTransform(affine);
          runFX.getLowerG().setTransform(affine);
          runFX.getUIG().setTransform(affine);
          scaleX -= 0.035;
          scaleY -= 0.035;
          if (scaleX < 1) {
            scaleX = 1;
          }
          if (scaleY < 1) {
            scaleY = 1;
          }
        } else {
          runFX.getUpperG().setEffect(null);
          runFX.getLowerG().setEffect(null);
          runFX.getUIG().setEffect(null);
        }
      }
      if (runFX.state == BreakoutRunFX.BreakoutState.GAME) {
        if (shouldRun && !gamePaused) {
          animateGame(t);
        }
        paintGame(runFX.getUpperG());
        runFX.lastState = BreakoutRunFX.BreakoutState.GAME;
      } else if (runFX.state == BreakoutRunFX.BreakoutState.MENU) {
        if(!gamePaused) {
          menuFX.animate(t);
        }
        menuFX.paint(runFX.getUpperG());
        runFX.lastState = BreakoutRunFX.BreakoutState.MENU;
      } else if (runFX.state == BreakoutRunFX.BreakoutState.CONTINUE) {
        if(!gamePaused) {
          continueFX.animate(t);
        }
        continueFX.paint(runFX.getUpperG());
        runFX.lastState = BreakoutRunFX.BreakoutState.CONTINUE;
      } else if (runFX.state == BreakoutRunFX.BreakoutState.INTRO) {
        if(!gamePaused) {
          introFX.animate(t);
        }
        introFX.paint(runFX.getUpperG());
        runFX.lastState = BreakoutRunFX.BreakoutState.INTRO;
      }
      changedState = runFX.state != runFX.lastState;

      if ((changedState || (!shouldRun && runFX.state == BreakoutRunFX.BreakoutState.GAME)) && !gamePaused) {
        try {
          Robot robot = new Robot();
          robot.mouseMove((int) (runFX.getStage().getX() + runFX.getStage().getWidth()/2), 407);
        } catch (AWTException e) {
          System.exit(-1);
        }
      }
      if(changedState) {
        setDefaultEffects();
      }
      if (changedCounter < 60 && runFX.state == BreakoutRunFX.BreakoutState.GAME) {
        double alpha = (60 - changedCounter) / 60.0;
        if (alpha < 0) {
          alpha = 0;
        }
        runFX.getGlassG().setGlobalAlpha(alpha);
        runFX.getGlassG().drawImage(transferImg, 0, 0, runFX.width, runFX.height);
        changedCounter++;
      } else if (changedCounter == 40) {
        setDefaultEffects();
      }
    }
    changedState = false;

    runFX.getUpperG().restore();
    runFX.getLowerG().restore();
    runFX.getUIG().restore();
    runFX.getGlassG().restore();
  }

  private void addAnims() {
    anims = new ArrayList<AnimatorFX>();
    anims.add(new InOutFadeAnimatorFX(0.0f, 0.02f, AnimatorFX.Fade.FADE_OUT, 50));
    anims.add(new InOutFadeAnimatorFX(0.5f, 0.02f, AnimatorFX.Fade.FADE_OUT, 75));
    anims.add(new InOutFadeAnimatorFX(1.0f, 0.02f, AnimatorFX.Fade.FADE_OUT, 100));
    anims.add(new RainbowAnimatorFX(0.001f));
    anims.add(new ScreenShakeAnimatorFX(0.0, 0.0, 5.0, 5.0, 3.0, 3.0, 4));
    anims.add(new ScreenShakeAnimatorFX(0.0, 0.0, 1.5, 1.5, 1.0, 1.0, 2));
  }

  private void loadBoxes(String url) {
    BufferedImage image = loadBufferedImage(url);
    if(image != null) {
      boxes = new BreakBoxFX[image.getHeight()][image.getWidth()];
      for (int y = 0; y < image.getHeight(); y++) {
        for (int x = 0; x < image.getWidth(); x++) {
          int[] rgb = image.getData().getPixel(x, y, (int[]) null);
          Color newColor = new Color(rgb[0] / 255.0, rgb[1] / 255.0, rgb[2] / 255.0, 1.0f);
          if (rgb[0] == 0 || rgb[1] == 0 || rgb[2] == 0) {
            boxes[y][x] = new BreakBoxFX(null, x * 4 * runFX.pixelScale, y * runFX.pixelScale + 77);
            boxes[y][x].setUnbreakable();
          } else if (rgb[0] < 250 || rgb[1] < 250 || rgb[2] < 250) {
            boxes[y][x] = new BreakBoxFX(newColor, x * 4 * runFX.pixelScale, y * runFX.pixelScale + 77);
          }
        }
      }
    } else {
      gameOver = true;
      level--;
    }
  }

  private void setDefaultEffects() {
    Bloom effect = new Bloom(0);
    BreakoutRunFX.upperCanvas.setEffect(effect); //FIXME add canvas effects
    BreakoutRunFX.lowerCanvas.setEffect(effect);
    BreakoutRunFX.UICanvas.setEffect(new GaussianBlur(1.5));
    BreakoutRunFX.glassCanvas.setEffect(effect);
  }

  public void addEventHandlers() {
    setDefaultEffects();
    runFX.getScene().setOnKeyPressed(event -> {
      if (event.getCode().equals(KeyCode.ESCAPE)) {
        if(runFX.state == BreakoutRunFX.BreakoutState.GAME) {
          pauseGame();
        } else {
          runFX.getStage().close();
        }
      }
      if (event.getCode().equals(KeyCode.WINDOWS)) {
        if(runFX.state == BreakoutRunFX.BreakoutState.GAME) {
          pauseGame();
        }
      }
      if (event.getCode().equals(KeyCode.RIGHT)) {
        if(!MenuScreenFX.oneSelected) {
          MenuScreenFX.screen = MenuScreenFX.ScreenMove.RIGHT;
        }
      }
      if (event.getCode().equals(KeyCode.LEFT)) {
        if(!MenuScreenFX.oneSelected) {
          MenuScreenFX.screen = MenuScreenFX.ScreenMove.LEFT;
        }
      }
      if(runFX.state == BreakoutRunFX.BreakoutState.INTRO) {
        if(event.getCode() == KeyCode.SPACE) {
          runFX.state = BreakoutRunFX.BreakoutState.MENU;
        }
      }
    });
    runFX.getScene().setOnKeyReleased(event -> {
      if (event.getCode().equals(KeyCode.SPACE)) {
        for (BallFX ball : balls) {
          if (ball.isStuck()) {
            ball.unStick();
            ball.deflect(ball.getX() - paddle.getX(), paddle.getBounds().getWidth() / 2);
          }
        }
        if (paddle.isGun()) {
          if(!gamePaused) {
            paddle.shootGun();
          }
        }
      }
    });

    runFX.getScene().setOnMouseDragged(event -> {
      mousePosition = new Point2D.Double(event.getX(), event.getY());
      double x = event.getScreenX();
      requestFocus();
      mouseDisplacement = x - (int) (runFX.getStage().getX() + runFX.getStage().getWidth()/2);
      if (isConfused) {
        mouseDisplacement = -mouseDisplacement;
      }
    });
    runFX.getScene().setOnMouseMoved(event -> {
      mousePosition = new Point2D.Double(event.getX(), event.getY());
      double x = event.getScreenX();
      requestFocus();
      mouseDisplacement = x - (int) (runFX.getStage().getX() + runFX.getStage().getWidth()/2);

//      System.out.println("centerX:" + (int) (runFX.getStage().getX() + runFX.getStage().getWidth()/2));
//      System.out.println("x:" + x);
//      System.out.println();

      if (isConfused) {
        mouseDisplacement = -mouseDisplacement;
      }
    });

    runFX.getScene().setOnMouseClicked(event -> {
      mouseClickedPos = new Point2D.Double(event.getX(), event.getY());
      for (int i = 0; i < balls.size(); i++) {
        if (balls.get(i).isStuck()) {
          balls.get(i).unStick();
          balls.get(i).deflect(balls.get(i).getX() - paddle.getX(), paddle.getBounds().getWidth() / 2);
        }
      }
      if (paddle.isGun()) {
        if(!gamePaused) {
          paddle.shootGun();
        }
      }
      if (runFX.lastState == BreakoutRunFX.BreakoutState.GAME) {
        if(gamePaused) {
//          splitBalls(); //FIXME add testing behavior here
        }
      }
      if(gamePaused) {
        unPauseGame();
      }
    });
  }

  private void pauseGame() {
    gamePaused = true;
  }

  private void unPauseGame() {
    gamePaused = false;
  }

  private void animateGame(double t) {
    if (confusionCounter > 250) {
      isConfused = false;
      confusionCounter = 0;
    }
    confusionCounter++;
    runFX.getScene().setCursor(Cursor.NONE);
    if(!gamePaused) {
      try {
        Robot robot = new Robot();
        robot.mouseMove((int) (runFX.getStage().getX() + runFX.getStage().getWidth() / 2), 407);
      } catch (AWTException e) {
        System.exit(-1);
      }
    }

    if (mouseDisplacement < 0) {
      if (paddle.getX() + mouseDisplacement > 2.5 * 25) {
        paddle.setPosition(paddle.getX() + mouseDisplacement, paddle.getY());
      } else {
        paddle.setPosition(2.5 * 25, paddle.getY());
      }
    } else if (mouseDisplacement > 0) {
      if (paddle.getX() + mouseDisplacement < runFX.width - 2.5 * 25) {
        paddle.setPosition(paddle.getX() + mouseDisplacement, paddle.getY());
      } else {
        paddle.setPosition(runFX.width - 2.5 * 25, paddle.getY());
      }
    }

    for (BallFX ball : balls) {
      if (ball.isStuck()) {
        ball.updateWithPaddle(paddle);
      }
      ball.moveStep();
    }
    checkCollisions();
    checkGameOver();
    cleanParticles();
    if(isNull(boxes) && !gameOver) {
      first = true;
      levelChangeCounter++;
      if(levelChangeCounter > 10) {
        if(!isCleaningBoard) {
          isCleaningBoard = true;
          velocity = 20;
          acceleration = 0;
          jerk = 2.21;
          resetProcess(false);
      }
        if(cleaningIsDone) {
          isTransitioningLevel = true;
          if(MenuScreenFX.lvlsUnlocked < level + 1) {
            MenuScreenFX.lvlsUnlocked++;
            menuFX.saveLevelState();
          }
          level++;
          levelChangeCounter = 0;
          loadBoxes("assets/levels/level" + level + ".png");  //fixme game over check
          balls = new ArrayList<BallFX>();
          balls.add(new BallFX(runFX.width / 2, 3 * runFX.height / 4, true));
          powerUps = new ArrayList<PowerUpFX>();
          cleaningIsDone = false;
          isCleaningBoard = false;
        }
      }
    }
  }

  private boolean isNull(BreakBoxFX[][] boxes) {
    for (BreakBoxFX[] box : boxes) {
      for (BreakBoxFX breakBox : box) {
        if(breakBox != null && !breakBox.isUnbreakable()) {
          return false;
        }
      }
    }
    return true;
  }

  private void checkGameOver() {
    if (lives == 0) {
      gameOver = true;
    }
  }

  private void cleanParticles() {
    for (int i = 0; i < deadBoxes.size(); i++) {
      if (!deadBoxes.get(i).getAnimator().isAnimating()) {
        deadBoxes.remove(i);
      }
    }
    for (int i = 0; i < texts.size(); i++) {
      if (!texts.get(i).isAnimating()) {
        texts.remove(i);
      }
    }
    for (int i = 0; i < balls.size(); i++) {
      if (balls.get(i).getX() < -50 || balls.get(i).getX() > runFX.width + 50 || balls.get(i).getY() < -50 || balls.get(i).getY() > runFX.width + 50)
        balls.remove(i);
    }
    for (int i = 0; i < dotlights.size(); i++) {
      if (!dotlights.get(i).isAnimating()) {
        dotlights.remove(i);
      }
    }
    for (int i = 0; i < shatters.size(); i++) {
      if (!shatters.get(i).isAnimating() || (shatters.get(i).getX() < -50 || shatters.get(i).getX() > runFX.width + 50 || shatters.get(i).getY() < -50 || shatters.get(i).getY() > runFX.width + 50)) {
        shatters.remove(i);
      }
    }
  }

  private void paintGame(GraphicsContext g) {
    runFX.getScene().setCursor(Cursor.NONE);
    if (willBigScreenShake) {
      anims.get(4).animate(g);
      ((ScreenShakeAnimatorFX) (anims.get(4))).applyTransform(runFX.getLowerG());
      ((ScreenShakeAnimatorFX) (anims.get(4))).applyTransform(runFX.getUIG());
      if (!anims.get(4).isAnimating()) {
        willBigScreenShake = false;
        anims.set(4, new ScreenShakeAnimatorFX(0.0, 0.0, 5.0, 5.0, 3.0, 3.0, 4));
      }
    } else if (willScreenShake) {
      anims.get(5).animate(g);
      ((ScreenShakeAnimatorFX) (anims.get(5))).applyTransform(runFX.getLowerG());
      ((ScreenShakeAnimatorFX) (anims.get(5))).applyTransform(runFX.getUIG());
      if (!anims.get(5).isAnimating()) {
        willScreenShake = false;
        anims.set(5, new ScreenShakeAnimatorFX(0.0, 0.0, 1.5, 1.5, 1.0, 1.0, 2));
      }
    }
    if (isNeon) {
      drawBackground();
    }
    paintParticles(g);
    paintBoard(g);
    paintUI();
  }

  private void drawBackground() {
    runFX.getLowerG().setFill(Color.BLACK);
    runFX.getLowerG().drawImage(image, -10, -10, runFX.width + 20, runFX.height + 20);
    drawBackgroundLights();
  }

  private void drawBackgroundLights() {
    if (shouldRun) {
      if(!gamePaused) {
        maybeAddDotLight();
      }
      for (DotLightFX dotlight : dotlights) {
        dotlight.animate(runFX.getLowerG());
      }
    }
    runFX.getLowerG().setGlobalAlpha(1);
  }

  private void maybeAddDotLight() {
    if(progress > 4) {
      int random = (int) (Math.random() * 1);
      double x = runFX.width * Math.random();
      double y = runFX.height * Math.random();
      double x1 = runFX.width * Math.random();
      double y1 = runFX.height * Math.random();
      if (random == 0) {
        dotlights.add(new DotLightFX(x, y, getRandomColor()));
      }
      if (Math.random() * 2 == 0) {
        dotlights.add(new DotLightFX(x1, y1, getRandomColor()));
      }
    }
  }

  private Color getRandomColor() {
    return new Color(Math.random(), Math.random(), Math.random(), 1);
  }

  private void paintParticles(GraphicsContext g) {
    for (int i = 0; i < deadBoxes.size(); i++) {
      deadBoxes.get(i).draw(g);
    }
    for (int i = 0; i < shatters.size(); i++) {
      if(!gamePaused) {
        shatters.get(i).animate(g);
      }
    }
    for (int i = 0; i < texts.size(); i++) {
      if(!gamePaused) {
        texts.get(i).animate(g);
      }
    }
  }

  int yTop1 = 100;
  int yTop2 = 100;
  int yBot = 100;
  int xTop = 425;
  double gl = 0;
  double vel = 0;
  double acc = 0;
  double jer = 0.05;
  int progress = 0;
  int glowCounter = 0;
  boolean first = true;
  private void paintUI() {
    // overall top frame
    GraphicsContext g = runFX.getUIG();
    g.setLineWidth(2);
    g.setStroke(new Color(Color.AQUAMARINE.getRed(), Color.AQUAMARINE.getGreen(), Color.AQUAMARINE.getBlue(), 0.5));
    g.setFill(new Color(Color.AQUAMARINE.getRed(), Color.AQUAMARINE.getGreen(), Color.AQUAMARINE.getBlue(), 0.1));
    g.fillRect(0, 0 - yTop2, runFX.width, 80);
    g.strokeLine(0, 80 - yTop2, runFX.width, 80 - yTop2);
    g.setFill(new Color(Color.AQUAMARINE.getRed(), Color.AQUAMARINE.getGreen(), Color.AQUAMARINE.getBlue(), 0.1));

    g.setLineWidth(5);
    // left side
    g.fillRect(0 - xTop, 0, runFX.width / 4, 78);
    g.fillArc(runFX.width / 4 - 78 - xTop, -78, 156, 156, 270, 90, ArcType.ROUND);
    g.strokeLine(0 - xTop, 78, runFX.width / 4 - 4.5 - xTop, 78);
    g.strokeArc(runFX.width / 4 - 77.5 - xTop, -78, 156, 156, 270, 90, ArcType.OPEN);

    // right side
    g.fillRect(3 * runFX.width / 4 + xTop, 0, runFX.width, 78);
    g.fillArc(3 * runFX.width / 4 - 78 + xTop, -78, 156, 156, 180, 90, ArcType.ROUND);
    g.strokeLine(3 * runFX.width / 4 + 4.5 + xTop, 78, runFX.width + xTop, 78);
    g.strokeArc(3 * runFX.width / 4 - 78.5 + xTop, -78, 156, 156, 180, 90, ArcType.OPEN);

    // middle part
    g.fillRect(runFX.width / 4 + runFX.width / 8, 0 - yTop1, runFX.width / 4, 64);
    g.strokeRect(runFX.width / 4 + runFX.width / 8, -15 - yTop1, runFX.width / 4, 74);

    // bottom part
    g.setLineWidth(1);
    g.setStroke(new Color(Color.AQUAMARINE.getRed(), Color.AQUAMARINE.getGreen(), Color.AQUAMARINE.getBlue(), 0.15));
    g.setFill(new Color(Color.AQUAMARINE.getRed(), Color.AQUAMARINE.getGreen(), Color.AQUAMARINE.getBlue(), 0.1));
    g.fillRect(0, runFX.height - 78 + yBot, runFX.width/4, 78);
    g.fillRect(3*runFX.width/4, runFX.height - 78 + yBot, runFX.width, 78);
    g.fillArc(runFX.width/4 - 2 * 78, runFX.height - 78 + yBot, 156 * 2, 156, 0, 90, ArcType.ROUND);
    g.fillArc(3*runFX.width/4 - 2 * 78, runFX.height - 78 + yBot, 156 * 2, 156, 90, 90, ArcType.ROUND);
    g.strokeLine(0, runFX.height - 78 + yBot, runFX.width/4, runFX.height - 78 + yBot);
    g.strokeLine(3*runFX.width/4, runFX.height - 78 + yBot, runFX.width, runFX.height - 78 + yBot);
    g.strokeArc(runFX.width/4 - 2 * 78, runFX.height - 78 + yBot, 156 * 2, 156, 0, 90, ArcType.OPEN);
    g.strokeArc(3 * runFX.width/4 - 2 * 78, runFX.height - 78 + yBot, 156 * 2, 156, 90, 90, ArcType.OPEN);

    if(shouldRun) {
      if(!isIntroducingBoxes && balls.size() > 0) {
        balls.get(0).incrementForStartUp(vel);
        if(balls.get(0).getCurrentY() == 0 && first) {
          isTransitioningLevel = false;
          first = false;
          enableScreenShake();
        }
      }
      if(progress == 0) {
        balls.get(0).incrementForStartUp(vel);
        paddle.incrementForStartUp(vel - 10);
        if(balls.get(0).getCurrentY() == 0 && first) {
          enableScreenShake();
          first = false;
        } else if(paddle.getCurrentY() == 0 && !first) {
          enableScreenShake();
          progress++;
        }
      } else if(progress == 1) {
        yTop1 -= vel;
        if(yTop1 < 0) {
          yTop1 = 0;
          resetProcess(true);
        }
      } else if(progress == 2) {
        xTop -= vel;
        if(xTop < 0) {
          xTop = 0;
          resetProcess(true);
        }
      } else if(progress == 3) {
        yTop2 -= vel;
        yBot -= vel;
        if(yTop2 < 0 && yBot < 0) {
          yTop2 = 0;
          yBot = 0;
          resetProcess(true);
        }
      } else if(progress == 4) {
        if (gl < 1) {
          gl += 0.02;
        }
        GaussianBlur effect = (GaussianBlur)(BreakoutRunFX.UICanvas.getEffect());
        effect.setInput(new Glow(gl));
        BreakoutRunFX.UICanvas.setEffect(effect);
        if(gl > 1) {
          progress++;
        }
      } else if(progress == 5) {
        if(glowCounter < 20) {
          glowCounter++;
        } else {
          if (gl > 0.5) {
            gl -= 0.01;
          }
          GaussianBlur effect = (GaussianBlur) (BreakoutRunFX.UICanvas.getEffect());
          effect.setInput(new Glow(gl));
          BreakoutRunFX.UICanvas.setEffect(effect);
          if (gl < 0.5) {
            progress++;
          }
        }
      }
      if(progress < 6 || !isIntroducingBoxes) {
        vel += acc;
        acc += jer;
      }
    }

    for (LifeFX lifeParticle : lifeParticles) {
      lifeParticle.animate(runFX.getUIG());
      lifeParticle.setYOffset(yTop1);
    }

    g.setFill(new Color(Color.ALICEBLUE.getRed(), Color.ALICEBLUE.getGreen(), Color.ALICEBLUE.getBlue(), 0.9)); // FIXME better font | better color
//    g.setFont(Font.font("Serif", FontWeight.BOLD, FontPosture.REGULAR, 76));
//    g.setFont(Font.font("Magneto", FontWeight.BOLD, FontPosture.REGULAR, 56));
//    g.setFont(Font.font("Forte", FontWeight.BOLD, FontPosture.REGULAR, 76));
//    g.setFont(Font.font("Bauhaus 93", FontWeight.BOLD, FontPosture.REGULAR, 76));
//    g.setFont(Font.loadFont(getClass().getResource("fonts/SlapAndCrumbly.ttf").toExternalForm(), 56));
//    g.setFont(Font.loadFont(getClass().getResource("fonts/SlapAndCrumblyAl.ttf").toExternalForm(), 56));
//    g.setFont(Font.loadFont(getClass().getResource("fonts/divlit.ttf").toExternalForm(), 70));
//    g.setFont(Font.loadFont(getClass().getResource("fonts/operationalAmplifier.ttf").toExternalForm(), 74));
//    g.setFont(Font.loadFont(getClass().getResource("fonts/Neutronium.ttf").toExternalForm(), 66));
    g.setFont(Font.loadFont(getClass().getResource("fonts/AndroidInsomnia.ttf").toExternalForm(), 60));

    int offset = (int) (runFX.width / 15.3);
    g.fillText("Level: " + level, offset - xTop, 63);
    g.fillText("Score: " + score, 2 * runFX.width / 3 + offset + xTop, 63);

    if(gamePaused) {
      g.setFont(Font.loadFont(getClass().getResource("fonts/AndroidInsomnia.ttf").toExternalForm(), 100));
      g.setFill(Color.AQUAMARINE);
      g.setGlobalAlpha(1);
      g.fillText("Paused", runFX.width/2 - 178, runFX.height/2);
    }
  }

  private void resetProcess(boolean screenShake) {
    progress++;
    vel = 0;
    acc = 0;
    jer = 0.05;
    if(screenShake) {
      enableScreenShake();
    }
  }

  int yMax = 0;
  double velocity = 20;
  double acceleration = 0;
  double jerk = 2.21;
  private void paintBoard(GraphicsContext g) {
    if(isTransitioningLevel) {
      yMax = 0;
      isIntroducingBoxes = true;
      isTransitioningLevel = false;
    }

    if(isCleaningBoard) {  // fixme do cleaning work here
      for (int i = 0; i < balls.size(); i++) {
        balls.get(i).startAnimateOut();
        if(balls.get(i).isFinishedExiting()) {
          balls.remove(i);
        }
      }
      boolean allBoxesEmpty = true;
      for (int y = 0; y < boxes.length; y++) {
        for (int x = 0; x < boxes[y].length; x++) {
          if(boxes[y][x] != null) {
            allBoxesEmpty = false;
            boxes[y][x].startAnimateOut();
            if(boxes[y][x].isFinishedExiting()) {
              boxes[y][x] = null;
            }
          }
        }
      }
      if(balls.size() == 0 && allBoxesEmpty) {
        isCleaningBoard = false;
        cleaningIsDone = true;
      }
    }

    if (changedCounter > 50) {
      for (int x = 0; x < boxes[yMax].length; x++) {
        if (boxes[yMax][x] != null) {
          boxes[yMax][x].incrementForStartUp(velocity);
        }
      }
    }
    boolean hasAllNull = true;
    for (int i = 0; i < boxes[yMax].length; i++) {
      if (boxes[yMax][i] != null) {
        hasAllNull = false;
      }
      if (boxes[yMax][i] != null && boxes[yMax][i].isDone() && yMax < boxes.length - 1) {
        yMax++;
        velocity += acceleration;
        acceleration += jerk;
        i = 1000;
        enableScreenShake();
      }
    }
    if (hasAllNull && yMax < boxes.length - 1) {
      yMax++;
      velocity += acceleration;
      acceleration += jerk;
    }
    g.setGlobalAlpha(1.0f);
    if (changedCounter > 50) {
      for (int y = 0; y < boxes.length; y++) {
        for (int x = 0; x < boxes[y].length; x++) {
          BreakBoxFX breakBox = boxes[y][x];
          if (breakBox != null) {
            breakBox.draw(g);
          }
        }
      }
    }
    for (int i = 0; i < powerUps.size(); i++) {
      if(!gamePaused) {
        powerUps.get(i).animate();
      }
      powerUps.get(i).draw(g);
    }
    paddle.draw(g);
    for (int i = 0; i < balls.size(); i++) {
      balls.get(i).draw(g);
    }
    boolean isDone = true;
    for (BreakBoxFX[] box : boxes) {
      for (BreakBoxFX breakBoxFX : box) {
        if (breakBoxFX != null && !breakBoxFX.isDone()) {
          isDone = false;
        }
      }
    }
    if (isDone) {
      shouldRun = true;
      isIntroducingBoxes = false;
      velocity = 20;
      acceleration = 0;
      jerk = 2.21;
    }
  }

  private void checkCollisions() {
    // balls with paddle
    for (BallFX ball : balls) {
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

    // bullets with blocks
    ArrayList<Shape> bullets = paddle.getBullets();
    for (int y = 0; y < boxes.length; y++) {
      for (int x = 0; x < boxes[y].length; x++) {
        for (int i = 0; i < bullets.size(); i++) {
          if (boxes[y][x] != null) {
            Area area1 = new Area(boxes[y][x].getBounds());
            Rectangle2D bullet = bullets.get(i).getBounds2D();
            Area area2 = new Area(new Arc2D.Double(bullet.getX(), bullet.getY() - 15, bullet.getWidth(), bullet.getHeight(), 0, 360, Arc2D.CHORD));
            area1.intersect(area2);
            if (!area1.isEmpty()) {
              paddle.removeBullet(i);
              if (!boxes[y][x].isUnbreakable()) {
                remove(x, y);
              }
              i++;
            }
          }
        }
      }
    }

    // balls with blocks
    for (BallFX ball : balls) {
      if(ball.isDone()) {
        boolean hasHit = false;
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
                  ball.flipRandomlyVelY();
                } else if (!Boolean.logicalXor(ball.getBallState() != BallFX.BallStates.POWER_BALL, ball.getBallState() != BallFX.BallStates.DRUNK_POWER_BALL) && !hasHit) {
                  ball.flipRandomlyVelY();
                  hasHit = true;
                }
                remove(x, y);
              }
              if (rightLine.intersectsLine(ballPath) || leftLine.intersectsLine(ballPath)) {
                if (boxes[y][x] != null && boxes[y][x].isUnbreakable()) {
                  ball.flipRandomlyVelX();
                } else if (!Boolean.logicalXor(ball.getBallState() != BallFX.BallStates.POWER_BALL, ball.getBallState() != BallFX.BallStates.DRUNK_POWER_BALL) && !hasHit) {
                  ball.flipRandomlyVelX();
                  hasHit = true;
                }
                remove(x, y);
              }
            }
          }
        }
      }
    }

    // Power-ups with paddle
    for (int i = 0; i < powerUps.size(); i++) {
      if (powerUps.get(i) != null) {
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

    // balls with sides
    for (int i = 0; i < balls.size(); i++) {
      Line2D ballPath = new Line2D.Double(balls.get(i).getX(), balls.get(i).getY(), balls.get(i).getNextX(), balls.get(i).getNextY());
      Line2D lastLastBallPath = new Line2D.Double(balls.get(i).getSecondLastX(), balls.get(i).getSecondLastY(), balls.get(i).getThirdLastX(), balls.get(i).getThirdLastY());
      Line2D upLine = new Line2D.Double(-100, 80, 1920, 80);
      Line2D downLine = new Line2D.Double(-100, runFX.height, 1920, runFX.height);
      Line2D rightLine = new Line2D.Double(runFX.width, -100, runFX.width, 1080);
      Line2D leftLine = new Line2D.Double(0, -100, 0, 1080);
      if (upLine.intersectsLine(ballPath)) {
        balls.get(i).flipRandomlyVelY();
      }
      if (rightLine.intersectsLine(ballPath) || leftLine.intersectsLine(ballPath)) {
        balls.get(i).flipRandomlyVelX();
      }
      if (downLine.intersectsLine(lastLastBallPath)) {
        balls.remove(i);
        i--;
        if (balls.size() == 0) {
          balls = new ArrayList<BallFX>();
          balls.add(new BallFX(runFX.width / 2, 3 * runFX.height / 4, true));
          powerUps = new ArrayList<PowerUpFX>();
          if (lives > 0) {
            lifeParticles.get(5 - lives).kill();
          }
          lives--;
        }
      }
    }
  }

  private void remove(int x, int y) {
    if (boxes[y][x] != null && !boxes[y][x].isUnbreakable()) {
      score += 5;
      spawnShatters(boxes[y][x]);
      deadBoxes.add(new DeadBoxFX(boxes[y][x]));
      maybeSpawnPowerUp(boxes[y][x]);
      boxes[y][x] = null;
      enableScreenShake();
    } else {
      enableBigScreenShake();
    }
  }

  private void spawnShatters(BreakBoxFX boxFX) {
    for (int i = 0; i < 6; i++) {
      shatters.add(new ShatterParticleFX(boxFX));
    }
  }

  private void enableBigScreenShake() {
    if(!isCleaningBoard) {
      willBigScreenShake = true;
    }
  }

  private void enableScreenShake() {
    if(!isCleaningBoard) {
      willScreenShake = true;
    }
  }

  private void maybeSpawnPowerUp(BreakBoxFX box) {
    int random = (int) (100 * Math.random());
    if (random < 10) {
      PowerUpFX power = new PowerUpFX(box);
      powerUps.add(power);
    }
  }

  private void applyPowerUp(PowerUpFX powerUp, double x, double y) {
    score += 20;
    boolean isDone = false;
    Color startColor = Color.BLACK;
    Color endColor = Color.RED;
    String display = normalizeEnumField(powerUp.getPower().toString());
    for (int i = 0; i < balls.size() && !isDone; i++) {
      if (powerUp.getPower() == PowerUpFX.PowerUps.POWER_BALL) {
        endColor = Color.GREEN;
        if (balls.get(i).getBallState() == BallFX.BallStates.DRUNK_BALL) {
          balls.get(i).setBallState(BallFX.BallStates.DRUNK_POWER_BALL);
        } else if (balls.get(i).getBallState() == BallFX.BallStates.DRUNK_POWER_BALL) {
          balls.get(i).setBallState(BallFX.BallStates.DRUNK_POWER_BALL);
        } else {
          balls.get(i).setBallState(BallFX.BallStates.POWER_BALL);
        }
      } else if (powerUp.getPower() == PowerUpFX.PowerUps.SPEED_DOWN) {
        endColor = Color.RED;
        balls.get(i).decreaseSpeed();
      } else if (powerUp.getPower() == PowerUpFX.PowerUps.SPEED_UP) {
        endColor = Color.GREEN;
        balls.get(i).increaseSpeed();
      } else if (powerUp.getPower() == PowerUpFX.PowerUps.SHORTER_PADDLE) {
        endColor = Color.RED;
        paddle.decreaseLength();
        isDone = true;
      } else if (powerUp.getPower() == PowerUpFX.PowerUps.LONGER_PADDLE) {
        endColor = Color.GREEN;
        paddle.increaseLength();
        isDone = true;
      } else if (powerUp.getPower() == PowerUpFX.PowerUps.MAGNETIC_PADDLE) {
        endColor = Color.GREEN;
        paddle.magnetize();
        isDone = true;
      } else if (powerUp.getPower() == PowerUpFX.PowerUps.LIFE_UP) {
        endColor = Color.GREEN;
        if(lives <= 5)
          lives++;
        isDone = true;
      } else if (powerUp.getPower() == PowerUpFX.PowerUps.GUN_PADDLE) {
        endColor = Color.GREEN;
        paddle.attachGun();
        isDone = true;
      } else if (powerUp.getPower() == PowerUpFX.PowerUps.CONFUSION) {
        endColor = Color.RED;
        isConfused = true;
        confusionCounter = 0;
        isDone = true;
      } else if (powerUp.getPower() == PowerUpFX.PowerUps.SPLIT_BALLS) {
        endColor = Color.GREEN;
        splitBalls();
        isDone = true;
      } else if (powerUp.getPower() == PowerUpFX.PowerUps.DRUNK_BALL) {
        endColor = Color.RED;
        if (balls.get(i).getBallState() == BallFX.BallStates.POWER_BALL) {
          balls.get(i).setBallState(BallFX.BallStates.DRUNK_POWER_BALL);
        } else if (balls.get(i).getBallState() == BallFX.BallStates.DRUNK_POWER_BALL) {
          balls.get(i).setBallState(BallFX.BallStates.DRUNK_POWER_BALL);
        } else {
          balls.get(i).setBallState(BallFX.BallStates.DRUNK_BALL);
        }
      }
    }

    Font font = Font.font("Serif", FontWeight.BOLD, FontPosture.REGULAR, 24);
    double time = 100;
    TextParticleFX particle = new TextParticleFX(display, font, x - 23, y, 0.75, 0.75, 0, 0, 0, (int) time);
    particle.setDY(-time / 100);
    particle.add(new OutFadeAnimatorFX((float) (time / 5000)));
    particle.add(new ColorToColorAnimatorFX(startColor, endColor.brighter().brighter().brighter().brighter(), time / 3));
    texts.add(particle);
  }

  private void splitBalls() {
    int limit = balls.size();
    for (int i = 0; i < limit; i++) {
      BallFX ball = balls.get(i);
      balls.add(new BallFX(ball.getX(), ball.getY(), ball.getVelX() + Math.pow(2, 0.5), ball.getVelY() + Math.pow(2, 0.5), false));
    }
  }

  private String normalizeEnumField(String badlyFormatted) {
    String wellFormatted = "" + badlyFormatted.charAt(0);
    for (int i = 1; i < badlyFormatted.length(); i++) {
      if (badlyFormatted.charAt(i) == '_') {
        wellFormatted += " ";
      } else if (badlyFormatted.charAt(i - 1) == '_') {
        wellFormatted += badlyFormatted.charAt(i);
      } else {
        wellFormatted += (char) (badlyFormatted.charAt(i) + 32);
      }
    }
    return wellFormatted;
  }

  public static BufferedImage loadBufferedImage(String url) {
    BufferedImage image = null;
    try {
      if(BreakoutScreenFX.class.getResource(url) != null) {
        image = ImageIO.read(BreakoutScreenFX.class.getResource(url));
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }
    return image;
  }

  public static Image loadFXImage(String url) {
    try (InputStream stream = BreakoutScreenFX.class.getResource(url).openStream()) {
      return new Image(stream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void transferFiresToMenu(ArrayList<FireFlyFX> fires) {
    for (int i = 0; i < fires.size(); i++) {
      menuFX.fires3.add(fires.get(i));
    }
  }

  public void setLevel(int level) {
    this.level = level;
    loadBoxes("assets/levels/level" + level + ".png");
  }

  public int getLevel() {
    return level;
  }
}
