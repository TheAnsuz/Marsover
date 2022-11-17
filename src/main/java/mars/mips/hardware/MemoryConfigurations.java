

package mars.mips.hardware;

import java.util.Iterator;
import mars.Globals;
import java.util.ArrayList;

public class MemoryConfigurations
{
    private static ArrayList configurations;
    private static MemoryConfiguration defaultConfiguration;
    private static MemoryConfiguration currentConfiguration;
    private static final String[] configurationItemNames;
    private static int[] defaultConfigurationItemValues;
    private static int[] dataBasedCompactConfigurationItemValues;
    private static int[] textBasedCompactConfigurationItemValues;
    
    public static void buildConfigurationCollection() {
        if (MemoryConfigurations.configurations == null) {
            (MemoryConfigurations.configurations = new ArrayList()).add(new MemoryConfiguration("Default", "Default", MemoryConfigurations.configurationItemNames, MemoryConfigurations.defaultConfigurationItemValues));
            MemoryConfigurations.configurations.add(new MemoryConfiguration("CompactDataAtZero", "Compact, Data at Address 0", MemoryConfigurations.configurationItemNames, MemoryConfigurations.dataBasedCompactConfigurationItemValues));
            MemoryConfigurations.configurations.add(new MemoryConfiguration("CompactTextAtZero", "Compact, Text at Address 0", MemoryConfigurations.configurationItemNames, MemoryConfigurations.textBasedCompactConfigurationItemValues));
            MemoryConfigurations.defaultConfiguration = MemoryConfigurations.configurations.get(0);
            MemoryConfigurations.currentConfiguration = MemoryConfigurations.defaultConfiguration;
            setCurrentConfiguration(getConfigurationByName(Globals.getSettings().getMemoryConfiguration()));
        }
    }
    
    public static Iterator getConfigurationsIterator() {
        if (MemoryConfigurations.configurations == null) {
            buildConfigurationCollection();
        }
        return MemoryConfigurations.configurations.iterator();
    }
    
    public static MemoryConfiguration getConfigurationByName(final String name) {
        final Iterator configurationsIterator = getConfigurationsIterator();
        while (configurationsIterator.hasNext()) {
            final MemoryConfiguration config = configurationsIterator.next();
            if (name.equals(config.getConfigurationIdentifier())) {
                return config;
            }
        }
        return null;
    }
    
    public static MemoryConfiguration getDefaultConfiguration() {
        if (MemoryConfigurations.defaultConfiguration == null) {
            buildConfigurationCollection();
        }
        return MemoryConfigurations.defaultConfiguration;
    }
    
    public static MemoryConfiguration getCurrentConfiguration() {
        if (MemoryConfigurations.currentConfiguration == null) {
            buildConfigurationCollection();
        }
        return MemoryConfigurations.currentConfiguration;
    }
    
    public static boolean setCurrentConfiguration(final MemoryConfiguration config) {
        if (config == null) {
            return false;
        }
        if (config != MemoryConfigurations.currentConfiguration) {
            MemoryConfigurations.currentConfiguration = config;
            Globals.memory.clear();
            RegisterFile.getUserRegister("$gp").changeResetValue(config.getGlobalPointer());
            RegisterFile.getUserRegister("$sp").changeResetValue(config.getStackPointer());
            RegisterFile.getProgramCounterRegister().changeResetValue(config.getTextBaseAddress());
            RegisterFile.initializeProgramCounter(config.getTextBaseAddress());
            RegisterFile.resetRegisters();
            return true;
        }
        return false;
    }
    
    public static int getDefaultTextBaseAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[0];
    }
    
    public static int getDefaultDataSegmentBaseAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[1];
    }
    
    public static int getDefaultExternBaseAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[2];
    }
    
    public static int getDefaultGlobalPointer() {
        return MemoryConfigurations.defaultConfigurationItemValues[3];
    }
    
    public static int getDefaultDataBaseAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[4];
    }
    
    public static int getDefaultHeapBaseAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[5];
    }
    
    public static int getDefaultStackPointer() {
        return MemoryConfigurations.defaultConfigurationItemValues[6];
    }
    
    public static int getDefaultStackBaseAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[7];
    }
    
    public static int getDefaultUserHighAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[8];
    }
    
    public static int getDefaultKernelBaseAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[9];
    }
    
    public static int getDefaultKernelTextBaseAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[10];
    }
    
    public static int getDefaultExceptionHandlerAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[11];
    }
    
    public static int getDefaultKernelDataBaseAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[12];
    }
    
    public static int getDefaultMemoryMapBaseAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[13];
    }
    
    public static int getDefaultKernelHighAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[14];
    }
    
    public int getDefaultDataSegmentLimitAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[15];
    }
    
    public int getDefaultTextLimitAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[16];
    }
    
    public int getDefaultKernelDataSegmentLimitAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[17];
    }
    
    public int getDefaultKernelTextLimitAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[18];
    }
    
    public int getDefaultStackLimitAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[19];
    }
    
    public int getMemoryMapLimitAddress() {
        return MemoryConfigurations.defaultConfigurationItemValues[20];
    }
    
    static {
        MemoryConfigurations.configurations = null;
        configurationItemNames = new String[] { ".text base address", "data segment base address", ".extern base address", "global pointer $gp", ".data base address", "heap base address", "stack pointer $sp", "stack base address", "user space high address", "kernel space base address", ".ktext base address", "exception handler address", ".kdata base address", "MMIO base address", "kernel space high address", "data segment limit address", "text limit address", "kernel data segment limit address", "kernel text limit address", "stack limit address", "memory map limit address" };
        MemoryConfigurations.defaultConfigurationItemValues = new int[] { 4194304, 268435456, 268435456, 268468224, 268500992, 268697600, 2147479548, 2147483644, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, -2147483264, -1879048192, -65536, -1, Integer.MAX_VALUE, 268435452, -65537, -1879048196, 268697600, -1 };
        MemoryConfigurations.dataBasedCompactConfigurationItemValues = new int[] { 12288, 0, 4096, 6144, 0, 8192, 12284, 12284, 16383, 16384, 16384, 16768, 20480, 32512, 32767, 12287, 16380, 32511, 20476, 8192, 32767 };
        MemoryConfigurations.textBasedCompactConfigurationItemValues = new int[] { 0, 4096, 4096, 6144, 8192, 12288, 16380, 16380, 16383, 16384, 16384, 16768, 20480, 32512, 32767, 16383, 4092, 32511, 20476, 12288, 32767 };
    }
}
