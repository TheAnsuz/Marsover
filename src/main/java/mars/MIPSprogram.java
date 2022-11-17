

package mars;

import mars.mips.hardware.RegisterFile;
import mars.simulator.Simulator;
import javax.swing.AbstractAction;
import mars.assembler.Assembler;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;
import mars.assembler.Tokenizer;
import mars.assembler.SourceLine;
import mars.assembler.MacroPool;
import mars.assembler.SymbolTable;
import mars.simulator.BackStepper;
import java.util.ArrayList;

public class MIPSprogram
{
    private boolean steppedExecution;
    private String filename;
    private ArrayList sourceList;
    private ArrayList tokenList;
    private ArrayList parsedList;
    private ArrayList machineList;
    private BackStepper backStepper;
    private SymbolTable localSymbolTable;
    private MacroPool macroPool;
    private ArrayList<SourceLine> sourceLineList;
    private Tokenizer tokenizer;
    
    public MIPSprogram() {
        this.steppedExecution = false;
    }
    
    public ArrayList getSourceList() {
        return this.sourceList;
    }
    
    public void setSourceLineList(final ArrayList<SourceLine> sourceLineList) {
        this.sourceLineList = sourceLineList;
        this.sourceList = new ArrayList();
        for (final SourceLine sl : sourceLineList) {
            this.sourceList.add(sl.getSource());
        }
    }
    
    public ArrayList<SourceLine> getSourceLineList() {
        return this.sourceLineList;
    }
    
    public String getFilename() {
        return this.filename;
    }
    
    public ArrayList getTokenList() {
        return this.tokenList;
    }
    
    public Tokenizer getTokenizer() {
        return this.tokenizer;
    }
    
    public ArrayList createParsedList() {
        return this.parsedList = new ArrayList();
    }
    
    public ArrayList getParsedList() {
        return this.parsedList;
    }
    
    public ArrayList getMachineList() {
        return this.machineList;
    }
    
    public BackStepper getBackStepper() {
        return this.backStepper;
    }
    
    public SymbolTable getLocalSymbolTable() {
        return this.localSymbolTable;
    }
    
    public boolean backSteppingEnabled() {
        return this.backStepper != null && this.backStepper.enabled();
    }
    
    public String getSourceLine(final int i) {
        if (i >= 1 && i <= this.sourceList.size()) {
            return this.sourceList.get(i - 1);
        }
        return null;
    }
    
    public void readSource(final String file) throws ProcessingException {
        this.filename = file;
        this.sourceList = new ArrayList();
        ErrorList errors = null;
        final int lengthSoFar = 0;
        try {
            final BufferedReader inputFile = new BufferedReader(new FileReader(file));
            for (String line = inputFile.readLine(); line != null; line = inputFile.readLine()) {
                this.sourceList.add(line);
            }
        }
        catch (Exception e) {
            errors = new ErrorList();
            errors.add(new ErrorMessage((MIPSprogram)null, 0, 0, e.toString()));
            throw new ProcessingException(errors);
        }
    }
    
    public void tokenize() throws ProcessingException {
        this.tokenizer = new Tokenizer();
        this.tokenList = this.tokenizer.tokenize(this);
        this.localSymbolTable = new SymbolTable(this.filename);
    }
    
    public ArrayList prepareFilesForAssembly(final ArrayList filenames, final String leadFilename, final String exceptionHandler) throws ProcessingException {
        final ArrayList MIPSprogramsToAssemble = new ArrayList();
        int leadFilePosition = 0;
        if (exceptionHandler != null && exceptionHandler.length() > 0) {
            filenames.add(0, exceptionHandler);
            leadFilePosition = 1;
        }
        for (int i = 0; i < filenames.size(); ++i) {
            final String filename = filenames.get(i);
            final MIPSprogram preparee = filename.equals(leadFilename) ? this : new MIPSprogram();
            preparee.readSource(filename);
            preparee.tokenize();
            if (preparee == this && MIPSprogramsToAssemble.size() > 0) {
                MIPSprogramsToAssemble.add(leadFilePosition, preparee);
            }
            else {
                MIPSprogramsToAssemble.add(preparee);
            }
        }
        return MIPSprogramsToAssemble;
    }
    
    public ErrorList assemble(final ArrayList MIPSprogramsToAssemble, final boolean extendedAssemblerEnabled) throws ProcessingException {
        return this.assemble(MIPSprogramsToAssemble, extendedAssemblerEnabled, false);
    }
    
    public ErrorList assemble(final ArrayList MIPSprogramsToAssemble, final boolean extendedAssemblerEnabled, final boolean warningsAreErrors) throws ProcessingException {
        this.backStepper = null;
        final Assembler asm = new Assembler();
        this.machineList = asm.assemble(MIPSprogramsToAssemble, extendedAssemblerEnabled, warningsAreErrors);
        this.backStepper = new BackStepper();
        return asm.getErrorList();
    }
    
    public boolean simulate(final int[] breakPoints) throws ProcessingException {
        return this.simulateFromPC(breakPoints, -1, null);
    }
    
    public boolean simulate(final int maxSteps) throws ProcessingException {
        return this.simulateFromPC(null, maxSteps, null);
    }
    
    public boolean simulateFromPC(final int[] breakPoints, final int maxSteps, final AbstractAction a) throws ProcessingException {
        this.steppedExecution = false;
        final Simulator sim = Simulator.getInstance();
        return sim.simulate(this, RegisterFile.getProgramCounter(), maxSteps, breakPoints, a);
    }
    
    public boolean simulateStepAtPC(final AbstractAction a) throws ProcessingException {
        this.steppedExecution = true;
        final Simulator sim = Simulator.getInstance();
        final boolean done = sim.simulate(this, RegisterFile.getProgramCounter(), 1, null, a);
        return done;
    }
    
    public boolean inSteppedExecution() {
        return this.steppedExecution;
    }
    
    public MacroPool createMacroPool() {
        return this.macroPool = new MacroPool(this);
    }
    
    public MacroPool getLocalMacroPool() {
        return this.macroPool;
    }
    
    public void setLocalMacroPool(final MacroPool macroPool) {
        this.macroPool = macroPool;
    }
}
