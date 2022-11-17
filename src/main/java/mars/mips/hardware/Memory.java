

package mars.mips.hardware;

import java.util.Vector;
import java.util.Iterator;
import java.util.Observer;
import mars.util.Binary;
import mars.Globals;
import mars.ProgramStatement;
import java.util.Collection;
import java.util.Observable;

public class Memory extends Observable
{
    public static int textBaseAddress;
    public static int dataSegmentBaseAddress;
    public static int externBaseAddress;
    public static int globalPointer;
    public static int dataBaseAddress;
    public static int heapBaseAddress;
    public static int stackPointer;
    public static int stackBaseAddress;
    public static int userHighAddress;
    public static int kernelBaseAddress;
    public static int kernelTextBaseAddress;
    public static int exceptionHandlerAddress;
    public static int kernelDataBaseAddress;
    public static int memoryMapBaseAddress;
    public static int kernelHighAddress;
    public static final int WORD_LENGTH_BYTES = 4;
    public static final boolean LITTLE_ENDIAN = true;
    public static final boolean BIG_ENDIAN = false;
    private static boolean byteOrder;
    public static int heapAddress;
    Collection<MemoryObservable> observables;
    private static final int BLOCK_LENGTH_WORDS = 1024;
    private static final int BLOCK_TABLE_LENGTH = 1024;
    private int[][] dataBlockTable;
    private int[][] kernelDataBlockTable;
    private int[][] stackBlockTable;
    private static final int MMIO_TABLE_LENGTH = 16;
    private int[][] memoryMapBlockTable;
    private static final int TEXT_BLOCK_LENGTH_WORDS = 1024;
    private static final int TEXT_BLOCK_TABLE_LENGTH = 1024;
    private ProgramStatement[][] textBlockTable;
    private ProgramStatement[][] kernelTextBlockTable;
    public static int dataSegmentLimitAddress;
    public static int textLimitAddress;
    public static int kernelDataSegmentLimitAddress;
    public static int kernelTextLimitAddress;
    public static int stackLimitAddress;
    public static int memoryMapLimitAddress;
    private static Memory uniqueMemoryInstance;
    private static final boolean STORE = true;
    private static final boolean FETCH = false;
    
    private Memory() {
        this.observables = this.getNewMemoryObserversCollection();
        this.initialize();
    }
    
    public static Memory getInstance() {
        return Memory.uniqueMemoryInstance;
    }
    
    public void clear() {
        setConfiguration();
        this.initialize();
    }
    
    public static void setConfiguration() {
        Memory.textBaseAddress = MemoryConfigurations.getCurrentConfiguration().getTextBaseAddress();
        Memory.dataSegmentBaseAddress = MemoryConfigurations.getCurrentConfiguration().getDataSegmentBaseAddress();
        Memory.externBaseAddress = MemoryConfigurations.getCurrentConfiguration().getExternBaseAddress();
        Memory.globalPointer = MemoryConfigurations.getCurrentConfiguration().getGlobalPointer();
        Memory.dataBaseAddress = MemoryConfigurations.getCurrentConfiguration().getDataBaseAddress();
        Memory.heapBaseAddress = MemoryConfigurations.getCurrentConfiguration().getHeapBaseAddress();
        Memory.stackPointer = MemoryConfigurations.getCurrentConfiguration().getStackPointer();
        Memory.stackBaseAddress = MemoryConfigurations.getCurrentConfiguration().getStackBaseAddress();
        Memory.userHighAddress = MemoryConfigurations.getCurrentConfiguration().getUserHighAddress();
        Memory.kernelBaseAddress = MemoryConfigurations.getCurrentConfiguration().getKernelBaseAddress();
        Memory.kernelTextBaseAddress = MemoryConfigurations.getCurrentConfiguration().getKernelTextBaseAddress();
        Memory.exceptionHandlerAddress = MemoryConfigurations.getCurrentConfiguration().getExceptionHandlerAddress();
        Memory.kernelDataBaseAddress = MemoryConfigurations.getCurrentConfiguration().getKernelDataBaseAddress();
        Memory.memoryMapBaseAddress = MemoryConfigurations.getCurrentConfiguration().getMemoryMapBaseAddress();
        Memory.kernelHighAddress = MemoryConfigurations.getCurrentConfiguration().getKernelHighAddress();
        Memory.dataSegmentLimitAddress = Math.min(MemoryConfigurations.getCurrentConfiguration().getDataSegmentLimitAddress(), Memory.dataSegmentBaseAddress + 4194304);
        Memory.textLimitAddress = Math.min(MemoryConfigurations.getCurrentConfiguration().getTextLimitAddress(), Memory.textBaseAddress + 4194304);
        Memory.kernelDataSegmentLimitAddress = Math.min(MemoryConfigurations.getCurrentConfiguration().getKernelDataSegmentLimitAddress(), Memory.kernelDataBaseAddress + 4194304);
        Memory.kernelTextLimitAddress = Math.min(MemoryConfigurations.getCurrentConfiguration().getKernelTextLimitAddress(), Memory.kernelTextBaseAddress + 4194304);
        Memory.stackLimitAddress = Math.max(MemoryConfigurations.getCurrentConfiguration().getStackLimitAddress(), Memory.stackBaseAddress - 4194304);
        Memory.memoryMapLimitAddress = Math.min(MemoryConfigurations.getCurrentConfiguration().getMemoryMapLimitAddress(), Memory.memoryMapBaseAddress + 65536);
    }
    
