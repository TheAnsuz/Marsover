

package mars.mips.dump;

import java.io.File;
import java.io.IOException;
import mars.mips.hardware.AddressErrorException;

public abstract class AbstractDumpFormat implements DumpFormat
{
    private final String name;
    private final String commandDescriptor;
    private final String description;
    private final String extension;
    
    public AbstractDumpFormat(final String name, final String commandDescriptor, final String description, final String extension) {
        this.name = name;
        this.commandDescriptor = ((commandDescriptor == null) ? null : commandDescriptor.replaceAll(" ", ""));
        this.description = description;
        this.extension = extension;
    }
    
    @Override
    public String getFileExtension() {
        return this.extension;
    }
    
    @Override
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    @Override
    public String getCommandDescriptor() {
        return this.commandDescriptor;
    }
    
    @Override
    public abstract void dumpMemoryRange(final File p0, final int p1, final int p2) throws AddressErrorException, IOException;
}
