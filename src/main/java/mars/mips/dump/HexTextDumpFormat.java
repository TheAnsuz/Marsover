package mars.mips.dump;

import java.io.*;
import mars.Globals;
import mars.mips.hardware.*;

/**
 * Class that represents the "hexadecimal text" memory dump format. The output
 * is a text file with one word of MIPS memory per line. The word is formatted
 * using hexadecimal characters, e.g. 3F205A39.
 *
 * @author Pete Sanderson
 * @version December 2007
 */
public class HexTextDumpFormat extends AbstractDumpFormat {

    /**
     * Constructor. There is no standard file extension for this format.
     */
    public HexTextDumpFormat() {
        super("Hexadecimal Text", "HexText", "Written as hex characters to text file", null);
    }

    /**
     * Write MIPS memory contents in hexadecimal text format. Each line of text
     * contains one memory word written in hexadecimal characters. Written using
     * PrintStream's println() method. Adapted by Pete Sanderson from code
     * written by Greg Gibeling.
     *
     * @param file File in which to store MIPS memory contents.
     * @param firstAddress first (lowest) memory address to dump. In bytes but
     * must be on word boundary.
     * @param lastAddress last (highest) memory address to dump. In bytes but
     * must be on word boundary. Will dump the word that starts at this address.
     * @throws AddressErrorException if firstAddress is invalid or not on a word
     * boundary.
     * @throws IOException if error occurs during file output.
     */
    public void dumpMemoryRange(File file, int firstAddress, int lastAddress)
            throws AddressErrorException, IOException {
        PrintStream out = new PrintStream(new FileOutputStream(file));
        String string = null;
        try {
            for (int address = firstAddress; address <= lastAddress; address += Memory.WORD_LENGTH_BYTES) {
                Integer temp = Globals.memory.getRawWordOrNull(address);
                if (temp == null)
                    break;
                string = Integer.toHexString(temp.intValue());
                while (string.length() < 8) {
                    string = '0' + string;
                }
                out.println(string);
            }
        } finally {
            out.close();
        }
    }

}