    public boolean usingCompactMemoryConfiguration() {
        return (Memory.kernelHighAddress & 0x7FFF) == Memory.kernelHighAddress;
    }
    
    private void initialize() {
        Memory.heapAddress = Memory.heapBaseAddress;
        this.textBlockTable = new ProgramStatement[1024][];
        this.dataBlockTable = new int[1024][];
        this.kernelTextBlockTable = new ProgramStatement[1024][];
        this.kernelDataBlockTable = new int[1024][];
        this.stackBlockTable = new int[1024][];
        this.memoryMapBlockTable = new int[16][];
        System.gc();
    }
    
    public int allocateBytesFromHeap(final int numBytes) throws IllegalArgumentException {
        final int result = Memory.heapAddress;
        if (numBytes < 0) {
            throw new IllegalArgumentException("request (" + numBytes + ") is negative heap amount");
        }
        int newHeapAddress = Memory.heapAddress + numBytes;
        if (newHeapAddress % 4 != 0) {
            newHeapAddress += 4 - newHeapAddress % 4;
        }
        if (newHeapAddress >= Memory.dataSegmentLimitAddress) {
            throw new IllegalArgumentException("request (" + numBytes + ") exceeds available heap storage");
        }
        Memory.heapAddress = newHeapAddress;
        return result;
    }
    
    public void setByteOrder(final boolean order) {
        Memory.byteOrder = order;
    }
    
    public boolean getByteOrder() {
        return Memory.byteOrder;
    }
    
    public int set(final int address, final int value, final int length) throws AddressErrorException {
        int oldValue = 0;
        if (Globals.debug) {
            System.out.println("memory[" + address + "] set to " + value + "(" + length + " bytes)");
        }
        if (inDataSegment(address)) {
            final int relativeByteAddress = address - Memory.dataSegmentBaseAddress;
            oldValue = this.storeBytesInTable(this.dataBlockTable, relativeByteAddress, length, value);
        }
        else if (address > Memory.stackLimitAddress && address <= Memory.stackBaseAddress) {
            final int relativeByteAddress = Memory.stackBaseAddress - address;
            oldValue = this.storeBytesInTable(this.stackBlockTable, relativeByteAddress, length, value);
        }
        else if (inTextSegment(address)) {
            if (!Globals.getSettings().getBooleanSetting(20)) {
                throw new AddressErrorException("Cannot write directly to text segment!", 5, address);
            }
            final ProgramStatement oldStatement = this.getStatementNoNotify(address);
            if (oldStatement != null) {
                oldValue = oldStatement.getBinaryStatement();
            }
            this.setStatement(address, new ProgramStatement(value, address));
        }
        else if (address >= Memory.memoryMapBaseAddress && address < Memory.memoryMapLimitAddress) {
            final int relativeByteAddress = address - Memory.memoryMapBaseAddress;
            oldValue = this.storeBytesInTable(this.memoryMapBlockTable, relativeByteAddress, length, value);
        }
        else if (inKernelDataSegment(address)) {
            final int relativeByteAddress = address - Memory.kernelDataBaseAddress;
            oldValue = this.storeBytesInTable(this.kernelDataBlockTable, relativeByteAddress, length, value);
        }
        else {
            if (inKernelTextSegment(address)) {
                throw new AddressErrorException("DEVELOPER: You must use setStatement() to write to kernel text segment!", 5, address);
            }
            throw new AddressErrorException("address out of range ", 5, address);
        }
        this.notifyAnyObservers(1, address, length, value);
        return oldValue;
    }
    
