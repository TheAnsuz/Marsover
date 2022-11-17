

package mars.mips.instructions.syscalls;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MetaEventListener;

class EndOfTrackListener implements MetaEventListener
{
    private boolean endedYet;
    
    EndOfTrackListener() {
        this.endedYet = false;
    }
    
    @Override
    public synchronized void meta(final MetaMessage m) {
        if (m.getType() == 47) {
            this.endedYet = true;
            this.notifyAll();
        }
    }
    
    public synchronized void awaitEndOfTrack() throws InterruptedException {
        while (!this.endedYet) {
            this.wait();
        }
    }
}
