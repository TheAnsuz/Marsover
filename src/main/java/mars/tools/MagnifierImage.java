

package mars.tools;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.Stroke;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.image.ImageObserver;
import java.awt.Graphics;
import java.awt.event.MouseMotionListener;
import java.awt.Graphics2D;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Rectangle;
import javax.swing.JPanel;

class MagnifierImage extends JPanel
{
    private Magnifier frame;
    private Rectangle screenRectangle;
    private static Robot robot;
    private Image image;
    private Scribbler scribbler;
    
    public MagnifierImage(final Magnifier frame) {
        this.frame = frame;
        this.scribbler = new Scribbler(frame.scribblerSettings);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                MagnifierImage.this.scribbler.moveto(e.getX(), e.getY());
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(final MouseEvent e) {
                MagnifierImage.this.scribbler.lineto(e.getX(), e.getY(), (Graphics2D)MagnifierImage.this.getGraphics());
            }
        });
    }
    
    public Image getImage() {
        return this.image;
    }
    
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (this.image != null) {
            g.drawImage(this.image, 0, 0, this);
        }
    }
    
    public void setImage(final Image image) {
        this.image = image;
        this.setPreferredSize(new Dimension(image.getWidth(this), image.getHeight(this)));
        this.revalidate();
        this.repaint();
    }
    
    static Image getScaledImage(final Image image, final double scale, final int scaleAlgorithm) {
        return (scale < 1.01 && scale > 0.99) ? image : image.getScaledInstance((int)(image.getWidth(null) * scale), (int)(image.getHeight(null) * scale), scaleAlgorithm);
    }
    
    static Image getScaledImage(final Image image, final double scale) {
        return getScaledImage(image, scale, 1);
    }
    
    private class Scribbler
    {
        private ScribblerSettings scribblerSettings;
        private BasicStroke drawingStroke;
        protected int last_x;
        protected int last_y;
        
        Scribbler(final ScribblerSettings scribblerSettings) {
            this.scribblerSettings = scribblerSettings;
            this.drawingStroke = new BasicStroke((float)scribblerSettings.getLineWidth());
        }
        
        public Color getColor() {
            return this.scribblerSettings.getLineColor();
        }
        
        public int getLineWidth() {
            this.drawingStroke = new BasicStroke((float)this.scribblerSettings.getLineWidth());
            return this.scribblerSettings.getLineWidth();
        }
        
        public void setColor(final Color newColor) {
            this.scribblerSettings.setLineColor(newColor);
        }
        
        public void setLineWidth(final int newWidth) {
            this.scribblerSettings.setLineWidth(newWidth);
            this.drawingStroke = new BasicStroke((float)newWidth);
        }
        
        private BasicStroke getStroke() {
            return this.drawingStroke;
        }
        
        private void setStroke(final BasicStroke newStroke) {
            this.drawingStroke = newStroke;
        }
        
        public void moveto(final int x, final int y) {
            this.last_x = x;
            this.last_y = y;
        }
        
        public void lineto(final int x, final int y, final Graphics2D g2d) {
            g2d.setStroke(new BasicStroke((float)this.scribblerSettings.getLineWidth()));
            g2d.setColor(this.scribblerSettings.getLineColor());
            g2d.draw(new Line2D.Float((float)this.last_x, (float)this.last_y, (float)x, (float)y));
            this.moveto(x, y);
        }
    }
}
