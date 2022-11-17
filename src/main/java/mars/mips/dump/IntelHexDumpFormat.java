

package mars.mips.dump;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import mars.Globals;
import mars.mips.hardware.AddressErrorException;

public class IntelHexDumpFormat extends AbstractDumpFormat
{
    public IntelHexDumpFormat() {
        super("Intel hex format", "HEX", "Written as Intel Hex Memory File", "hex");
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
                String addr;
                for (addr = Integer.toHexString(address - firstAddress); addr.length() < 4; addr = '0' + addr) {}
                int tmp_chksum = 0;
                tmp_chksum += 4;
                tmp_chksum += (0xFF & address - firstAddress);
                tmp_chksum += (0xFF & address - firstAddress >> 8);
                tmp_chksum += (0xFF & temp);
                tmp_chksum += (0xFF & temp >> 8);
                tmp_chksum += (0xFF & temp >> 16);
                tmp_chksum += (0xFF & temp >> 24);
                tmp_chksum %= 256;
                tmp_chksum = ~tmp_chksum + 1;
                String chksum = Integer.toHexString(0xFF & tmp_chksum);
                if (chksum.length() == 1) {
                    chksum = '0' + chksum;
                }
                final String finalstr = ":04" + addr + "00" + string + chksum;
                out.println(finalstr.toUpperCase());
            }
            out.println(":00000001FF");
        }
        finally {
            out.close();
        }
    }
}
