

package mars;

import java.awt.image.ImageObserver;
import java.awt.Graphics;
import javax.swing.ImageIcon;
import java.awt.Image;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import java.awt.Toolkit;
import java.awt.Container;
import javax.swing.JWindow;

public class MarsSplashScreen extends JWindow
{
    private int duration;
    
    public MarsSplashScreen(final int d) {
        this.duration = d;
    }
    
    public void showSplash() {
        final ImageBackgroundPanel content = new ImageBackgroundPanel();
        this.setContentPane(content);
        final int width = 390;
        final int height = 215;
        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Dimension screen = tk.getScreenSize();
        final int x = (screen.width - width) / 2;
        final int y = (screen.height - height) / 2;
        this.setBounds(x, y, width, height);
        final JLabel title = new JLabel("MARS: Mips Assembler and Runtime Simulator", 0);
        final JLabel copyrt1 = new JLabel("<html><br><br>Version 4.5 Copyright (c) " + Globals.copyrightYears + "</html>", 0);
        final JLabel copyrt2 = new JLabel("<html><br><br>" + Globals.copyrightHolders + "</html>", 0);
        title.setFont(new Font("Sans-Serif", 1, 16));
        title.setForeground(Color.black);
        copyrt1.setFont(new Font("Sans-Serif", 1, 14));
        copyrt2.setFont(new Font("Sans-Serif", 1, 14));
        copyrt1.setForeground(Color.white);
        copyrt2.setForeground(Color.white);
        content.add(title, "North");
        content.add(copyrt1, "Center");
        content.add(copyrt2, "South");
        this.setVisible(true);
        try {
            Thread.sleep(this.duration);
        }
        catch (Exception ex) {}
        this.setVisible(false);
    }
    
    class ImageBackgroundPanel extends JPanel
    {
        Image image;
        
        public ImageBackgroundPanel() {
            try {
                this.image = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/images/MarsSurfacePathfinder.jpg"))).getImage();
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
        
        @Override
        protected void paintComponent(final Graphics g) {
            super.paintComponent(g);
            if (this.image != null) {
                g.drawImage(this.image, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        }
    }
}
