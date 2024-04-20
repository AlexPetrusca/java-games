import org.age.animation.Animator;
import org.age.panel.AbstractGamePanel;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by PetruscaFamily on 5/22/2015.
 */
public class PongGame extends AbstractGamePanel implements Animator {
  private static final int MAX_BOUNCE_ANGLE = 60;
  PongRun game;
  Image back;
  Ball ball;
  PlayerBat player;
  EnemyBat enemy;

  public PongGame(PongRun game) {
    back = Toolkit.getDefaultToolkit().getImage(getClass().getResource("images/game.jpg"));
    ball = new Ball(200, 200, 12, -30);
    player = new PlayerBat(950, 704/2, 3, 60, 10);
    enemy = new EnemyBat(20, 704/2, 3, 60, 10);
    this.game = game;
    game.requestFocus();
  }

  @Override
  public void processInput(boolean[] keys) {
    if(keys[KeyEvent.VK_UP]) {
      player.y -= player.velocity;
      if (player.y <= 0) {
        player.y = 0;
      }
    }
    if(keys[KeyEvent.VK_DOWN]) {
      player.y += player.velocity;
      if (player.y + player.batLength >= 704) {
        player.y = 712 - player.batLength;
      }
    }

    if(keys[KeyEvent.VK_W]) {
      enemy.y -= enemy.velocity;
      if (enemy.y <= 0) {
        enemy.y = 0;
      }
    }
    if(keys[KeyEvent.VK_S]) {
      enemy.y += enemy.velocity;
      if (enemy.y + enemy.batLength >= 704) {
        enemy.y = 712 - enemy.batLength;
      }
    }
  }

  @Override
  public void animate() {
    ball.x += ball.xVelocity;
    ball.y += ball.yVelocity;
    if (ball.y <= 0 || ball.y >= 704) {
      ball.angle = - ball.angle;
    }
    if (ball.x <= 0 || ball.x >= 977) {
      ball.angle += 180 - 2 *ball.angle;
    }

    int playerDY = (player.y + player.batLength/2) - (ball.y + 5);
    int enemyDY = (enemy.y + enemy.batLength/2) - (ball.y + 5);
    if(ball.nextRect.intersects(player.rect)) {
      ball.angle = (double) playerDY/(player.batLength/2) * MAX_BOUNCE_ANGLE;
      ball.angle += 180 - 2 *ball.angle;
    }
    if(ball.nextRect.intersects(enemy.rect)) {
      ball.angle = (double) enemyDY/(enemy.batLength/2) * MAX_BOUNCE_ANGLE;
    }
  }

  @Override
  public void paint(Graphics2D g) {
    g.drawImage(back, 0, 0, null);
    g.setColor(Color.WHITE);
    ball.paint(g);
    player.paint(g);
    enemy.paint(g);
  }
}
