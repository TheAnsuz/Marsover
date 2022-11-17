

package mars.tools;

public class ScreenMagnifier implements MarsTool
{
    @Override
    public String getName() {
        return "Screen Magnifier";
    }
    
    @Override
    public void action() {
        final Magnifier mag = new Magnifier();
    }
    
    public static void main(final String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new ScreenMagnifier().action();
            }
        }).start();
    }
}
