

package mars.mips.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;

public interface SimulationCode
{
    void simulate(final ProgramStatement p0) throws ProcessingException;
}
