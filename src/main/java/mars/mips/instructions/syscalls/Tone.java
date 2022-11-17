

package mars.mips.instructions.syscalls;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

class Tone implements Runnable
{
    public static final int TEMPO = 1000;
    public static final int DEFAULT_CHANNEL = 0;
    private final byte pitch;
    private final int duration;
    private final byte instrument;
    private final byte volume;
    private static Lock openLock;
    
    public Tone(final byte pitch, final int duration, final byte instrument, final byte volume) {
        this.pitch = pitch;
        this.duration = duration;
        this.instrument = instrument;
        this.volume = volume;
    }
    
    @Override
    public void run() {
        this.playTone();
    }
    
    private void playTone() {
        try {
            Sequencer player = null;
            Tone.openLock.lock();
            try {
                player = MidiSystem.getSequencer();
                player.open();
            }
            finally {
                Tone.openLock.unlock();
            }
            final Sequence seq = new Sequence(0.0f, 1);
            player.setTempoInMPQ(1000.0f);
            final Track t = seq.createTrack();
            final ShortMessage inst = new ShortMessage();
            inst.setMessage(192, 0, this.instrument, 0);
            final MidiEvent instChange = new MidiEvent(inst, 0L);
            t.add(instChange);
            final ShortMessage on = new ShortMessage();
            on.setMessage(144, 0, this.pitch, this.volume);
            final MidiEvent noteOn = new MidiEvent(on, 0L);
            t.add(noteOn);
            final ShortMessage off = new ShortMessage();
            off.setMessage(128, 0, this.pitch, this.volume);
            final MidiEvent noteOff = new MidiEvent(off, this.duration);
            t.add(noteOff);
            player.setSequence(seq);
            final EndOfTrackListener eot = new EndOfTrackListener();
            player.addMetaEventListener(eot);
            player.start();
            try {
                eot.awaitEndOfTrack();
            }
            catch (InterruptedException ex) {}
            finally {
                player.close();
            }
        }
        catch (MidiUnavailableException mue) {
            mue.printStackTrace();
        }
        catch (InvalidMidiDataException imde) {
            imde.printStackTrace();
        }
    }
    
    static {
        Tone.openLock = new ReentrantLock();
    }
}
