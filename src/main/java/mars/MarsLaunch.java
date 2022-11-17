

package mars;

import java.util.Iterator;
import mars.mips.hardware.InvalidRegisterAccessException;
import mars.pipeline.pipes.StaticPipe;
import mars.mips.hardware.RegisterAccessNotice;
import mars.pipeline.Decode;
import mars.pipeline.tomasulo.Tomasulo;
import mars.pipeline.StageRegisters;
import mars.mips.hardware.MemoryAccessNotice;
import mars.mips.hardware.AccessNotice;
import java.util.Observable;
import java.util.Observer;
import mars.mips.hardware.Memory;
import mars.simulator.ProgramArgumentList;
import java.util.Collection;
import mars.util.FilenameFinder;
import mars.mips.hardware.MemoryConfiguration;
import java.util.logging.Level;
import java.util.logging.Logger;
import mars.pipeline.pipe_config;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.RegisterFile;
import javax.swing.SwingUtilities;
import mars.venus.VenusUI;
import mars.mips.dump.DumpFormat;
import java.io.IOException;
import mars.mips.hardware.AddressErrorException;
import java.io.FileNotFoundException;
import mars.mips.dump.DumpFormatLoader;
import mars.util.Binary;
import mars.util.MemoryDump;
import java.io.File;
import mars.mips.hardware.MemoryConfigurations;
import mars.pipeline.Pipeline;
import mars.pipeline.Stage;
import mars.pipeline.BranchPredictor;
import java.io.PrintStream;
import java.util.ArrayList;

public class MarsLaunch
{
    private boolean simulate;
    private int displayFormat;
    private boolean verbose;
    private boolean assembleProject;
    private boolean pseudo;
    private boolean warningsAreErrors;
    private boolean startAtMain;
    private boolean countInstructions;
    private boolean selfModifyingCode;
    private static final String rangeSeparator = "-";
    private static final int splashDuration = 2000;
    private static final int memoryWordsPerLine = 4;
    private static final int DECIMAL = 0;
    private static final int HEXADECIMAL = 1;
    private static final int ASCII = 2;
    private ArrayList registerDisplayList;
    private ArrayList memoryDisplayList;
    private ArrayList<String> filenameList;
    private MIPSprogram code;
    private int maxSteps;
    private int instructionCount;
    private int instructionCount_orig;
    private PrintStream out;
    private ArrayList<String[]> dumpTriples;
    private ArrayList programArgumentList;
    private int assembleErrorExitCode;
    private int simulateErrorExitCode;
    private BranchPredictor.BranchPredictor_type branch_type;
    private Stage branch_stage;
    protected Pipeline.planificacion_t planificacion;
    private boolean hasPipe;
    private String pipe_filename;
    private Pipeline pipe2;
    
    public MarsLaunch(final String[] args) {
        this.dumpTriples = null;
        final boolean gui = args.length == 0;
        Globals.initialize(gui);
        if (gui) {
            this.launchIDE();
        }
        else {
            System.setProperty("java.awt.headless", "true");
            this.simulate = true;
            this.displayFormat = 1;
            this.verbose = true;
            this.assembleProject = false;
            this.pseudo = true;
            this.warningsAreErrors = false;
            this.startAtMain = false;
            this.countInstructions = false;
            this.selfModifyingCode = true;
            this.instructionCount = 0;
            this.instructionCount_orig = 0;
            this.branch_type = BranchPredictor.BranchPredictor_type.ideal;
            this.hasPipe = false;
            this.assembleErrorExitCode = 0;
            this.simulateErrorExitCode = 0;
            this.registerDisplayList = new ArrayList();
            this.memoryDisplayList = new ArrayList();
            this.filenameList = new ArrayList();
            MemoryConfigurations.setCurrentConfiguration(MemoryConfigurations.getDefaultConfiguration());
            this.code = new MIPSprogram();
            this.maxSteps = -1;
            this.out = System.out;
            if (this.parseCommandArgs(args)) {
                if (this.runCommand()) {
                    this.displayMiscellaneousPostMortem();
                    this.displayRegistersPostMortem();
                    this.displayMemoryPostMortem();
                }
                this.dumpSegments();
            }
            System.exit(Globals.exitCode);
        }
    }
    
