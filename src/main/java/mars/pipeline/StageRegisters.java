

package mars.pipeline;

import mars.pipeline.DiagramaMulticiclo.InstructionInfo;

public class StageRegisters
{
    private int address;
    private int instruction;
    private int RsValue;
    private int RtValue;
    private int RdValue;
    private boolean isBranch;
    private boolean prediccion;
    private boolean resultadoSalto;
    private InstructionInfo ins_info;
    private boolean unused;
    private boolean bubble;
    private boolean missSpeculated;
    
    public boolean getPrediccion() {
        return this.prediccion;
    }
    
    public void setPrediccion(final boolean prediccion) {
        this.prediccion = prediccion;
    }
    
    public boolean getResultadoSalto() {
        return this.resultadoSalto;
    }
    
    public void setResultadoSalto(final boolean resultadoSalto) {
        this.resultadoSalto = resultadoSalto;
    }
    
    public InstructionInfo getIns_info() {
        return this.ins_info;
    }
    
    public void setIns_info(final InstructionInfo ins_info) {
        this.ins_info = ins_info;
    }
    
    public StageRegisters() {
        this.unused = true;
        this.address = 0;
        this.instruction = 0;
        this.RsValue = 0;
        this.RtValue = 0;
        this.RdValue = 0;
        this.prediccion = false;
        this.resultadoSalto = false;
        this.bubble = true;
        this.isBranch = false;
        this.missSpeculated = false;
        this.ins_info = null;
    }
    
    public int getAddress() {
        return this.address;
    }
    
    public int getInstruction() {
        return this.instruction;
    }
    
    public int getRsValue() {
        return this.RsValue;
    }
    
    public int getRtValue() {
        return this.RtValue;
    }
    
    public int getRdValue() {
        return this.RdValue;
    }
    
    public void setAddress(final int address) {
        this.address = address;
    }
    
    public void setInstruction(final int instruction) {
        this.instruction = instruction;
    }
    
    public void setRsValue(final int RsValue) {
        this.RsValue = RsValue;
    }
    
    public void setRtValue(final int RtValue) {
        this.RtValue = RtValue;
    }
    
    public void setRdValue(final int RdValue) {
        this.RdValue = RdValue;
    }
    
    public void copyTo(final StageRegisters dest) {
        dest.address = this.address;
        dest.instruction = this.instruction;
        dest.RsValue = this.RsValue;
        dest.RtValue = this.RtValue;
        dest.RdValue = this.RdValue;
        dest.ins_info = this.ins_info;
        dest.prediccion = this.prediccion;
        dest.resultadoSalto = this.resultadoSalto;
        dest.unused = this.unused;
        dest.bubble = this.bubble;
        dest.isBranch = this.isBranch;
        dest.missSpeculated = this.missSpeculated;
    }
    
    public void update(final int address, final int instruction, final BranchPredictor bp) {
        this.instruction = instruction;
        this.address = address;
        this.RsValue = Decode.getRs(instruction);
        this.RtValue = Decode.getRt(instruction);
        this.RdValue = Decode.getDestination(instruction);
        if (Decode.isBranch(instruction)) {
            this.resultadoSalto = bp.is_branch_taken(instruction);
            this.prediccion = bp.getPrediction(instruction);
            this.isBranch = true;
        }
        else {
            this.isBranch = false;
        }
        this.unused = false;
        this.bubble = false;
        this.missSpeculated = false;
    }
    
    public void MarkAsMissSpeculated() {
        this.missSpeculated = true;
    }
    
    public boolean isMissSpeculated() {
        return this.missSpeculated;
    }
    
    public void unused() {
        this.unused = true;
        this.markAsBubble();
    }
    
    public boolean isUnused() {
        return this.unused;
    }
    
    public void markAsBubble() {
        this.address = 0;
        this.instruction = 0;
        this.RsValue = 0;
        this.RtValue = 0;
        this.RdValue = 0;
        this.prediccion = false;
        this.resultadoSalto = false;
        this.ins_info = null;
        this.bubble = true;
        this.isBranch = false;
        this.missSpeculated = false;
    }
    
    public boolean isBubble() {
        return this.bubble;
    }
    
    public int branch_address() {
        final int inm = Decode.getInm_ExtensionSigno(this.instruction) << 2;
        return this.address + 4 + inm;
    }
    
    public boolean isBranch() {
        return this.isBranch;
    }
}
