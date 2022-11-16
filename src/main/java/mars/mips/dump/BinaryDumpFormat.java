   package mars.mips.dump;

   import java.io.*;
   import mars.Globals;
   import mars.mips.hardware.*;

/**
 * Class that represents the "binary" memory dump format.  The output 
 * is a binary file containing the memory words as a byte stream.  Output
 * is produced using PrintStream's write() method.
 * @author Pete Sanderson 
 * @version December 2007
 */


    public class BinaryDumpFormat extends AbstractDumpFormat {
   
   /**
   *  Constructor.  There is no standard file extension for this format.
   */
       public BinaryDumpFormat() {
         super("Binary", "Binary", "Written as byte stream to binary file", null);
      }
   
   
   /**
   *  Write MIPS memory contents in pure binary format.  One byte at a time
	*  using PrintStream's write() method.  Adapted by Pete Sanderson from
	*  code written by Greg Gibeling.
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
         try {
            for (int address = firstAddress; address <= lastAddress; address += Memory.WORD_LENGTH_BYTES) {
               Integer temp = Globals.memory.getRawWordOrNull(address);
               if (temp == null) 
                  break;
               int word = temp.intValue();
               for (int i = 0; i < 4; i++) 
                  out.write((word >>> (i << 3)) & 0xFF);
            }
         } 
         finally { 
            out.close(); 
         }
      }
   
   }