    private void dumpSegments() {
        if (this.dumpTriples == null) {
            return;
        }
        for (int i = 0; i < this.dumpTriples.size(); ++i) {
            final String[] triple = this.dumpTriples.get(i);
            final File file = new File(triple[2]);
            Integer[] segInfo = MemoryDump.getSegmentBounds(triple[0]);
            if (segInfo == null) {
                try {
                    final String[] memoryRange = this.checkMemoryAddressRange(triple[0]);
                    segInfo = new Integer[] { new Integer(Binary.stringToInt(memoryRange[0])), new Integer(Binary.stringToInt(memoryRange[1])) };
                }
                catch (NumberFormatException nfe) {
                    segInfo = null;
                }
                catch (NullPointerException npe) {
                    segInfo = null;
                }
            }
            if (segInfo == null) {
                this.out.println("Error while attempting to save dump, segment/address-range " + triple[0] + " is invalid!");
            }
            else {
                final DumpFormatLoader loader = new DumpFormatLoader();
                final ArrayList dumpFormats = loader.loadDumpFormats();
                final DumpFormat format = DumpFormatLoader.findDumpFormatGivenCommandDescriptor(dumpFormats, triple[1]);
                if (format == null) {
                    this.out.println("Error while attempting to save dump, format " + triple[1] + " was not found!");
                }
                else {
                    try {
                        final int highAddress = Globals.memory.getAddressOfFirstNull(segInfo[0], segInfo[1]) - 4;
                        if (highAddress < segInfo[0]) {
                            this.out.println("This segment has not been written to, there is nothing to dump.");
                        }
                        else {
                            format.dumpMemoryRange(file, segInfo[0], highAddress);
                        }
                    }
                    catch (FileNotFoundException e2) {
                        this.out.println("Error while attempting to save dump, file " + file + " was not found!");
                    }
                    catch (AddressErrorException e) {
                        this.out.println("Error while attempting to save dump, file " + file + "!  Could not access address: " + e.getAddress() + "!");
                    }
                    catch (IOException e3) {
                        this.out.println("Error while attempting to save dump, file " + file + "!  Disk IO failed!");
                    }
                }
            }
        }
    }
    
