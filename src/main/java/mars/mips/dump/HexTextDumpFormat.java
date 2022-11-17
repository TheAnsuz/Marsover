

package mars.mips.dump;

import java.io.IOException;
import mars.mips.hardware.AddressErrorException;
import mars.Globals;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;

public class HexTextDumpFormat extends AbstractDumpFormat
{
    public HexTextDumpFormat() {
        super("Hexadecimal Text", "HexText", "Written as hex characters to text file", null);
    }
    
    @Override
    public void dumpMemoryRange(final File file, final int firstAddress, final int lastAddress) throws AddressErrorException, IOException {
        final PrintStream out = new PrintStream(new FileOutputStream(file));
        String string = null;
        try {
            for (int address = firstAddress; address <= lastAddress; address += 4) {
                final Integer temp = Globals.memory.getRawWordOrNull(address);
                if (temp == null) {
                    break;
                }
                for (string = Integer.toHexString(temp); string.length() < 8; string = '0' + string) {}
                out.println(string);
            }
        }
        finally {
            out.close();
        }
    }
}