    public int setRawWord(final int address, final int value) throws AddressErrorException {
        int oldValue = 0;
        if (address % 4 != 0) {
            throw new AddressErrorException("store address not aligned on word boundary ", 5, address);
        }
        if (inDataSegment(address)) {
            final int relative = address - Memory.dataSegmentBaseAddress >> 2;
            oldValue = this.storeWordInTable(this.dataBlockTable, relative, value);
        }
        else if (address > Memory.stackLimitAddress && address <= Memory.stackBaseAddress) {
            final int relative = Memory.stackBaseAddress - address >> 2;
            oldValue = this.storeWordInTable(this.stackBlockTable, relative, value);
        }
        else if (inTextSegment(address)) {
            if (!Globals.getSettings().getBooleanSetting(20)) {
                throw new AddressErrorException("Cannot write directly to text segment!", 5, address);
            }
            final ProgramStatement oldStatement = this.getStatementNoNotify(address);
            if (oldStatement != null) {
                oldValue = oldStatement.getBinaryStatement();
            }
            this.setStatement(address, new ProgramStatement(value, address));
        }
        else if (address >= Memory.memoryMapBaseAddress && address < Memory.memoryMapLimitAddress) {
            final int relative = address - Memory.memoryMapBaseAddress >> 2;
            oldValue = this.storeWordInTable(this.memoryMapBlockTable, relative, value);
        }
        else if (inKernelDataSegment(address)) {
            final int relative = address - Memory.kernelDataBaseAddress >> 2;
            oldValue = this.storeWordInTable(this.kernelDataBlockTable, relative, value);
        }
        else {
            if (inKernelTextSegment(address)) {
                throw new AddressErrorException("DEVELOPER: You must use setStatement() to write to kernel text segment!", 5, address);
            }
            throw new AddressErrorException("store address out of range ", 5, address);
        }
        this.notifyAnyObservers(1, address, 4, value);
        if (Globals.getSettings().getBackSteppingEnabled()) {
            Globals.program.getBackStepper().addMemoryRestoreRawWord(address, oldValue);
        }
        return oldValue;
    }
    
    public int setWord(final int address, final int value) throws AddressErrorException {
        if (address % 4 != 0) {
            throw new AddressErrorException("store address not aligned on word boundary ", 5, address);
        }
        return Globals.getSettings().getBackSteppingEnabled() ? Globals.program.getBackStepper().addMemoryRestoreWord(address, this.set(address, value, 4)) : this.set(address, value, 4);
    }
    
    public int setHalf(final int address, final int value) throws AddressErrorException {
        if (address % 2 != 0) {
            throw new AddressErrorException("store address not aligned on halfword boundary ", 5, address);
        }
        return Globals.getSettings().getBackSteppingEnabled() ? Globals.program.getBackStepper().addMemoryRestoreHalf(address, this.set(address, value, 2)) : this.set(address, value, 2);
    }
    
