

package mars;

import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;
import mars.simulator.Exceptions;
import mars.util.Binary;

public class ProcessingException extends Exception
{
    private ErrorList errs;
    
    public ProcessingException(final ErrorList e) {
        this.errs = e;
    }
    
    public ProcessingException(final ErrorList e, final AddressErrorException aee) {
        this.errs = e;
        Exceptions.setRegisters(aee.getType(), aee.getAddress());
    }
    
    public ProcessingException(final ProgramStatement ps, final String m) {
        (this.errs = new ErrorList()).add(new ErrorMessage(ps, "Runtime exception at " + Binary.intToHexString(RegisterFile.getProgramCounter() - 4) + ": " + m));
    }
    
    public ProcessingException(final ProgramStatement ps, final String m, final int cause) {
        this(ps, m);
        Exceptions.setRegisters(cause);
    }
    
    public ProcessingException(final ProgramStatement ps, final AddressErrorException aee) {
        this(ps, aee.getMessage());
        Exceptions.setRegisters(aee.getType(), aee.getAddress());
    }
    
    public ProcessingException() {
        this.errs = null;
    }
    
    public ErrorList errors() {
        return this.errs;
    }
}
