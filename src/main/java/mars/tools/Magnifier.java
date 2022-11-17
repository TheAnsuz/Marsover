

package mars.tools;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mars.Globals;

class Magnifier extends JFrame implements ComponentListener
{
    static Robot robot;
    JButton close;
    JButton capture;
    JButton settings;
    JSpinner scaleAdjuster;
    JScrollPane view;
    Dimension frameSize;
    Dimension viewSize;
    MagnifierImage magnifierImage;
    ActionListener captureActionListener;
    CaptureModel captureResize;
    CaptureModel captureMove;
    CaptureModel captureRescale;
    CaptureModel captureDisplayCenter;
    CaptureModel captureDisplayUpperleft;
    CaptureModel dialogDisplayCenter;
    ScribblerSettings scribblerSettings;
    static final double SCALE_MINIMUM = 1.0;
    static final double SCALE_MAXIMUM = 4.0;
    static final double SCALE_INCREMENT = 0.5;
    static final double SCALE_DEFAULT = 2.0;
    double scale;
    CaptureDisplayAlignmentStrategy alignment;
    CaptureRectangleStrategy captureLocationSize;
    JFrame frame;
    static final String CAPTURE_TOOLTIP_TEXT = "Capture, scale, and display pixels that lay beneath the Magnifier.";
    static final String SETTINGS_TOOLTIP_TEXT = "Show dialog for changing tool settings.";
    static final String SCALE_TOOLTIP_TEXT = "Magnification scale for captured image.";
    static final String CLOSE_TOOLTIP_TEXT = "Exit the Screen Magnifier.  Changed settings are NOT retained.";
    