    public int setByte(final int address, final int value) throws AddressErrorException {
        return Globals.getSettings().getBackSteppingEnabled() ? Globals.program.getBackStepper().addMemoryRestoreByte(address, this.set(address, value, 1)) : this.set(address, value, 1);
    }
    
    public double setDouble(final int address, final double value) throws AddressErrorException {
        final long longValue = Double.doubleToLongBits(value);
        final int oldHighOrder = this.set(address + 4, Binary.highOrderLongToInt(longValue), 4);
        final int oldLowOrder = this.set(address, Binary.lowOrderLongToInt(longValue), 4);
        return Double.longBitsToDouble(Binary.twoIntsToLong(oldHighOrder, oldLowOrder));
    }
    
    public void setStatement(final int address, final ProgramStatement statement) throws AddressErrorException {
        if (address % 4 != 0 || (!inTextSegment(address) && !inKernelTextSegment(address))) {
            throw new AddressErrorException("store address to text segment out of range or not aligned to word boundary ", 5, address);
        }
        if (Globals.debug) {
            System.out.println("memory[" + address + "] set to " + statement.getBinaryStatement());
        }
        if (inTextSegment(address)) {
            this.storeProgramStatement(address, statement, Memory.textBaseAddress, this.textBlockTable);
        }
        else {
            this.storeProgramStatement(address, statement, Memory.kernelTextBaseAddress, this.kernelTextBlockTable);
        }
    }
    
    public int get(final int address, final int length) throws AddressErrorException {
        return this.get(address, length, true);
    }
    
    private int get(final int address, final int length, final boolean notify) throws AddressErrorException {
        int value = 0;
        if (inDataSegment(address)) {
            final int relativeByteAddress = address - Memory.dataSegmentBaseAddress;
            value = this.fetchBytesFromTable(this.dataBlockTable, relativeByteAddress, length);
        }
        else if (address > Memory.stackLimitAddress && address <= Memory.stackBaseAddress) {
            final int relativeByteAddress = Memory.stackBaseAddress - address;
            value = this.fetchBytesFromTable(this.stackBlockTable, relativeByteAddress, length);
        }
        else if (address >= Memory.memoryMapBaseAddress && address < Memory.memoryMapLimitAddress) {
            final int relativeByteAddress = address - Memory.memoryMapBaseAddress;
            value = this.fetchBytesFromTable(this.memoryMapBlockTable, relativeByteAddress, length);
        }
        else if (inTextSegment(address)) {
            if (!Globals.getSettings().getBooleanSetting(20)) {
                throw new AddressErrorException("Cannot read directly from text segment!", 4, address);
            }
            final ProgramStatement stmt = this.getStatementNoNotify(address);
            value = ((stmt == null) ? 0 : stmt.getBinaryStatement());
        }
        else if (inKernelDataSegment(address)) {
            final int relativeByteAddress = address - Memory.kernelDataBaseAddress;
            value = this.fetchBytesFromTable(this.kernelDataBlockTable, relativeByteAddress, length);
        }
        else {
            if (inKernelTextSegment(address)) {
                throw new AddressErrorException("DEVELOPER: You must use getStatement() to read from kernel text segment!", 4, address);
            }
            throw new AddressErrorException("address out of range ", 4, address);
        }
        if (notify) {
            this.notifyAnyObservers(0, address, length, value);
        }
        return value;
    }
    
