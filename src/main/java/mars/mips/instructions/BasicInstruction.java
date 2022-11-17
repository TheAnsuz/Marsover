

package mars.mips.instructions;

public class BasicInstruction extends Instruction
{
    private String instructionName;
    private BasicInstructionFormat instructionFormat;
    private String operationMask;
    private SimulationCode simulationCode;
    private int opcodeMask;
    private int opcodeMatch;
    
    public BasicInstruction(final String example, final String description, final BasicInstructionFormat instrFormat, final String operMask, final SimulationCode simCode) {
        this.exampleFormat = example;
        this.mnemonic = this.extractOperator(example);
        this.description = description;
        this.instructionFormat = instrFormat;
        this.operationMask = operMask.replaceAll(" ", "");
        if (this.operationMask.length() != 32) {
            System.out.println(example + " mask not " + 32 + " bits!");
        }
        this.simulationCode = simCode;
        this.opcodeMask = (int)Long.parseLong(this.operationMask.replaceAll("[01]", "1").replaceAll("[^01]", "0"), 2);
        this.opcodeMatch = (int)Long.parseLong(this.operationMask.replaceAll("[^1]", "0"), 2);
    }
    
    public BasicInstruction(final String example, final BasicInstructionFormat instrFormat, final String operMask, final SimulationCode simCode) {
        this(example, "", instrFormat, operMask, simCode);
    }
    
    public String getOperationMask() {
        return this.operationMask;
    }
    
    public BasicInstructionFormat getInstructionFormat() {
        return this.instructionFormat;
    }
    
    public SimulationCode getSimulationCode() {
        return this.simulationCode;
    }
    
    public int getOpcodeMask() {
        return this.opcodeMask;
    }
    
    public int getOpcodeMatch() {
        return this.opcodeMatch;
    }
}
