

package mars.util;

import mars.mips.hardware.AddressErrorException;
import mars.Globals;
import mars.mips.hardware.Memory;
import java.util.HashMap;
import java.util.ArrayList;

public class MemoryDump
{
    public static ArrayList dumpTriples;
    private static final HashMap segmentBoundMap;
    private static final String[] segmentNames;
    private static int[] baseAddresses;
    private static int[] limitAddresses;
    
    public static Integer[] getSegmentBounds(final String segment) {
        for (int i = 0; i < MemoryDump.segmentNames.length; ++i) {
            if (MemoryDump.segmentNames[i].equals(segment)) {
                final Integer[] bounds = { new Integer(getBaseAddresses(MemoryDump.segmentNames)[i]), new Integer(getLimitAddresses(MemoryDump.segmentNames)[i]) };
                return bounds;
            }
        }
        return null;
    }
    
    public static String[] getSegmentNames() {
        return MemoryDump.segmentNames;
    }
    
    public static int[] getBaseAddresses(final String[] segments) {
        MemoryDump.baseAddresses[0] = Memory.textBaseAddress;
        MemoryDump.baseAddresses[1] = Memory.dataBaseAddress;
        return MemoryDump.baseAddresses;
    }
    
    public static int[] getLimitAddresses(final String[] segments) {
        MemoryDump.limitAddresses[0] = Memory.textLimitAddress;
        MemoryDump.limitAddresses[1] = Memory.dataSegmentLimitAddress;
        return MemoryDump.limitAddresses;
    }
    
    public static int getAddressOfFirstNull(final int baseAddress, final int limitAddress) throws AddressErrorException {
        int address;
        for (address = baseAddress; address < limitAddress && Globals.memory.getRawWordOrNull(address) != null; address += 4) {}
        return address;
    }
    
    static {
        MemoryDump.dumpTriples = null;
        segmentBoundMap = new HashMap();
        segmentNames = new String[] { ".text", ".data" };
        MemoryDump.baseAddresses = new int[2];
        MemoryDump.limitAddresses = new int[2];
    }
}
