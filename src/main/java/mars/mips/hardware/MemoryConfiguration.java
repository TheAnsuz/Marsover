

package mars.mips.hardware;

public class MemoryConfiguration
{
    private String configurationIdentifier;
    private String configurationName;
    private String[] configurationItemNames;
    private int[] configurationItemValues;
    
    public MemoryConfiguration(final String ident, final String name, final String[] items, final int[] values) {
        this.configurationIdentifier = ident;
        this.configurationName = name;
        this.configurationItemNames = items;
        this.configurationItemValues = values;
    }
    
    public String getConfigurationIdentifier() {
        return this.configurationIdentifier;
    }
    
    public String getConfigurationName() {
        return this.configurationName;
    }
    
    public int[] getConfigurationItemValues() {
        return this.configurationItemValues;
    }
    
    public String[] getConfigurationItemNames() {
        return this.configurationItemNames;
    }
    
    public int getTextBaseAddress() {
        return this.configurationItemValues[0];
    }
    
    public int getDataSegmentBaseAddress() {
        return this.configurationItemValues[1];
    }
    
    public int getExternBaseAddress() {
        return this.configurationItemValues[2];
    }
    
    public int getGlobalPointer() {
        return this.configurationItemValues[3];
    }
    
    public int getDataBaseAddress() {
        return this.configurationItemValues[4];
    }
    
    public int getHeapBaseAddress() {
        return this.configurationItemValues[5];
    }
    
    public int getStackPointer() {
        return this.configurationItemValues[6];
    }
    
    public int getStackBaseAddress() {
        return this.configurationItemValues[7];
    }
    
    public int getUserHighAddress() {
        return this.configurationItemValues[8];
    }
    
    public int getKernelBaseAddress() {
        return this.configurationItemValues[9];
    }
    
    public int getKernelTextBaseAddress() {
        return this.configurationItemValues[10];
    }
    
    public int getExceptionHandlerAddress() {
        return this.configurationItemValues[11];
    }
    
    public int getKernelDataBaseAddress() {
        return this.configurationItemValues[12];
    }
    
    public int getMemoryMapBaseAddress() {
        return this.configurationItemValues[13];
    }
    
    public int getKernelHighAddress() {
        return this.configurationItemValues[14];
    }
    
    public int getDataSegmentLimitAddress() {
        return this.configurationItemValues[15];
    }
    
    public int getTextLimitAddress() {
        return this.configurationItemValues[16];
    }
    
    public int getKernelDataSegmentLimitAddress() {
        return this.configurationItemValues[17];
    }
    
    public int getKernelTextLimitAddress() {
        return this.configurationItemValues[18];
    }
    
    public int getStackLimitAddress() {
        return this.configurationItemValues[19];
    }
    
    public int getMemoryMapLimitAddress() {
        return this.configurationItemValues[20];
    }
}