    private void launchIDE() {
        new MarsSplashScreen(2000).showSplash();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new VenusUI("MARS 4.5");
            }
        });
    }
    
    private boolean parseCommandArgs(final String[] args) {
        final String noCopyrightSwitch = "nc";
        final String displayMessagesToErrSwitch = "me";
        boolean argsOK = true;
        boolean inProgramArgumentList = false;
        this.programArgumentList = null;
        if (args.length == 0) {
            return true;
        }
        this.processDisplayMessagesToErrSwitch(args, displayMessagesToErrSwitch);
        this.displayCopyright(args, noCopyrightSwitch);
        for (int i = 0; i < args.length; ++i) {
            if (inProgramArgumentList) {
                if (this.programArgumentList == null) {
                    this.programArgumentList = new ArrayList();
                }
                this.programArgumentList.add(args[i]);
            }
            else if (args[i].toLowerCase().equals("pa")) {
                inProgramArgumentList = true;
            }
            else if (!args[i].toLowerCase().equals(displayMessagesToErrSwitch)) {
                if (!args[i].toLowerCase().equals(noCopyrightSwitch)) {
                    if (args[i].toLowerCase().equals("dump")) {
                        if (args.length <= i + 3) {
                            this.out.println("Dump command line argument requires a segment, format and file name.");
                            argsOK = false;
                        }
                        else {
                            if (this.dumpTriples == null) {
                                this.dumpTriples = new ArrayList();
                            }
                            this.dumpTriples.add(new String[] { args[++i], args[++i], args[++i] });
                        }
                    }
                    else if (args[i].toLowerCase().equals("mc")) {
                        final String configName = args[++i];
                        final MemoryConfiguration config = MemoryConfigurations.getConfigurationByName(configName);
                        if (config == null) {
                            this.out.println("Invalid memory configuration: " + configName);
                            argsOK = false;
                        }
                        else {
                            MemoryConfigurations.setCurrentConfiguration(config);
                        }
                    }
                    else {
                        if (args[i].toLowerCase().indexOf("ae") == 0) {
                            final String s = args[i].substring(2);
                            try {
                                this.assembleErrorExitCode = Integer.decode(s);
                                continue;
                            }
                            catch (NumberFormatException ex2) {}
                        }
                        if (args[i].toLowerCase().indexOf("se") == 0) {
                            final String s = args[i].substring(2);
                            try {
                                this.simulateErrorExitCode = Integer.decode(s);
                                continue;
                            }
                            catch (NumberFormatException ex3) {}
                        }
                        if (args[i].toLowerCase().equals("d")) {
                            Globals.debug = true;
                        }
                        else if (args[i].toLowerCase().equals("a")) {
                            this.simulate = false;
                        }
                        else if (args[i].toLowerCase().equals("ad") || args[i].toLowerCase().equals("da")) {
                            Globals.debug = true;
                            this.simulate = false;
                        }
                        else if (args[i].toLowerCase().equals("p")) {
                            this.assembleProject = true;
                        }
                        else if (args[i].toLowerCase().equals("dec")) {
                            this.displayFormat = 0;
                        }
                        else if (args[i].toLowerCase().equals("hex")) {
                            this.displayFormat = 1;
                        }
                        else if (args[i].toLowerCase().equals("ascii")) {
                            this.displayFormat = 2;
                        }
                        else if (args[i].toLowerCase().equals("pipe")) {
                            if (args.length <= i + 1) {
                                this.out.println("pipe command line argument requires a file name.");
                                argsOK = false;
                            }
                            else {
                                this.hasPipe = true;
                                this.pipe_filename = args[++i];
                            }
                        }
                        else if (args[i].toLowerCase().equals("b")) {
                            this.verbose = false;
                        }
                        else if (args[i].toLowerCase().equals("np") || args[i].toLowerCase().equals("ne")) {
                            this.pseudo = false;
                        }
                        else if (args[i].toLowerCase().equals("we")) {
                            this.warningsAreErrors = true;
                        }
                        else if (args[i].toLowerCase().equals("sm")) {
                            this.startAtMain = true;
                        }
                        else if (args[i].toLowerCase().equals("smc")) {
                            this.selfModifyingCode = true;
                        }
                        else if (args[i].toLowerCase().equals("ic")) {
                            this.countInstructions = true;
                        }
                        else {
                            if (args[i].equals("h")) {
                                this.displayHelp();
                                return false;
                            }
                            if (args[i].indexOf("$") == 0) {
                                if (RegisterFile.getUserRegister(args[i]) == null && Coprocessor1.getRegister(args[i]) == null) {
                                    this.out.println("Invalid Register Name: " + args[i]);
                                }
                                else {
                                    this.registerDisplayList.add(args[i]);
                                }
                            }
                            else if (RegisterFile.getUserRegister("$" + args[i]) != null || Coprocessor1.getRegister("$" + args[i]) != null) {
                                this.registerDisplayList.add("$" + args[i]);
                            }
                            else if (new File(args[i]).exists()) {
                                this.filenameList.add(args[i]);
                            }
                            else {
                                try {
                                    Integer.decode(args[i]);
                                    this.maxSteps = Integer.decode(args[i]);
                                }
                                catch (NumberFormatException ex4) {
                                    try {
                                        final String[] memoryRange = this.checkMemoryAddressRange(args[i]);
                                        this.memoryDisplayList.add(memoryRange[0]);
                                        this.memoryDisplayList.add(memoryRange[1]);
                                    }
                                    catch (NumberFormatException nfe) {
                                        this.out.println("Invalid/unaligned address or invalid range: " + args[i]);
                                        argsOK = false;
                                    }
                                    catch (NullPointerException ex5) {
                                        this.out.println("Invalid Command Argument: " + args[i]);
                                        argsOK = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (this.hasPipe) {
            final pipe_config config2 = new pipe_config(this.pipe_filename);
            try {
                this.pipe2 = config2.getPipeline();
                if (!pipe_config.isConcurso() && !pipe_config.isMagic()) {
                    System.out.println(this.pipe2.toString());
                }
                this.branch_stage = this.pipe2.getBranchResolve();
                this.planificacion = this.pipe2.getPlanificacion();
                this.branch_type = this.pipe2.getPreditor_type();
            }
            catch (pipe_config.ExceptionXML ex) {
                Logger.getLogger(MarsLaunch.class.getName()).log(Level.SEVERE, null, ex);
                argsOK = false;
            }
            if (this.branch_stage != Stage.ID && this.branch_type == BranchPredictor.BranchPredictor_type.delayedBranch) {
                this.out.println("Invalid Arguments: delayed Branch is only compatible with resolving branches on ID stage");
                argsOK = false;
            }
            if (this.planificacion == Pipeline.planificacion_t.dinamica && this.branch_type != BranchPredictor.BranchPredictor_type.ideal && this.branch_type != BranchPredictor.BranchPredictor_type.taken && this.branch_type != BranchPredictor.BranchPredictor_type.notTaken && this.branch_type != BranchPredictor.BranchPredictor_type.btb) {
                this.out.println("Predictor no v\u00e1lido para Tomasulo");
                argsOK = false;
            }
        }
        return argsOK;
    }
    
    private boolean runCommand() {
        boolean programRan = false;
        if (this.filenameList.isEmpty()) {
            return programRan;
        }
        try {
            final boolean delayedBranching = this.branch_type == BranchPredictor.BranchPredictor_type.delayedBranch;
            Globals.getSettings().setBooleanSettingNonPersistent(8, delayedBranching);
            Globals.getSettings().setBooleanSettingNonPersistent(20, this.selfModifyingCode);
            final File mainFile = new File(this.filenameList.get(0)).getAbsoluteFile();
            ArrayList filesToAssemble;
            if (this.assembleProject) {
                filesToAssemble = FilenameFinder.getFilenameList(mainFile.getParent(), Globals.fileExtensions);
                if (this.filenameList.size() > 1) {
                    this.filenameList.remove(0);
                    final ArrayList moreFilesToAssemble = FilenameFinder.getFilenameList(this.filenameList, FilenameFinder.MATCH_ALL_EXTENSIONS);
                    for (int index2 = 0; index2 < moreFilesToAssemble.size(); ++index2) {
                        for (int index3 = 0; index3 < filesToAssemble.size(); ++index3) {
                            if (filesToAssemble.get(index3).equals(moreFilesToAssemble.get(index2))) {
                                moreFilesToAssemble.remove(index2);
                                --index2;
                                break;
                            }
                        }
                    }
                    filesToAssemble.addAll(moreFilesToAssemble);
                }
            }
            else {
                filesToAssemble = FilenameFinder.getFilenameList(this.filenameList, FilenameFinder.MATCH_ALL_EXTENSIONS);
            }
            if (Globals.debug) {
                this.out.println("--------  TOKENIZING BEGINS  -----------");
            }
            final ArrayList MIPSprogramsToAssemble = this.code.prepareFilesForAssembly(filesToAssemble, mainFile.getAbsolutePath(), null);
            if (Globals.debug) {
                this.out.println("--------  ASSEMBLY BEGINS  -----------");
            }
            final ErrorList warnings = this.code.assemble(MIPSprogramsToAssemble, this.pseudo, this.warningsAreErrors);
            if (warnings != null && warnings.warningsOccurred()) {
                this.out.println(warnings.generateWarningReport());
            }
            RegisterFile.initializeProgramCounter(this.startAtMain);
            if (this.simulate) {
                new ProgramArgumentList(this.programArgumentList).storeProgramArguments();
                if (this.hasPipe) {
                    if (this.planificacion == Pipeline.planificacion_t.dinamica) {
                        this.establishTomasuloObserver();
                    }
                    else {
                        this.establishObserver();
                    }
                }
                this.establishOriginalObserver();
                if (Globals.debug) {
                    this.out.println("--------  SIMULATION BEGINS  -----------");
                }
                programRan = true;
                final boolean done = this.code.simulate(this.maxSteps);
                if (!done) {
                    this.out.println("\nProgram terminated when maximum step limit " + this.maxSteps + " reached.");
                }
            }
            if (Globals.debug) {
                this.out.println("\n--------  ALL PROCESSING COMPLETE  -----------");
            }
        }
        catch (ProcessingException e) {
            Globals.exitCode = (programRan ? this.simulateErrorExitCode : this.assembleErrorExitCode);
            this.out.println(e.errors().generateErrorAndWarningReport());
            this.out.println("Processing terminated due to errors.");
        }
        return programRan;
    }
    
    private String[] checkMemoryAddressRange(final String arg) throws NumberFormatException {
        String[] memoryRange = null;
        if (arg.indexOf("-") > 0 && arg.indexOf("-") < arg.length() - 1) {
            memoryRange = new String[] { arg.substring(0, arg.indexOf("-")), arg.substring(arg.indexOf("-") + 1) };
            if (Binary.stringToInt(memoryRange[0]) > Binary.stringToInt(memoryRange[1]) || !Memory.wordAligned(Binary.stringToInt(memoryRange[0])) || !Memory.wordAligned(Binary.stringToInt(memoryRange[1]))) {
                throw new NumberFormatException();
            }
        }
        return memoryRange;
    }
    
    private void establishOriginalObserver() {
        if (this.countInstructions) {
            final Observer instructionCounter = new Observer() {
                private int lastAddress = 0;
                
                @Override
                public void update(final Observable o, final Object obj) {
                    if (obj instanceof AccessNotice) {
                        final AccessNotice notice = (AccessNotice)obj;
                        if (!notice.accessIsFromMIPS()) {
                            return;
                        }
                        if (notice.getAccessType() != 0) {
                            return;
                        }
                        final MemoryAccessNotice m = (MemoryAccessNotice)notice;
                        final int a = m.getAddress();
                        if (a == this.lastAddress) {
                            return;
                        }
                        this.lastAddress = a;
                        MarsLaunch.this.instructionCount_orig++;
                    }
                }
            };
            try {
                Globals.memory.addObserver(instructionCounter, Memory.textBaseAddress, Memory.textLimitAddress);
            }
            catch (AddressErrorException aee) {
                this.out.println("Internal error: MarsLaunch uses incorrect text segment address for instruction observer");
            }
        }
    }
    
    private void establishObserver() {
        final Observer instructionCounter = new Observer() {
            private int lastAddress = 0;
            private int lastLaunch = -100;
            
            @Override
            public void update(final Observable o, final Object obj) {
                if (obj instanceof AccessNotice) {
                    final AccessNotice notice = (AccessNotice)obj;
                    if (!notice.accessIsFromMIPS()) {
                        return;
                    }
                    if (notice.getAccessType() != 0) {
                        return;
                    }
                    final MemoryAccessNotice m = (MemoryAccessNotice)notice;
                    final int a = m.getAddress();
                    if (a == this.lastAddress) {
                        return;
                    }
                    MarsLaunch.this.instructionCount++;
                    final int current_instruction = m.getValue();
                    MarsLaunch.this.pipe2.UpdatePipeline(a, current_instruction);
                    this.lastAddress = a;
                }
            }
        };
        try {
            Globals.memory.addObserver(instructionCounter, Memory.textBaseAddress, Memory.textLimitAddress);
        }
        catch (AddressErrorException aee) {
            this.out.println("Internal error: MarsLaunch uses incorrect text segment address for instruction observer");
        }
    }
    
    private void establishTomasuloObserver() {
        final Observer instructionCounter = new Observer() {
            boolean last_instruction_sent = false;
            private StageRegisters st_reg = new StageRegisters();
            private final Tomasulo tpipe = (Tomasulo)MarsLaunch.this.pipe2;
            
            private void launchToPipe() {
                this.tpipe.IF(this.st_reg);
                this.last_instruction_sent = true;
                this.tpipe.UpdatePipeline(this.st_reg.getAddress(), this.st_reg.getInstruction());
            }
            
            private void instructionFetchNotification(final int address, final int instruction_read) {
                this.st_reg.setAddress(address);
                final int func = Decode.getFunct(instruction_read);
                this.st_reg.setInstruction(instruction_read);
                this.last_instruction_sent = false;
                MarsLaunch.this.instructionCount++;
                this.st_reg.setRsValue(RegisterFile.getValue(Decode.getRs(instruction_read)));
                this.st_reg.setRtValue(RegisterFile.getValue(Decode.getRt(instruction_read)));
                if (Decode.getDestination(instruction_read) == 0 & (func < 24 || func > 27)) {
                    this.st_reg.setRdValue(0);
                    this.launchToPipe();
                }
            }
            
            private void RegisterUpdateNotification(final int register) {
                final int func = Decode.getFunct(this.st_reg.getInstruction());
                if (func < 24 || func > 27 || register == 34) {
                    this.st_reg.setRdValue(RegisterFile.getValue(register));
                    if (!this.last_instruction_sent) {
                        this.launchToPipe();
                    }
                }
            }
            
            @Override
            public void update(final Observable o, final Object obj) {
                final AccessNotice notice = (AccessNotice)obj;
                if (obj instanceof AccessNotice) {
                    if (!notice.accessIsFromMIPS()) {
                        return;
                    }
                    if (obj instanceof MemoryAccessNotice) {
                        if (notice.getAccessType() != 0) {
                            return;
                        }
                        final MemoryAccessNotice m = (MemoryAccessNotice)notice;
                        final int a = m.getAddress();
                        if (a == this.st_reg.getAddress()) {
                            return;
                        }
                        final int current_instruction = m.getValue();
                        this.instructionFetchNotification(a, current_instruction);
                    }
                    else if (obj instanceof RegisterAccessNotice) {
                        final RegisterAccessNotice r = (RegisterAccessNotice)notice;
                        if (r.getAccessType() != 1) {
                            return;
                        }
                        this.RegisterUpdateNotification(r.getId());
                    }
                }
            }
        };
        try {
            Globals.memory.addObserver(instructionCounter, Memory.textBaseAddress, Memory.textLimitAddress);
            RegisterFile.addRegistersObserverSinHi(instructionCounter);
        }
        catch (AddressErrorException aee) {
            this.out.println("Internal error: MarsLaunch uses incorrect terxt segment address for instruction observer");
        }
    }
    
    private void displayMiscellaneousPostMortem() {
        if (this.hasPipe) {
            this.pipe2.finalizar();
            if (pipe_config.isConcurso()) {
                this.out.println("\nCycles executed: " + (this.pipe2.getCycle() - 1));
                this.out.println("Instructions executed: " + this.pipe2.getNumInstruccionesConfirmadas());
                if (this.pipe2 instanceof StaticPipe) {
                    final StaticPipe spipe = (StaticPipe)this.pipe2;
                    this.out.println("Data Risks: " + spipe.getOther_stalls());
                    this.out.println("Control Risks: " + spipe.getBranch_stalls());
                }
            }
            else if (pipe_config.isMagic()) {
                this.out.println("\nMagic Number: " + (this.pipe2.getCycle() - 1));
                this.out.println("Instructions executed: " + this.pipe2.getNumInstruccionesConfirmadas());
            }
            else {
                this.pipe2.writeResumen();
            }
        }
        if (this.countInstructions) {
            this.out.println("\n" + this.instructionCount_orig);
        }
    }
    
    private void displayRegistersPostMortem() {
        this.out.println();
        final Iterator regIter = this.registerDisplayList.iterator();
        while (regIter.hasNext()) {
            final String reg = regIter.next().toString();
            if (RegisterFile.getUserRegister(reg) != null) {
                if (this.verbose) {
                    this.out.print(reg + "\t");
                }
                final int value = RegisterFile.getUserRegister(reg).getValue();
                this.out.println(this.formatIntForDisplay(value));
            }
            else {
                final float fvalue = Coprocessor1.getFloatFromRegister(reg);
                final int ivalue = Coprocessor1.getIntFromRegister(reg);
                double dvalue = Double.NaN;
                long lvalue = 0L;
                boolean hasDouble = false;
                try {
                    dvalue = Coprocessor1.getDoubleFromRegisterPair(reg);
                    lvalue = Coprocessor1.getLongFromRegisterPair(reg);
                    hasDouble = true;
                }
                catch (InvalidRegisterAccessException ex) {}
                if (this.verbose) {
                    this.out.print(reg + "\t");
                }
                if (this.displayFormat == 1) {
                    this.out.print(Binary.binaryStringToHexString(Binary.intToBinaryString(ivalue)));
                    if (hasDouble) {
                        this.out.println("\t" + Binary.binaryStringToHexString(Binary.longToBinaryString(lvalue)));
                    }
                    else {
                        this.out.println("");
                    }
                }
                else if (this.displayFormat == 0) {
                    this.out.print(fvalue);
                    if (hasDouble) {
                        this.out.println("\t" + dvalue);
                    }
                    else {
                        this.out.println("");
                    }
                }
                else {
                    this.out.print(Binary.intToAscii(ivalue));
                    if (hasDouble) {
                        this.out.println("\t" + Binary.intToAscii(Binary.highOrderLongToInt(lvalue)) + Binary.intToAscii(Binary.lowOrderLongToInt(lvalue)));
                    }
                    else {
                        this.out.println("");
                    }
                }
            }
        }
    }
    
    private String formatIntForDisplay(final int value) {
        String strValue = null;
        switch (this.displayFormat) {
            case 0: {
                strValue = "" + value;
                break;
            }
            case 1: {
                strValue = Binary.intToHexString(value);
                break;
            }
            case 2: {
                strValue = Binary.intToAscii(value);
                break;
            }
            default: {
                strValue = Binary.intToHexString(value);
                break;
            }
        }
        return strValue;
    }
    
    private void displayMemoryPostMortem() {
        final Iterator memIter = this.memoryDisplayList.iterator();
        int addressStart = 0;
        int addressEnd = 0;
        while (memIter.hasNext()) {
            try {
                addressStart = Binary.stringToInt(memIter.next().toString());
                addressEnd = Binary.stringToInt(memIter.next().toString());
            }
            catch (NumberFormatException ex) {}
            int valuesDisplayed = 0;
            for (int addr = addressStart; addr <= addressEnd && (addr >= 0 || addressEnd <= 0); addr += 4) {
                if (valuesDisplayed % 4 == 0) {
                    this.out.print((valuesDisplayed > 0) ? "\n" : "");
                    if (this.verbose) {
                        this.out.print("Mem[" + Binary.intToHexString(addr) + "]\t");
                    }
                }
                try {
                    int value;
                    if (Memory.inTextSegment(addr) || Memory.inKernelTextSegment(addr)) {
                        final Integer iValue = Globals.memory.getRawWordOrNull(addr);
                        value = ((iValue == null) ? 0 : iValue);
                    }
                    else {
                        value = Globals.memory.getWord(addr);
                    }
                    this.out.print(this.formatIntForDisplay(value) + "\t");
                }
                catch (AddressErrorException aee) {
                    this.out.print("Invalid address: " + addr + "\t");
                }
                ++valuesDisplayed;
            }
            this.out.println();
        }
    }
    
    private void processDisplayMessagesToErrSwitch(final String[] args, final String displayMessagesToErrSwitch) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].toLowerCase().equals(displayMessagesToErrSwitch)) {
                this.out = System.err;
                return;
            }
        }
    }
    
    private void displayCopyright(final String[] args, final String noCopyrightSwitch) {
        final boolean print = true;
        for (int i = 0; i < args.length; ++i) {
            if (args[i].toLowerCase().equals(noCopyrightSwitch)) {
                return;
            }
        }
        this.out.println("MARS 4.5  Copyright " + Globals.copyrightYears + " " + Globals.copyrightHolders);
        this.out.println("MARS-F 2020-2022 Fran And\u00fajar\n");
    }
    
    private void displayHelp() {
        final String[] segmentNames = MemoryDump.getSegmentNames();
        String segments = "";
        for (int i = 0; i < segmentNames.length; ++i) {
            segments += segmentNames[i];
            if (i < segmentNames.length - 1) {
                segments += ", ";
            }
        }
        final ArrayList<DumpFormat> dumpFormats = new DumpFormatLoader().loadDumpFormats();
        String formats = "";
        for (int j = 0; j < dumpFormats.size(); ++j) {
            formats += dumpFormats.get(j).getCommandDescriptor();
            if (j < dumpFormats.size() - 1) {
                formats += ", ";
            }
        }
        this.out.println("Usage:  Mars  [options] filename [additional filenames]");
        this.out.println("-----------------------------------------------------------");
        this.out.println("  Options for Computer architecture subjects");
        this.out.println("-----------------------------------------------------------");
        this.out.println("  Valid options (not case sensitive, separate by spaces) are:");
        this.out.println("      a  -- assemble only, do not simulate");
        this.out.println("  ae<n>  -- terminate MARS with integer exit code <n> if an assemble error occurs.");
        this.out.println("  ascii  -- display memory or register contents interpreted as ASCII codes.");
        this.out.println("      b  -- brief - do not display register/memory address along with contents");
        this.out.println("      d  -- display MARS debugging statements");
        this.out.println("    dec  -- display memory or register contents in decimal.");
        this.out.println("   dump <segment> <format> <file> -- memory dump of specified memory segment");
        this.out.println("            in specified format to specified file.  Option may be repeated.");
        this.out.println("            Dump occurs at the end of simulation unless 'a' option is used.");
        this.out.println("            Segment and format are case-sensitive and possible values are:");
        this.out.println("            <segment> = " + segments);
        this.out.println("            <format> = " + formats);
        this.out.println("      h  -- display this help.  Use by itself with no filename.");
        this.out.println("    hex  -- display memory or register contents in hexadecimal (default)");
        this.out.println("     mc <config>  -- set memory configuration.  Argument <config> is");
        this.out.println("            case-sensitive and possible values are: Default for the default");
        this.out.println("            32-bit address space, CompactDataAtZero for a 32KB memory with");
        this.out.println("            data segment at address 0, or CompactTextAtZero for a 32KB");
        this.out.println("            memory with text segment at address 0.");
        this.out.println("     me  -- display MARS messages to standard err instead of standard out. ");
        this.out.println("            Can separate messages from program output using redirection");
        this.out.println("     nc  -- do not display copyright notice (for cleaner redirected/piped output).");
        this.out.println("     np  -- use of pseudo instructions and formats not permitted");
        this.out.println("      p  -- Project mode - assemble all files in the same directory as given file.");
        this.out.println("  se<n>  -- terminate MARS with integer exit code <n> if a simulation (run) error occurs.");
        this.out.println("     sm  -- start execution at statement with global label main, if defined");
        this.out.println("    smc  -- Self Modifying Code - Program can write and branch to either text or data segment");
        this.out.println("    <n>  -- where <n> is an integer maximum count of steps to simulate.");
        this.out.println("            If 0, negative or not specified, there is no maximum.");
        this.out.println(" $<reg>  -- where <reg> is number or name (e.g. 5, t3, f10) of register whose ");
        this.out.println("            content to display at end of run.  Option may be repeated.");
        this.out.println("<reg_name>  -- where <reg_name> is name (e.g. t3, f10) of register whose");
        this.out.println("            content to display at end of run.  Option may be repeated. ");
        this.out.println("            The $ is not required.");
        this.out.println("<m>-<n>  -- memory address range from <m> to <n> whose contents to");
        this.out.println("            display at end of run. <m> and <n> may be hex or decimal,");
        this.out.println("            must be on word boundary, <m> <= <n>.  Option may be repeated.");
        this.out.println("     pa  -- Program Arguments follow in a space-separated list.  This");
        this.out.println("            option must be placed AFTER ALL FILE NAMES, because everything");
        this.out.println("            that follows it is interpreted as a program argument to be");
        this.out.println("            made available to the MIPS program at runtime.");
        this.out.println("If more than one filename is listed, the first is assumed to be the main");
        this.out.println("unless the global statement label 'main' is defined in one of the files.");
        this.out.println("Exception handler not automatically assembled.  Add it to the file list.");
        this.out.println("Options used here do not affect MARS Settings menu values and vice versa.");
    }
}
