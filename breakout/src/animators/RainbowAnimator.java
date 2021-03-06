package animators;

import java.awt.*;

/**
 * Created by PetruscaFamily on 2/24/2016.
 */
public class RainbowAnimator extends Animator {
  private float color = 0.0f;
  private int redCounter = 0;
  private float deltaColor;
  private boolean halfDone = false;

  public RainbowAnimator(float deltaColor) {
    this.deltaColor = deltaColor;
    alpha = 1.00f;
  }

  public void animate(Graphics2D g) {
    if(redCounter > 100) {
      if (color > .98f) {
        halfDone = true;
      }
      if (halfDone && color < 0.2f) {
        halfDone = false;
      }
      if (halfDone) {
        color -= deltaColor;
      } else {
        color += deltaColor;
      }
    } else {
      redCounter++;
    }
    g.setColor(Color.getHSBColor(color, 1.0f, 1.0f));
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
  }

  public Color getColor() {
    return Color.getHSBColor(color, 1.0f, 1.0f);
  }
}
