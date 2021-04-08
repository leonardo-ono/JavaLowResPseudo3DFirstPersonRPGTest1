package rpg;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Low Res First Person RPG
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class LowResFirstPersonRPG extends JPanel {
    
    private double playerX = 99, playerY = 131;
    private double playerDirection = 3.154;
    
    private final BufferedImage offscreen;
    private final BufferedImage offscreen2;

    private final BufferedImage floorOffscreen;
    private final BufferedImage floorOffscreen2;
    
    private BufferedImage map;
    private BufferedImage mapFloor;
    private BufferedImage[] bricks = new BufferedImage[4];
    
    private AffineTransform cameraTransform = new AffineTransform();
    
    private final Graphics2D g1;
    private final Graphics2D fg1;
    
    private long previousTime = -1;
    
    private List<Enemy> enemies = new ArrayList<>();
    
    public LowResFirstPersonRPG() {
        try {
            map = ImageIO.read(getClass().getResourceAsStream("map24.png"));
            mapFloor = ImageIO.read(getClass()
                    .getResourceAsStream("floor.png"));
            
            bricks[0] = ImageIO.read(getClass()
                    .getResourceAsStream("brick.png"));
            
            bricks[1] = ImageIO.read(getClass()
                    .getResourceAsStream("brick2.png"));
            
            bricks[2] = ImageIO.read(getClass()
                    .getResourceAsStream("brick3.png"));
            
            bricks[3] = ImageIO.read(getClass()
                    .getResourceAsStream("monster.png"));
            
        } catch (IOException ex) {
            Logger.getLogger(LowResFirstPersonRPG.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
            
            System.exit(-1);
        }
        
        offscreen = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        offscreen2 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        g1 = (Graphics2D) offscreen.getGraphics();

        floorOffscreen = new BufferedImage(
                256, 256, BufferedImage.TYPE_INT_ARGB);
        
        floorOffscreen2 = new BufferedImage(
                256, 256, BufferedImage.TYPE_INT_ARGB);
        
        fg1 = (Graphics2D) floorOffscreen.getGraphics();
    }
    
    
    public void start() {
        addKeyListener(new Input());
        enemies.add(new Enemy());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        update();
        enemies.forEach(enemy -> enemy.update());

        g1.setBackground(new Color(0, 0, 0, 0));
        g1.clearRect(0, 0, 256, 256);
        
        fg1.setBackground(new Color(0, 0, 0, 0));
        fg1.clearRect(0, 0, 256, 256);

        cameraTransform.setToIdentity();
        cameraTransform.translate(128, 256);
        cameraTransform.rotate(-playerDirection - Math.toRadians(90));
        cameraTransform.translate(-playerX, -playerY);

        g1.drawImage(map, cameraTransform, null);
        fg1.drawImage(mapFloor, cameraTransform, null);
        
        Graphics2D fg2 = (Graphics2D) floorOffscreen2.getGraphics();
        fg2.setColor(Color.BLACK);
        fg2.fillRect(0, 0, 256, 256);
        
        Graphics2D g2 = (Graphics2D) offscreen2.getGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 256, 256);
        
        enemies.forEach(enemy 
                -> enemy.draw(g1, cameraTransform, playerDirection));
        
        for (int y = 0; y < 250; y += 1) {
            double scale = y / 256.0;
            double height1 = 10.0 / (1.0 - y / 256.0);
            double height2 = 10.0 / (1.0 - (y + 1) / 256.0);
            int x1 = (int) (128 * scale);
            int x2 = (int) (256 - 128 * scale);
            g2.drawImage(offscreen, 0, y, 256, y + 1, x1, y, x2, y + 1, null);
            fg2.drawImage(floorOffscreen, 0, 128 + (int) (height1)
                    , 256, 128 + (int) (height2), x1, y, x2, y + 1, null);
        }
        
        g1.clearRect(0, 0, 256, 256);
        
        outer:
        for (int x = 0; x < 256; x++) {
            for (int y = 255; y >= 0; y--) {
                int c = offscreen2.getRGB(x, y);
                if (c == -16777216) {
                    offscreen2.setRGB(x, y, Color.BLUE.getRGB());
                }
                else {
                    double height = 10.0 / (1.0 - y / 256.0);
                    int textureIndex = (c >> 8) & 255; // green
                    if (textureIndex < 4) { 
                        int dx1 = x;
                        int dy1 = (int) (128 - height);
                        int dx2 = dx1 + 1;
                        int dy2 = (int) (128 + height);
                        int sx1 = (c & 255);
                        int sy1 = 0;
                        int sx2 = sx1 + 1;
                        int sy2 = 24;

                        Composite oc = g1.getComposite();
                        g1.setComposite(AlphaComposite.DstOver);
                        g1.drawImage(bricks[textureIndex]
                                , dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
                        
                        g1.setComposite(oc);
                    }

                    if (textureIndex < 2) {
                        continue outer;
                    }
                }
            }
        }
        
        fg2.drawImage(floorOffscreen2, 0, 128, 256, 0, 0, 128, 256, 256, this);
        fg2.drawImage(offscreen, 0, 0, null);
        g.drawImage(floorOffscreen2, 0, -100, 800, 800, null);
        
        long currentTime = System.currentTimeMillis();
        long waitTime = 1000 / 60;
        if (previousTime > 0) {
            waitTime = 1000 / 60 - (currentTime - previousTime);
            if (waitTime < 0) waitTime = 0;
        }
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ex) {
        }
        previousTime = System.currentTimeMillis();
            
        repaint();
    }

    private void update() {
        double strafe = 0;
        double speed = 1.25;
        double speedTmp = 0;

        if (Input.isKeyPressed(KeyEvent.VK_Z)) {
            strafe = Math.toRadians(-90);
            speedTmp = speed;
        }
        if (Input.isKeyPressed(KeyEvent.VK_X)) {
            strafe = Math.toRadians(90);
            speedTmp = speed;
        }

        if (Input.isKeyPressed(KeyEvent.VK_UP)) {
            strafe = 0;
            speedTmp = speed;
        }
        if (Input.isKeyPressed(KeyEvent.VK_DOWN)) {
            strafe = 0;
            speedTmp = -speed;
        }

        if (Input.isKeyPressed(KeyEvent.VK_LEFT)) {
            playerDirection -= 0.075;
        }
        if (Input.isKeyPressed(KeyEvent.VK_RIGHT)) {
            playerDirection += 0.075;
        }

        double playerXTmp = playerX + speedTmp 
                * Math.cos(playerDirection + strafe);
        
        if (!collidingWithWall((int) playerXTmp, (int) playerY)) {
            playerX = playerXTmp;
        }
        double playerYTmp = playerY + speedTmp 
                * Math.sin(playerDirection + strafe);
        
        if (!collidingWithWall((int) playerX, (int) playerYTmp)) {
            playerY = playerYTmp;
        }
        
        System.out.println("player position: " + playerX + ", " + playerY 
                                        + " direction: " + playerDirection);
    }
    
    private boolean collidingWithWall(double nx, double ny) {
        return map.getRGB((int) (nx - 6), (int) (ny - 6)) != 0
                || map.getRGB((int) (nx + 6), (int) (ny - 6)) != 0
                || map.getRGB((int) (nx - 6), (int) (ny + 6)) != 0
                || map.getRGB((int) (nx + 6), (int) (ny + 6)) != 0;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LowResFirstPersonRPG view = new LowResFirstPersonRPG();
            view.setPreferredSize(new Dimension(800, 600));
            JFrame frame = new JFrame(
                    "Java 2.5D Low Res First Person RPG Test");
            
            frame.getContentPane().add(view);
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            view.start();
            view.requestFocus();
        });
    }
    
}