    public int getRawWord(final int address) throws AddressErrorException {
        int value = 0;
        if (address % 4 != 0) {
            throw new AddressErrorException("address for fetch not aligned on word boundary", 4, address);
        }
        if (inDataSegment(address)) {
            final int relative = address - Memory.dataSegmentBaseAddress >> 2;
            value = this.fetchWordFromTable(this.dataBlockTable, relative);
        }
        else if (address > Memory.stackLimitAddress && address <= Memory.stackBaseAddress) {
            final int relative = Memory.stackBaseAddress - address >> 2;
            value = this.fetchWordFromTable(this.stackBlockTable, relative);
        }
        else if (address >= Memory.memoryMapBaseAddress && address < Memory.memoryMapLimitAddress) {
            final int relative = address - Memory.memoryMapBaseAddress >> 2;
            value = this.fetchWordFromTable(this.memoryMapBlockTable, relative);
        }
        else if (inTextSegment(address)) {
            if (!Globals.getSettings().getBooleanSetting(20)) {
                throw new AddressErrorException("Cannot read directly from text segment!", 4, address);
            }
            final ProgramStatement stmt = this.getStatementNoNotify(address);
            value = ((stmt == null) ? 0 : stmt.getBinaryStatement());
        }
        else if (inKernelDataSegment(address)) {
            final int relative = address - Memory.kernelDataBaseAddress >> 2;
            value = this.fetchWordFromTable(this.kernelDataBlockTable, relative);
        }
        else {
            if (inKernelTextSegment(address)) {
                throw new AddressErrorException("DEVELOPER: You must use getStatement() to read from kernel text segment!", 4, address);
            }
            throw new AddressErrorException("address out of range ", 4, address);
        }
        this.notifyAnyObservers(0, address, 4, value);
        return value;
    }
    
    public Integer getRawWordOrNull(final int address) throws AddressErrorException {
        Integer value = null;
        if (address % 4 != 0) {
            throw new AddressErrorException("address for fetch not aligned on word boundary", 4, address);
        }
        if (inDataSegment(address)) {
            final int relative = address - Memory.dataSegmentBaseAddress >> 2;
            value = this.fetchWordOrNullFromTable(this.dataBlockTable, relative);
        }
        else if (address > Memory.stackLimitAddress && address <= Memory.stackBaseAddress) {
            final int relative = Memory.stackBaseAddress - address >> 2;
            value = this.fetchWordOrNullFromTable(this.stackBlockTable, relative);
        }
        else {
            if (!inTextSegment(address)) {
                if (!inKernelTextSegment(address)) {
                    if (inKernelDataSegment(address)) {
                        final int relative = address - Memory.kernelDataBaseAddress >> 2;
                        value = this.fetchWordOrNullFromTable(this.kernelDataBlockTable, relative);
                        return value;
                    }
                    throw new AddressErrorException("address out of range ", 4, address);
                }
            }
            try {
                value = ((this.getStatementNoNotify(address) == null) ? null : new Integer(this.getStatementNoNotify(address).getBinaryStatement()));
            }
            catch (AddressErrorException aee) {
                value = null;
            }
        }
        return value;
    }
    
    public int getAddressOfFirstNull(final int baseAddress, final int limitAddress) throws AddressErrorException {
        int address;
        for (address = baseAddress; address < limitAddress && this.getRawWordOrNull(address) != null; address += 4) {}
        return address;
    }
    
    public int getWord(final int address) throws AddressErrorException {
        if (address % 4 != 0) {
            throw new AddressErrorException("fetch address not aligned on word boundary ", 4, address);
        }
        return this.get(address, 4, true);
    }
    
    public int getWordNoNotify(final int address) throws AddressErrorException {
        if (address % 4 != 0) {
            throw new AddressErrorException("fetch address not aligned on word boundary ", 4, address);
        }
        return this.get(address, 4, false);
    }
    
    public int getHalf(final int address) throws AddressErrorException {
        if (address % 2 != 0) {
            throw new AddressErrorException("fetch address not aligned on halfword boundary ", 4, address);
        }
        return this.get(address, 2);
    }
    
    public int getByte(final int address) throws AddressErrorException {
        return this.get(address, 1);
    }
    
    public ProgramStatement getStatement(final int address) throws AddressErrorException {
        return this.getStatement(address, true);
    }
    
    public ProgramStatement getStatementNoNotify(final int address) throws AddressErrorException {
        return this.getStatement(address, false);
    }
    
