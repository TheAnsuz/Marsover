   package mars.mips.instructions.syscalls;
   import mars.*;
   import mars.mips.hardware.*;
   import mars.util.*;



/** 
 * Service to read console input string into buffer starting at address in $a0.  
 */
 
    public class SyscallReadString extends AbstractSyscall {
   /**
    * Build an instance of the Read String syscall.  Default service number
    * is 8 and name is "ReadString".
    */
       public SyscallReadString() {
         super(8, "ReadString");
      }
      
   /**
   * Performs syscall function to read console input string into buffer starting at address in $a0.
   * Follows semantics of UNIX 'fgets'.  For specified length n,
   * string can be no longer than n-1. If less than that, add
   * newline to end.  In either case, then pad with null byte.
   */
       public void simulate(ProgramStatement statement) throws ProcessingException {
      
         String inputString = "";
         int buf = RegisterFile.getValue(4); // buf addr in $a0
         int maxLength = RegisterFile.getValue(5) - 1; // $a1
			boolean addNullByte = true;
      	// Guard against negative maxLength.  DPS 13-July-2011
         if (maxLength < 0) 
         {
            maxLength = 0;
				addNullByte = false;
         }
         inputString = SystemIO.readString(this.getNumber(), maxLength);
         int stringLength = Math.min(maxLength, inputString.length());
         try
         {
            for (int index = 0; index < stringLength; index++)
            {
               Globals.memory.setByte(buf + index,
                                       inputString.charAt(index));
            }            
            if (stringLength < maxLength)
            {
               Globals.memory.setByte(buf + stringLength, '\n');
               stringLength++;
            }
            if (addNullByte) Globals.memory.setByte(buf + stringLength, 0);
         } 
             catch (AddressErrorException e)
            {
               throw new ProcessingException(statement, e);
            }
      }
   }