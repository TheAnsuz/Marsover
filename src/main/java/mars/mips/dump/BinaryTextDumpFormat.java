   package mars.mips.dump;

   import java.io.*;
   import mars.Globals;
   import mars.mips.hardware.*;

/**
 * Class that represents the "binary text" memory dump format.  The output 
 * is a text file with one word of MIPS memory per line.  The word is formatted
 * using '0' and '1' characters, e.g. 01110101110000011111110101010011.
 * @author Pete Sanderson 
 * @version December 2007
 */


    public class BinaryTextDumpFormat extends AbstractDumpFormat {
   
   /**
   *  Constructor.  There is no standard file extension for this format.
   */
       public BinaryTextDumpFormat() {
         super("Binary Text", "BinaryText", "Written as '0' and '1' characters to text file", null);
      }
   
   
   /**
   *  Write MIPS memory contents in binary text format.  Each line of
   *  text contains one memory word written as 32 '0' and '1' characters.  Written
   *  using PrintStream's println() method.
   *  Adapted by Pete Sanderson from code written by Greg Gibeling.
   *
   *  @param  file  File in which to store MIPS memory contents.  
   *  @param firstAddress first (lowest) memory address to dump.  In bytes but
   *  must be on word boundary.
   *  @param lastAddress last (highest) memory address to dump.  In bytes but
   *  must be on word boundary.  Will dump the word that starts at this address.
   *  @throws AddressErrorException if firstAddress is invalid or not on a word boundary.
   *  @throws IOException if error occurs during file output.
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
               string = Integer.toBinaryString(temp.intValue());
               while (string.length() < 32) {
                  string = '0' + string;
               }
               out.println(string);
            }
         } 
         finally { 
            out.close(); 
         }
      }
   
   }