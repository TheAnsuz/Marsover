

package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.ProgramStatement;

public interface Syscall
{
    String getName();
    
    void setNumber(final int p0);
    
    int getNumber();
    
    void simulate(final ProgramStatement p0) throws ProcessingException;
}