    private ProgramStatement getStatement(final int address, final boolean notify) throws AddressErrorException {
        if (!wordAligned(address)) {
            throw new AddressErrorException("fetch address for text segment not aligned to word boundary ", 4, address);
        }
        if (!Globals.getSettings().getBooleanSetting(20) && !inTextSegment(address) && !inKernelTextSegment(address)) {
            throw new AddressErrorException("fetch address for text segment out of range ", 4, address);
        }
        if (inTextSegment(address)) {
            return this.readProgramStatement(address, Memory.textBaseAddress, this.textBlockTable, notify);
        }
        if (inKernelTextSegment(address)) {
            return this.readProgramStatement(address, Memory.kernelTextBaseAddress, this.kernelTextBlockTable, notify);
        }
        return new ProgramStatement(this.get(address, 4), address);
    }
    
    public static boolean wordAligned(final int address) {
        return address % 4 == 0;
    }
    
    public static boolean doublewordAligned(final int address) {
        return address % 8 == 0;
    }
    
    public static int alignToWordBoundary(int address) {
        if (!wordAligned(address)) {
            if (address > 0) {
                address += 4 - address % 4;
            }
            else {
                address -= 4 - address % 4;
            }
        }
        return address;
    }
    
    public static boolean inTextSegment(final int address) {
        return address >= Memory.textBaseAddress && address < Memory.textLimitAddress;
    }
    
    public static boolean inKernelTextSegment(final int address) {
        return address >= Memory.kernelTextBaseAddress && address < Memory.kernelTextLimitAddress;
    }
    
    public static boolean inDataSegment(final int address) {
        return address >= Memory.dataSegmentBaseAddress && address < Memory.dataSegmentLimitAddress;
    }
    
    public static boolean inKernelDataSegment(final int address) {
        return address >= Memory.kernelDataBaseAddress && address < Memory.kernelDataSegmentLimitAddress;
    }
    
    public static boolean inMemoryMapSegment(final int address) {
        return address >= Memory.memoryMapBaseAddress && address < Memory.kernelHighAddress;
    }
    
    @Override
    public void addObserver(final Observer obs) {
        try {
            this.addObserver(obs, 0, 2147483644);
            this.addObserver(obs, Integer.MIN_VALUE, -4);
        }
        catch (AddressErrorException aee) {
            System.out.println("Internal Error in Memory.addObserver: " + aee);
        }
    }
    
    public void addObserver(final Observer obs, final int addr) throws AddressErrorException {
        this.addObserver(obs, addr, addr);
    }
    
    public void addObserver(final Observer obs, final int startAddr, final int endAddr) throws AddressErrorException {
        if (startAddr % 4 != 0) {
            throw new AddressErrorException("address not aligned on word boundary ", 4, startAddr);
        }
        if (endAddr != startAddr && endAddr % 4 != 0) {
            throw new AddressErrorException("address not aligned on word boundary ", 4, startAddr);
        }
        if (startAddr >= 0 && endAddr < 0) {
            throw new AddressErrorException("range cannot cross 0x8000000; please split it up", 4, startAddr);
        }
        if (endAddr < startAddr) {
            throw new AddressErrorException("end address of range < start address of range ", 4, startAddr);
        }
        this.observables.add(new MemoryObservable(obs, startAddr, endAddr));
    }
    
    @Override
    public int countObservers() {
        return this.observables.size();
    }
    
    @Override
    public void deleteObserver(final Observer obs) {
        final Iterator<MemoryObservable> it = this.observables.iterator();
        while (it.hasNext()) {
            it.next().deleteObserver(obs);
        }
    }
    
    @Override
    public void deleteObservers() {
        this.observables = this.getNewMemoryObserversCollection();
    }
    
    @Override
    public void notifyObservers() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void notifyObservers(final Object obj) {
        throw new UnsupportedOperationException();
    }
    
    private Collection getNewMemoryObserversCollection() {
        return new Vector();
    }
    
    private void notifyAnyObservers(final int type, final int address, final int length, final int value) {
        if ((Globals.program != null || Globals.getGui() == null) && this.observables.size() > 0) {
            for (final MemoryObservable mo : this.observables) {
                if (mo.match(address)) {
                    mo.notifyObserver(new MemoryAccessNotice(type, address, length, value));
                }
            }
        }
    }
    
