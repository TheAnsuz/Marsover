

package mars.venus;

import java.io.FileReader;
import java.awt.Toolkit;
import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;
import java.awt.PageAttributes;
import java.awt.JobAttributes;
import java.awt.Frame;
import java.util.Properties;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.PrintJob;
import java.io.Writer;

public class HardcopyWriter extends Writer
{
    protected PrintJob job;
    protected Graphics page;
    protected String jobname;
    protected int fontsize;
    protected String time;
    protected Dimension pagesize;
    protected int pagedpi;
    protected Font font;
    protected Font headerfont;
    protected FontMetrics metrics;
    protected FontMetrics headermetrics;
    protected int x0;
    protected int y0;
    protected int width;
    protected int height;
    protected int headery;
    protected int charwidth;
    protected int lineheight;
    protected int lineascent;
    protected int chars_per_line;
    protected int lines_per_page;
    protected int chars_per_tab;
    protected int charnum;
    protected int linenum;
    protected int pagenum;
    private boolean last_char_was_return;
    protected static Properties printprops;
    
    public HardcopyWriter(final Frame frame, final String jobname, int fontsize, final double leftmargin, final double rightmargin, final double topmargin, final double bottommargin) throws PrintCanceledException {
        this.chars_per_tab = 4;
        this.charnum = 0;
        this.linenum = 0;
        this.pagenum = 0;
        this.last_char_was_return = false;
        final Toolkit toolkit = frame.getToolkit();
        synchronized (HardcopyWriter.printprops) {
            final JobAttributes ja = new JobAttributes();
            final PageAttributes pa = new PageAttributes();
            this.job = toolkit.getPrintJob(frame, jobname, ja, pa);
        }
        if (this.job == null) {
            throw new PrintCanceledException("User cancelled print request");
        }
        this.pagedpi = 72;
        this.pagesize = new Dimension((int)(8.5 * this.pagedpi), 11 * this.pagedpi);
        fontsize = fontsize * this.pagedpi / 72;
        this.x0 = (int)(leftmargin * this.pagedpi);
        this.y0 = (int)(topmargin * this.pagedpi);
        this.width = this.pagesize.width - (int)((leftmargin + rightmargin) * this.pagedpi);
        this.height = this.pagesize.height - (int)((topmargin + bottommargin) * this.pagedpi);
        this.font = new Font("Monospaced", 0, fontsize);
        this.metrics = frame.getFontMetrics(this.font);
        this.lineheight = this.metrics.getHeight();
        this.lineascent = this.metrics.getAscent();
        this.charwidth = this.metrics.charWidth('0');
        this.chars_per_line = this.width / this.charwidth;
        this.lines_per_page = this.height / this.lineheight;
        this.headerfont = new Font("SansSerif", 2, fontsize);
        this.headermetrics = frame.getFontMetrics(this.headerfont);
        this.headery = this.y0 - (int)(0.125 * this.pagedpi) - this.headermetrics.getHeight() + this.headermetrics.getAscent();
        final DateFormat df = DateFormat.getDateTimeInstance(1, 3);
        df.setTimeZone(TimeZone.getDefault());
        this.time = df.format(new Date());
        this.jobname = jobname;
        this.fontsize = fontsize;
    }
    
    @Override
    public void write(final char[] buffer, final int index, final int len) {
        synchronized (this.lock) {
            for (int i = index; i < index + len; ++i) {
                if (this.page == null) {
                    this.newpage();
                }
                if (buffer[i] == '\n') {
                    if (!this.last_char_was_return) {
                        this.newline();
                    }
                }
                else if (buffer[i] == '\r') {
                    this.newline();
                    this.last_char_was_return = true;
                }
                else {
                    this.last_char_was_return = false;
                    if (!Character.isWhitespace(buffer[i]) || Character.isSpaceChar(buffer[i]) || buffer[i] == '\t') {
                        if (this.charnum >= this.chars_per_line) {
                            this.newline();
                            if (this.page == null) {
                                this.newpage();
                            }
                        }
                        if (Character.isSpaceChar(buffer[i])) {
                            ++this.charnum;
                        }
                        else if (buffer[i] == '\t') {
                            this.charnum += this.chars_per_tab - this.charnum % this.chars_per_tab;
                        }
                        else {
                            this.page.drawChars(buffer, i, 1, this.x0 + this.charnum * this.charwidth, this.y0 + this.linenum * this.lineheight + this.lineascent);
                            ++this.charnum;
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void flush() {
    }
    
    @Override
    public void close() {
        synchronized (this.lock) {
            if (this.page != null) {
                this.page.dispose();
            }
            this.job.end();
        }
    }
    
    public void setFontStyle(final int style) {
        synchronized (this.lock) {
            final Font current = this.font;
            try {
                this.font = new Font("Monospaced", style, this.fontsize);
            }
            catch (Exception e) {
                this.font = current;
            }
            if (this.page != null) {
                this.page.setFont(this.font);
            }
        }
    }
    
    public void pageBreak() {
        synchronized (this.lock) {
            this.newpage();
        }
    }
    
    public int getCharactersPerLine() {
        return this.chars_per_line;
    }
    
    public int getLinesPerPage() {
        return this.lines_per_page;
    }
    
    protected void newline() {
        this.charnum = 0;
        ++this.linenum;
        if (this.linenum >= this.lines_per_page) {
            this.page.dispose();
            this.page = null;
        }
    }
    
    protected void newpage() {
        this.page = this.job.getGraphics();
        this.linenum = 0;
        this.charnum = 0;
        ++this.pagenum;
        this.page.setFont(this.headerfont);
        this.page.drawString(this.jobname, this.x0, this.headery);
        final String s = "- " + this.pagenum + " -";
        int w = this.headermetrics.stringWidth(s);
        this.page.drawString(s, this.x0 + (this.width - w) / 2, this.headery);
        w = this.headermetrics.stringWidth(this.time);
        this.page.drawString(this.time, this.x0 + this.width - w, this.headery);
        final int y = this.headery + this.headermetrics.getDescent() + 1;
        this.page.drawLine(this.x0, y, this.x0 + this.width, y);
        this.page.setFont(this.font);
    }
    
    public static void main(final String[] args) {
        try {
            if (args.length != 1) {
                throw new IllegalArgumentException("Wrong # of arguments");
            }
            final FileReader in = new FileReader(args[0]);
            HardcopyWriter out = null;
            final Frame f = new Frame("PrintFile: " + args[0]);
            f.setSize(200, 50);
            f.setVisible(true);
            try {
                out = new HardcopyWriter(f, args[0], 10, 0.5, 0.5, 0.5, 0.5);
            }
            catch (PrintCanceledException e2) {
                System.exit(0);
            }
            f.setVisible(false);
            final char[] buffer = new char[4096];
            int numchars;
            while ((numchars = in.read(buffer)) != -1) {
                out.write(buffer, 0, numchars);
            }
            in.close();
            out.close();
        }
        catch (Exception e) {
            System.err.println(e);
            System.err.println("Usage: java HardcopyWriter$PrintFile <filename>");
            System.exit(1);
        }
        System.exit(0);
    }
    
    static {
        HardcopyWriter.printprops = new Properties();
    }
    
    public static class PrintCanceledException extends Exception
    {
        public PrintCanceledException(final String msg) {
            super(msg);
        }
    }
}
