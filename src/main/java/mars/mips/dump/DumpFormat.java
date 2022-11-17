

package mars.mips.dump;

import java.io.File;
import java.io.IOException;
import mars.mips.hardware.AddressErrorException;

public interface DumpFormat
{
    String getFileExtension();
    
    String getDescription();
    
    String getCommandDescriptor();
    
    String toString();
    
    void dumpMemoryRange(final File p0, final int p1, final int p2) throws AddressErrorException, IOException;
}
