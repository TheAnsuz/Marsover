

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.mips.hardware.RegisterFile;
import mars.ProgramStatement;

public class SyscallMidiOutSync extends AbstractSyscall
{
    static final int rangeLowEnd = 0;
    static final int rangeHighEnd = 127;
    
    public SyscallMidiOutSync() {
        super(33, "MidiOutSync");
    }
    
    @Override
    public void simulate(final ProgramStatement statement) throws ProcessingException {
        int pitch = RegisterFile.getValue(4);
        int duration = RegisterFile.getValue(5);
        int instrument = RegisterFile.getValue(6);
        int volume = RegisterFile.getValue(7);
        if (pitch < 0 || pitch > 127) {
            pitch = 60;
        }
        if (duration < 0) {
            duration = 1000;
        }
        if (instrument < 0 || instrument > 127) {
            instrument = 0;
        }
        if (volume < 0 || volume > 127) {
            volume = 100;
        }
        new ToneGenerator().generateToneSynchronously((byte)pitch, duration, (byte)instrument, (byte)volume);
    }
}
