   package mars.mips.instructions.syscalls;
   import mars.*;
   import mars.mips.hardware.*;
   import mars.simulator.*;
   import mars.util.*;



/** 
 * Service to read a character from input console into $a0.
 *
 */
 
    public class SyscallReadChar extends AbstractSyscall {
   /**
    * Build an instance of the Read Char syscall.  Default service number
    * is 12 and name is "ReadChar".
    */
       public SyscallReadChar() {
         super(12, "ReadChar");
      }
      
   /**
   * Performs syscall function to read a character from input console into $a0
   */
       public void simulate(ProgramStatement statement) throws ProcessingException {
         int value = 0;
         try
         {
            value = SystemIO.readChar(this.getNumber());
         } 
             catch (IndexOutOfBoundsException e) // means null input
            {
               throw new ProcessingException(statement,
                     "invalid char input (syscall "+this.getNumber()+")",
                  	Exceptions.SYSCALL_EXCEPTION);
            }
			// DPS 20 June 2008: changed from 4 ($a0) to 2 ($v0)
         RegisterFile.updateRegister(2, value); 
      }
   
   }