    private int storeBytesInTable(final int[][] blockTable, final int relativeByteAddress, final int length, final int value) {
        return this.storeOrFetchBytesInTable(blockTable, relativeByteAddress, length, value, true);
    }
    
    private int fetchBytesFromTable(final int[][] blockTable, final int relativeByteAddress, final int length) {
        return this.storeOrFetchBytesInTable(blockTable, relativeByteAddress, length, 0, false);
    }
    
    private synchronized int storeOrFetchBytesInTable(final int[][] blockTable, int relativeByteAddress, final int length, int value, final boolean op) {
        int oldValue = 0;
        final int loopStopper = 3 - length;
        if (blockTable == this.stackBlockTable) {
            final int delta = relativeByteAddress % 4;
            if (delta != 0) {
                relativeByteAddress += 4 - delta << 1;
            }
        }
        for (int bytePositionInValue = 3; bytePositionInValue > loopStopper; --bytePositionInValue) {
            int bytePositionInMemory = relativeByteAddress % 4;
            final int relativeWordAddress = relativeByteAddress >> 2;
            final int block = relativeWordAddress / 1024;
            final int offset = relativeWordAddress % 1024;
            if (blockTable[block] == null) {
                if (!op) {
                    return 0;
                }
                blockTable[block] = new int[1024];
            }
            if (Memory.byteOrder) {
                bytePositionInMemory = 3 - bytePositionInMemory;
            }
            if (op) {
                oldValue = this.replaceByte(blockTable[block][offset], bytePositionInMemory, oldValue, bytePositionInValue);
                blockTable[block][offset] = this.replaceByte(value, bytePositionInValue, blockTable[block][offset], bytePositionInMemory);
            }
            else {
                value = this.replaceByte(blockTable[block][offset], bytePositionInMemory, value, bytePositionInValue);
            }
            ++relativeByteAddress;
        }
        return op ? oldValue : value;
    }
    
    private synchronized int storeWordInTable(final int[][] blockTable, final int relative, final int value) {
        final int block = relative / 1024;
        final int offset = relative % 1024;
        if (blockTable[block] == null) {
            blockTable[block] = new int[1024];
        }
        final int oldValue = blockTable[block][offset];
        blockTable[block][offset] = value;
        return oldValue;
    }
    
    private synchronized int fetchWordFromTable(final int[][] blockTable, final int relative) {
        int value = 0;
        final int block = relative / 1024;
        final int offset = relative % 1024;
        if (blockTable[block] == null) {
            value = 0;
        }
        else {
            value = blockTable[block][offset];
        }
        return value;
    }
    
    private synchronized Integer fetchWordOrNullFromTable(final int[][] blockTable, final int relative) {
        int value = 0;
        final int block = relative / 1024;
        final int offset = relative % 1024;
        if (blockTable[block] == null) {
            return null;
        }
        value = blockTable[block][offset];
        return new Integer(value);
    }
    
    private int replaceByte(final int sourceValue, final int bytePosInSource, final int destValue, final int bytePosInDest) {
        return (sourceValue >> 24 - (bytePosInSource << 3) & 0xFF) << 24 - (bytePosInDest << 3) | (destValue & ~(255 << 24 - (bytePosInDest << 3)));
    }
    
    private int reverseBytes(final int source) {
        return (source >> 24 & 0xFF) | (source >> 8 & 0xFF00) | (source << 8 & 0xFF0000) | source << 24;
    }
    
    private void storeProgramStatement(final int address, final ProgramStatement statement, final int baseAddress, final ProgramStatement[][] blockTable) {
        final int relative = address - baseAddress >> 2;
        final int block = relative / 1024;
        final int offset = relative % 1024;
        if (block < 1024) {
            if (blockTable[block] == null) {
                blockTable[block] = new ProgramStatement[1024];
            }
            blockTable[block][offset] = statement;
        }
    }
    
