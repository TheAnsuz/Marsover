

package mars.venus;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.Action;
import javax.swing.Timer;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import javax.swing.JButton;

public class RepeatButton extends JButton implements ActionListener, MouseListener
{
    private boolean pressed;
    private boolean repeatEnabled;
    private Timer timer;
    private int initialDelay;
    private int delay;
    private int modifiers;
    private static boolean testing;
    
    public RepeatButton() {
        this.pressed = false;
        this.repeatEnabled = true;
        this.timer = null;
        this.initialDelay = 300;
        this.delay = 60;
        this.modifiers = 0;
        this.init();
    }
    
    public RepeatButton(final Action a) {
        super(a);
        this.pressed = false;
        this.repeatEnabled = true;
        this.timer = null;
        this.initialDelay = 300;
        this.delay = 60;
        this.modifiers = 0;
        this.init();
    }
    
    public RepeatButton(final Icon icon) {
        super(icon);
        this.pressed = false;
        this.repeatEnabled = true;
        this.timer = null;
        this.initialDelay = 300;
        this.delay = 60;
        this.modifiers = 0;
        this.init();
    }
    
    public RepeatButton(final String text) {
        super(text);
        this.pressed = false;
        this.repeatEnabled = true;
        this.timer = null;
        this.initialDelay = 300;
        this.delay = 60;
        this.modifiers = 0;
        this.init();
    }
    
    public RepeatButton(final String text, final Icon icon) {
        super(text, icon);
        this.pressed = false;
        this.repeatEnabled = true;
        this.timer = null;
        this.initialDelay = 300;
        this.delay = 60;
        this.modifiers = 0;
        this.init();
    }
    
    private void init() {
        this.addMouseListener(this);
        (this.timer = new Timer(this.delay, this)).setRepeats(true);
    }
    
    public int getDelay() {
        return this.delay;
    }
    
    public void setDelay(final int d) {
        this.delay = d;
    }
    
    public int getInitialDelay() {
        return this.initialDelay;
    }
    
    public void setInitialDelay(final int d) {
        this.initialDelay = d;
    }
    
    public boolean isRepeatEnabled() {
        return this.repeatEnabled;
    }
    
    public void setRepeatEnabled(final boolean en) {
        if (!en) {
            this.pressed = false;
            if (this.timer.isRunning()) {
                this.timer.stop();
            }
        }
        this.repeatEnabled = en;
    }
    
    @Override
    public void setEnabled(final boolean en) {
        if (en != super.isEnabled()) {
            this.pressed = false;
            if (this.timer.isRunning()) {
                this.timer.stop();
            }
        }
        super.setEnabled(en);
    }
    
    @Override
    public void actionPerformed(final ActionEvent ae) {
        if (ae.getSource() == this.timer) {
            final ActionEvent event = new ActionEvent(this, 1001, super.getActionCommand(), this.modifiers);
            super.fireActionPerformed(event);
        }
        else if (RepeatButton.testing && ae.getSource() == this) {
            System.out.println(ae.getActionCommand());
        }
    }
    
    @Override
    public void mouseClicked(final MouseEvent me) {
        if (me.getSource() == this) {
            this.pressed = false;
            if (this.timer.isRunning()) {
                this.timer.stop();
            }
        }
    }
    
    @Override
    public void mousePressed(final MouseEvent me) {
        if (me.getSource() == this && this.isEnabled() && this.isRepeatEnabled()) {
            this.pressed = true;
            if (!this.timer.isRunning()) {
                this.modifiers = me.getModifiers();
                this.timer.setInitialDelay(this.initialDelay);
                this.timer.start();
            }
        }
    }
    
    @Override
    public void mouseReleased(final MouseEvent me) {
        if (me.getSource() == this) {
            this.pressed = false;
            if (this.timer.isRunning()) {
                this.timer.stop();
            }
        }
    }
    
    @Override
    public void mouseEntered(final MouseEvent me) {
        if (me.getSource() == this && this.isEnabled() && this.isRepeatEnabled() && this.pressed && !this.timer.isRunning()) {
            this.modifiers = me.getModifiers();
            this.timer.setInitialDelay(this.delay);
            this.timer.start();
        }
    }
    
    @Override
    public void mouseExited(final MouseEvent me) {
        if (me.getSource() == this && this.timer.isRunning()) {
            this.timer.stop();
        }
    }
    
    public static void main(final String[] args) {
        RepeatButton.testing = true;
        final JFrame f = new JFrame("RepeatButton Test");
        f.setDefaultCloseOperation(3);
        final JPanel p = new JPanel();
        final RepeatButton b = new RepeatButton("hold me");
        b.setActionCommand("test");
        b.addActionListener(b);
        p.add(b);
        f.getContentPane().add(p);
        f.pack();
        f.setVisible(true);
    }
    
    static {
        RepeatButton.testing = false;
    }
}
