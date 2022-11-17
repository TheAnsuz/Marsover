

package mars.mips.dump;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import mars.Globals;
import mars.ProgramStatement;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Memory;
import mars.util.Binary;

public class SegmentWindowDumpFormat extends AbstractDumpFormat
{
    public SegmentWindowDumpFormat() {
        super("Text/Data Segment Window", "SegmentWindow", " Text Segment Window or Data Segment Window format to text file", null);
    }
    
    @Override
    public void dumpMemoryRange(final File file, final int firstAddress, final int lastAddress) throws AddressErrorException, IOException {
        final PrintStream out = new PrintStream(new FileOutputStream(file));
        final boolean hexAddresses = Globals.getSettings().getDisplayAddressesInHex();
        if (Memory.inDataSegment(firstAddress)) {
            final boolean hexValues = Globals.getSettings().getDisplayValuesInHex();
            int offset = 0;
            String string = "";
            try {
                for (int address = firstAddress; address <= lastAddress; address += 4) {
                    if (offset % 8 == 0) {
                        string = (hexAddresses ? Binary.intToHexString(address) : Binary.unsignedIntToIntString(address)) + "    ";
                    }
                    ++offset;
                    final Integer temp = Globals.memory.getRawWordOrNull(address);
                    if (temp == null) {
                        break;
                    }
                    string = string + (hexValues ? Binary.intToHexString(temp) : ("           " + temp).substring(temp.toString().length())) + " ";
                    if (offset % 8 == 0) {
                        out.println(string);
                        string = "";
                    }
                }
            }
            finally {
                out.close();
            }
            return;
        }
        if (!Memory.inTextSegment(firstAddress)) {
            return;
        }
        out.println(" Address    Code        Basic                     Source");
        out.println();
        String string2 = null;
        try {
            for (int address2 = firstAddress; address2 <= lastAddress; address2 += 4) {
                string2 = (hexAddresses ? Binary.intToHexString(address2) : Binary.unsignedIntToIntString(address2)) + "  ";
                final Integer temp2 = Globals.memory.getRawWordOrNull(address2);
                if (temp2 == null) {
                    break;
                }
                string2 = string2 + Binary.intToHexString(temp2) + "  ";
                try {
                    final ProgramStatement ps = Globals.memory.getStatement(address2);
                    string2 += (ps.getPrintableBasicAssemblyStatement() + "                      ").substring(0, 22);
                    string2 += ((("".equals(ps.getSource())) ? "" : Integer.toString(ps.getSourceLine())) + "     ").substring(0, 5);
                    string2 += ps.getSource();
                }
                catch (AddressErrorException ex) {}
                out.println(string2);
            }
        }
        finally {
            out.close();
        }
    }
}
