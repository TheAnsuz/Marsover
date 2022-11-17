

package mars.mips.instructions.syscalls;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class ToneGenerator
{
    public static final byte DEFAULT_PITCH = 60;
    public static final int DEFAULT_DURATION = 1000;
    public static final byte DEFAULT_INSTRUMENT = 0;
    public static final byte DEFAULT_VOLUME = 100;
    private static Executor threadPool;
    
    public void generateTone(final byte pitch, final int duration, final byte instrument, final byte volume) {
        final Runnable tone = new Tone(pitch, duration, instrument, volume);
        ToneGenerator.threadPool.execute(tone);
    }
    
    public void generateToneSynchronously(final byte pitch, final int duration, final byte instrument, final byte volume) {
        final Runnable tone = new Tone(pitch, duration, instrument, volume);
        tone.run();
    }
    
    static {
        ToneGenerator.threadPool = Executors.newCachedThreadPool();
    }
}
