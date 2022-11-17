

package mars.simulator;

import javax.swing.SwingUtilities;

public abstract class SwingWorker
{
    private Object value;
    private ThreadVar threadVar;
    
    protected synchronized Object getValue() {
        return this.value;
    }
    
    private synchronized void setValue(final Object x) {
        this.value = x;
    }
    
    public abstract Object construct();
    
    public void finished() {
    }
    
    public void interrupt() {
        final Thread t = this.threadVar.get();
        if (t != null) {
            t.interrupt();
        }
        this.threadVar.clear();
    }
    
    public Object get() {
        while (true) {
            final Thread t = this.threadVar.get();
            if (t == null) {
                break;
            }
            try {
                t.join();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return this.getValue();
    }
    
    public SwingWorker(final boolean useSwing) {
        final Runnable doFinished = new Runnable() {
            @Override
            public void run() {
                SwingWorker.this.finished();
            }
        };
        final Runnable doConstruct = new Runnable() {
            @Override
            public void run() {
                try {
                    SwingWorker.this.setValue(SwingWorker.this.construct());
                }
                finally {
                    SwingWorker.this.threadVar.clear();
                }
                if (useSwing) {
                    SwingUtilities.invokeLater(doFinished);
                }
                else {
                    doFinished.run();
                }
            }
        };
        final Thread t = new Thread(doConstruct, "MIPS");
        this.threadVar = new ThreadVar(t);
    }
    
    public void start() {
        final Thread t = this.threadVar.get();
        if (t != null) {
            t.start();
        }
    }
    
    private static class ThreadVar
    {
        private Thread thread;
        
        ThreadVar(final Thread t) {
            this.thread = t;
        }
        
        synchronized Thread get() {
            return this.thread;
        }
        
        synchronized void clear() {
            this.thread = null;
        }
    }
}