    Magnifier() {
        super("Screen Magnifier 1.0");
        this.scale = 2.0;
        this.captureLocationSize = new CaptureMagnifierRectangle();
        ((Magnifier)(this.frame = this)).createSettings();
        try {
            this.setIconImage(Globals.getGui().getIconImage());
        }
        catch (Exception ex) {}
        this.getContentPane().setLayout(new BorderLayout());
        this.addComponentListener(this);
        try {
            Magnifier.robot = new Robot();
        }
        catch (AWTException ex2) {}
        catch (SecurityException ex3) {}
        (this.close = new JButton("Close")).setToolTipText("Exit the Screen Magnifier.  Changed settings are NOT retained.");
        this.close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                Magnifier.this.setVisible(false);
            }
        });
        (this.settings = new JButton("Settings...")).setToolTipText("Show dialog for changing tool settings.");
        this.settings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                new SettingsDialog(Magnifier.this.frame);
            }
        });
        this.magnifierImage = new MagnifierImage(this);
        this.view = new JScrollPane(this.magnifierImage);
        this.viewSize = new Dimension(200, 150);
        this.view.setSize(this.viewSize);
        (this.capture = new JButton("Capture")).setToolTipText("Capture, scale, and display pixels that lay beneath the Magnifier.");
        this.captureActionListener = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                Magnifier.this.magnifierImage.setImage(MagnifierImage.getScaledImage(Magnifier.this.captureScreenSection(Magnifier.this.captureLocationSize.getCaptureRectangle(Magnifier.this.getFrameRectangle())), Magnifier.this.scale));
                Magnifier.this.alignment.setScrollBarValue(Magnifier.this.view.getHorizontalScrollBar());
                Magnifier.this.alignment.setScrollBarValue(Magnifier.this.view.getVerticalScrollBar());
            }
        };
        final JLabel scaleLabel = new JLabel("Scale: ");
        final SpinnerModel scaleModel = new SpinnerNumberModel(2.0, 1.0, 4.0, 0.5);
        (this.scaleAdjuster = new JSpinner(scaleModel)).setToolTipText("Magnification scale for captured image.");
        final JSpinner.NumberEditor scaleEditor = new JSpinner.NumberEditor(this.scaleAdjuster, "0.0");
        scaleEditor.getTextField().setEditable(false);
        this.scaleAdjuster.setEditor(scaleEditor);
        this.scaleAdjuster.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                Magnifier.this.scale = (double)Magnifier.this.scaleAdjuster.getValue();
                if (Magnifier.this.captureRescale.isEnabled()) {
                    Magnifier.this.captureActionListener.actionPerformed(new ActionEvent(Magnifier.this.frame, 0, "capture"));
                }
            }
        });
        final JPanel scalePanel = new JPanel();
        scalePanel.add(scaleLabel);
        scalePanel.add(this.scaleAdjuster);
        this.capture.addActionListener(this.captureActionListener);
        final Box buttonRow = Box.createHorizontalBox();
        buttonRow.add(Box.createHorizontalStrut(4));
        buttonRow.add(this.capture);
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(this.settings);
        buttonRow.add(scalePanel);
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(this.getHelpButton());
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(this.close);
        buttonRow.add(Box.createHorizontalStrut(4));
        this.getContentPane().add(this.view, "Center");
        this.getContentPane().add(buttonRow, "South");
        this.pack();
        this.setSize(500, 400);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.captureActionListener.actionPerformed(new ActionEvent(this.frame, 0, "capture"));
        this.captureActionListener.actionPerformed(new ActionEvent(this.frame, 0, "capture"));
    }
    
    private void createSettings() {
        this.captureResize = new CaptureModel(false);
        this.captureMove = new CaptureModel(false);
        this.captureRescale = new CaptureModel(true);
        this.alignment = new CaptureDisplayCentered();
        this.captureDisplayCenter = new CaptureModel(this.alignment instanceof CaptureDisplayCentered);
        this.captureDisplayUpperleft = new CaptureModel(this.alignment instanceof CaptureDisplayUpperleft);
        this.scribblerSettings = new ScribblerSettings(2, Color.RED);
        this.dialogDisplayCenter = new CaptureModel(true);
    }
    
    private JButton getHelpButton() {
        final String helpContent = "Use this utility tool to display a magnified image of a\nscreen section and highlight things on the image.  This\nwill be of interest mainly to instructors.\n\nTo capture an image, size and position the Screen Magnifier\nover the screen segment to be magnified and click \"Capture\".\nThe pixels beneath the magnifier will be captured, scaled,\nand displayed in a scrollable window.\n\nTo highlight things in the image, just drag the mouse over\nthe image to make a scribble line.  This line is ephemeral\n(is not repainted if covered then uncovered).\n\nThe magnification scale can be adjusted using the spinner.\nOther settings can be adjusted through the Settings dialog.\nSettings include: justification of displayed image, automatic\ncapture upon certain tool events, and the thickness and color\nof the scribble line.\n\nLIMITS: The image is static; it is not updated when the\nunderlying pixels change.  Scale changes do not take effect\nuntil the next capture (but you can set auto-capture).  The\nMagnifier does not capture frame contents of other tools.\nSetting changes are not saved when the tool is closed.\n\nContact Pete Sanderson at psanderson@otterbein.edu with\nquestions or comments.\n";
        final JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                JOptionPane.showMessageDialog(Magnifier.this.frame, "Use this utility tool to display a magnified image of a\nscreen section and highlight things on the image.  This\nwill be of interest mainly to instructors.\n\nTo capture an image, size and position the Screen Magnifier\nover the screen segment to be magnified and click \"Capture\".\nThe pixels beneath the magnifier will be captured, scaled,\nand displayed in a scrollable window.\n\nTo highlight things in the image, just drag the mouse over\nthe image to make a scribble line.  This line is ephemeral\n(is not repainted if covered then uncovered).\n\nThe magnification scale can be adjusted using the spinner.\nOther settings can be adjusted through the Settings dialog.\nSettings include: justification of displayed image, automatic\ncapture upon certain tool events, and the thickness and color\nof the scribble line.\n\nLIMITS: The image is static; it is not updated when the\nunderlying pixels change.  Scale changes do not take effect\nuntil the next capture (but you can set auto-capture).  The\nMagnifier does not capture frame contents of other tools.\nSetting changes are not saved when the tool is closed.\n\nContact Pete Sanderson at psanderson@otterbein.edu with\nquestions or comments.\n");
            }
        });
        return help;
    }
    
    BufferedImage captureScreenSection(final Rectangle section) {
        this.setVisible(false);
        try {
            Globals.getGui().update(Globals.getGui().getGraphics());
        }
        catch (Exception ex) {}
        final BufferedImage imageOfSection = Magnifier.robot.createScreenCapture(section);
        this.setVisible(true);
        return imageOfSection;
    }
    
    Rectangle getFrameRectangle() {
        return new Rectangle(this.getLocation().x, this.getLocation().y, this.getSize().width, this.getSize().height);
    }
    
    Rectangle getScreenRectangle() {
        return new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    }
    
    @Override
    public void componentMoved(final ComponentEvent e) {
        if (this.captureMove.isEnabled()) {
            this.captureActionListener.actionPerformed(new ActionEvent(e.getComponent(), e.getID(), "capture"));
        }
    }
    
    @Override
    public void componentResized(final ComponentEvent e) {
        if (this.captureResize.isEnabled()) {
            this.captureActionListener.actionPerformed(new ActionEvent(e.getComponent(), e.getID(), "capture"));
        }
    }
    
    @Override
    public void componentShown(final ComponentEvent e) {
    }
    
    @Override
    public void componentHidden(final ComponentEvent e) {
    }
}
