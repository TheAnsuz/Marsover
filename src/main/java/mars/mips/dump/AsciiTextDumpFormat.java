

package mars.mips.dump;

import java.io.IOException;
import mars.mips.hardware.AddressErrorException;
import mars.util.Binary;
import mars.Globals;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;

public class AsciiTextDumpFormat extends AbstractDumpFormat
{
    public AsciiTextDumpFormat() {
        super("ASCII Text", "AsciiText", "Memory contents interpreted as ASCII characters", null);
    }
    
    @Override
    public void dumpMemoryRange(final File file, final int firstAddress, final int lastAddress) throws AddressErrorException, IOException {
        final PrintStream out = new PrintStream(new FileOutputStream(file));
        final String string = null;
        try {
            for (int address = firstAddress; address <= lastAddress; address += 4) {
                final Integer temp = Globals.memory.getRawWordOrNull(address);
                if (temp == null) {
                    break;
                }
                out.println(Binary.intToAscii(temp));
            }
        }
        finally {
            out.close();
        }
    }
}
