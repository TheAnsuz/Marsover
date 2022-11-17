

package mars.mips.instructions.syscalls;

import java.util.HashMap;
import java.util.Random;

public class RandomStreams
{
    static final HashMap<Integer,Random> randomStreams;
    
    static {
        randomStreams = new HashMap();
    }
}
