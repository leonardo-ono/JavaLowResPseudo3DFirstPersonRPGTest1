package rpg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Enemy class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Enemy {
    
    private double x = 36;
    private double y = 60;
    private BufferedImage monsterMap;

    public Enemy() {
        try {
            monsterMap = ImageIO.read(getClass()
                    .getResourceAsStream("monster_map.png"));
        } catch (IOException ex) {
            Logger.getLogger(LowResFirstPersonRPG.class.getName())
                        .log(java.util.logging.Level.SEVERE, null, ex);

            System.exit(-1);
        }
    }

    public void update() {
        y = 60 + 12 * Math.sin(System.nanoTime() * 0.00000001);
    }

    public void draw(Graphics2D g
            , AffineTransform cameraTransform, double playerDirection) {
        
        System.out.println("enemy draw");
        AffineTransform at = g.getTransform();
        g.transform(cameraTransform);
        g.translate(x, y);
        g.rotate(playerDirection - Math.toRadians(90));
        g.translate(-12, -2);
        g.setColor(Color.BLUE);
        g.drawImage(monsterMap, 0, 0, null);
        g.setTransform(at);
    }

}

