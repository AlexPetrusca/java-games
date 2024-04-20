import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class BreakoutRunFX extends Application {
  public enum BreakoutState {
    INTRO,
    GAME,
    MENU,
    CONTINUE
  }
  public static final int pixelScale = 25;
  public static final int height = pixelScale*35;
  public static final int width = (4*pixelScale)*13;
  private BreakoutScreenFX gameScreen;
  private Scene theScene;
  private GraphicsContext upperG;
  private GraphicsContext lowerG;
  private GraphicsContext UIG;
  private GraphicsContext glassG;
  private Stage theStage;
  public static Canvas lowerCanvas;
  public static Canvas upperCanvas;
  public static Canvas UICanvas;
  public static Canvas glassCanvas;
  BreakoutState state;
  BreakoutState lastState;

  public void start(Stage theStage) {
    theStage.setTitle("Breakout -- Alex Petrusca");
    state = BreakoutState.INTRO;
    lastState = BreakoutState.INTRO;

    gameScreen = new BreakoutScreenFX(this);
    theScene = new Scene(gameScreen, width, height, Color.WHITE);
    theStage.setScene(theScene);
    lowerCanvas = new Canvas(width, height);
    upperCanvas = new Canvas(width, height);
    UICanvas = new Canvas(width, height);
    glassCanvas = new Canvas(width, height);

    lowerCanvas.setCacheHint(CacheHint.SPEED);
    upperCanvas.setCacheHint(CacheHint.SPEED);
    UICanvas.setCacheHint(CacheHint.SPEED);
    glassCanvas.setCacheHint(CacheHint.SPEED);
    gameScreen.setCacheHint(CacheHint.SPEED);

    gameScreen.getChildren().add(lowerCanvas);
    gameScreen.getChildren().add(upperCanvas);
    gameScreen.getChildren().add(UICanvas);
    gameScreen.getChildren().add(glassCanvas);
    upperG = upperCanvas.getGraphicsContext2D();
    lowerG = lowerCanvas.getGraphicsContext2D();
    UIG = UICanvas.getGraphicsContext2D();
    glassG = glassCanvas.getGraphicsContext2D();
    this.theStage = theStage;
    gameScreen.addEventHandlers();

    final long startNanoTime = System.nanoTime();
    AnimationTimer gameLoop = new AnimationTimer() {
      public void handle(long currentNanoTime)
      {
        double t = (currentNanoTime - startNanoTime) / 1e9;
        gameScreen.runGame(t);
      }
    };
    gameLoop.start();
    theStage.show();
  }

  public Scene getScene() {
    return theScene;
  }

  public Stage getStage() {
    return theStage;
  }

  public GraphicsContext getUpperG() {
    return upperG;
  }

  public GraphicsContext getLowerG() {
    return lowerG;
  }

  public GraphicsContext getUIG() {
    return UIG;
  }

  public GraphicsContext getGlassG() {
    return glassG;
  }

  public static void main(String[] args) {
    java.awt.Toolkit.getDefaultToolkit();
    launch(args);
  }
}