    private ProgramStatement readProgramStatement(final int address, final int baseAddress, final ProgramStatement[][] blockTable, final boolean notify) {
        final int relative = address - baseAddress >> 2;
        final int block = relative / 1024;
        final int offset = relative % 1024;
        if (block >= 1024) {
            if (notify) {
                this.notifyAnyObservers(0, address, 4, 0);
            }
            return null;
        }
        if (blockTable[block] == null || blockTable[block][offset] == null) {
            if (notify) {
                this.notifyAnyObservers(0, address, 4, 0);
            }
            return null;
        }
        if (notify) {
            this.notifyAnyObservers(0, address, 4, blockTable[block][offset].getBinaryStatement());
        }
        return blockTable[block][offset];
    }
    
    static {
        Memory.textBaseAddress = MemoryConfigurations.getDefaultTextBaseAddress();
        Memory.dataSegmentBaseAddress = MemoryConfigurations.getDefaultDataSegmentBaseAddress();
        Memory.externBaseAddress = MemoryConfigurations.getDefaultExternBaseAddress();
        Memory.globalPointer = MemoryConfigurations.getDefaultGlobalPointer();
        Memory.dataBaseAddress = MemoryConfigurations.getDefaultDataBaseAddress();
        Memory.heapBaseAddress = MemoryConfigurations.getDefaultHeapBaseAddress();
        Memory.stackPointer = MemoryConfigurations.getDefaultStackPointer();
        Memory.stackBaseAddress = MemoryConfigurations.getDefaultStackBaseAddress();
        Memory.userHighAddress = MemoryConfigurations.getDefaultUserHighAddress();
        Memory.kernelBaseAddress = MemoryConfigurations.getDefaultKernelBaseAddress();
        Memory.kernelTextBaseAddress = MemoryConfigurations.getDefaultKernelTextBaseAddress();
        Memory.exceptionHandlerAddress = MemoryConfigurations.getDefaultExceptionHandlerAddress();
        Memory.kernelDataBaseAddress = MemoryConfigurations.getDefaultKernelDataBaseAddress();
        Memory.memoryMapBaseAddress = MemoryConfigurations.getDefaultMemoryMapBaseAddress();
        Memory.kernelHighAddress = MemoryConfigurations.getDefaultKernelHighAddress();
        Memory.byteOrder = true;
        Memory.dataSegmentLimitAddress = Memory.dataSegmentBaseAddress + 4194304;
        Memory.textLimitAddress = Memory.textBaseAddress + 4194304;
        Memory.kernelDataSegmentLimitAddress = Memory.kernelDataBaseAddress + 4194304;
        Memory.kernelTextLimitAddress = Memory.kernelTextBaseAddress + 4194304;
        Memory.stackLimitAddress = Memory.stackBaseAddress - 4194304;
        Memory.memoryMapLimitAddress = Memory.memoryMapBaseAddress + 65536;
        Memory.uniqueMemoryInstance = new Memory();
    }
    
    private class MemoryObservable extends Observable implements Comparable
    {
        private int lowAddress;
        private int highAddress;
        
        public MemoryObservable(final Observer obs, final int startAddr, final int endAddr) {
            this.lowAddress = startAddr;
            this.highAddress = endAddr;
            this.addObserver(obs);
        }
        
        public boolean match(final int address) {
            return address >= this.lowAddress && address <= this.highAddress - 1 + 4;
        }
        
        public void notifyObserver(final MemoryAccessNotice notice) {
            this.setChanged();
            this.notifyObservers(notice);
        }
        
        @Override
        public int compareTo(final Object obj) {
            if (!(obj instanceof MemoryObservable)) {
                throw new ClassCastException();
            }
            final MemoryObservable mo = (MemoryObservable)obj;
            if (this.lowAddress < mo.lowAddress || (this.lowAddress == mo.lowAddress && this.highAddress < mo.highAddress)) {
                return -1;
            }
            if (this.lowAddress > mo.lowAddress || (this.lowAddress == mo.lowAddress && this.highAddress > mo.highAddress)) {
                return -1;
            }
            return 0;
        }
    }
}
