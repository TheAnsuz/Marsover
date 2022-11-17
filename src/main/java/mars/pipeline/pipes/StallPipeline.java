

package mars.pipeline.pipes;

import mars.pipeline.Decode;
import mars.pipeline.DiagramaMulticiclo.InstructionInfo;
import mars.pipeline.StageRegisters;
import mars.pipeline.BranchPredictor;
import mars.pipeline.Stage;

public class StallPipeline extends StaticPipe
{
    private boolean emitiendoIncorrectas;
    
    public StallPipeline(final Stage st, final BranchPredictor.BranchPredictor_type bp) {
        super(5, st, bp);
        this.emitiendoIncorrectas = false;
    }
    
    @Override
    public void UpdatePipeline(final int address, final int instruction) {
        this.updateStageRegs(address, instruction, false);
    }
    
    private boolean check_misprediction() {
        if (this.stage_regs[this.BranchResolve.ordinal()].isBranch() && this.stage_regs[this.BranchResolve.ordinal()].getPrediccion() != this.stage_regs[this.BranchResolve.ordinal()].getResultadoSalto()) {
            final StageRegisters reg = this.stage_regs[this.BranchResolve.ordinal()];
            final int mal_lanzadas = this.BranchResolve.ordinal();
            this.branch_stalls += mal_lanzadas;
            this.emitiendoIncorrectas = false;
            for (int i = 0; i < mal_lanzadas; ++i) {
                if (!this.stage_regs[i].isBubble()) {
                    --this.inst_onflight;
                    this.stage_regs[i].getIns_info().setDiscard(this.cycle);
                    this.stage_regs[i].markAsBubble();
                }
            }
            return true;
        }
        return false;
    }
    
    private void updateStageRegs(final int address, final int instruction, final boolean end) {
        final int MemRd = this.stage_regs[3].getRdValue();
        final int ExRd = this.stage_regs[2].getRdValue();
        final int Rs = this.stage_regs[1].getRsValue();
        final int Rt = this.stage_regs[1].getRtValue();
        final boolean stop = (MemRd != 0 && (MemRd == Rs || MemRd == Rt)) || (ExRd != 0 && (ExRd == Rs || ExRd == Rt));
        if (this.emitiendoIncorrectas && (this.BranchResolve != Stage.ID || !stop) && this.check_misprediction()) {
            return;
        }
        ++this.cycle;
        if (stop && !this.stage_regs[1].isMissSpeculated()) {
            ++this.other_stalls;
        }
        if (!this.stage_regs[4].isBubble()) {
            --this.inst_onflight;
            ++this.instruccionesConfirmadas;
            this.stage_regs[4].getIns_info().finaliza(this.cycle);
        }
        this.stage_regs[3].copyTo(this.stage_regs[4]);
        if (!this.stage_regs[4].isBubble()) {
            this.stage_regs[4].getIns_info().setWB(this.cycle);
        }
        this.stage_regs[2].copyTo(this.stage_regs[3]);
        if (!this.stage_regs[3].isBubble()) {
            this.stage_regs[3].getIns_info().setMEM(this.cycle);
        }
        if (!stop) {
            this.stage_regs[1].copyTo(this.stage_regs[2]);
            if (!this.stage_regs[2].isBubble()) {
                this.stage_regs[2].getIns_info().setEX(this.cycle);
            }
            this.stage_regs[0].copyTo(this.stage_regs[1]);
            if (!this.stage_regs[1].isBubble()) {
                this.stage_regs[1].getIns_info().setIssue(this.cycle);
            }
            if (!end) {
                this.stage_regs[0].update(address, instruction, this.bp);
                final InstructionInfo info = new InstructionInfo(instruction, this.cycle, false);
                this.stage_regs[0].setIns_info(info);
                this.diagrama.addInstruction(info);
                ++this.instrucciones;
                ++this.inst_onflight;
                this.diagrama.writeDiagrama(this.cycle);
                if (this.emitiendoIncorrectas) {
                    this.stage_regs[0].MarkAsMissSpeculated();
                    final int branch_address = address + 4;
                    final int branch_instruction = this.readAddres(branch_address);
                    this.updateStageRegs(branch_address, branch_instruction, end);
                }
                else if (Decode.isBranch(instruction)) {
                    if (this.stage_regs[0].getPrediccion() == this.stage_regs[0].getResultadoSalto()) {
                        ++this.aciertos_predictor;
                    }
                    else {
                        this.emitiendoIncorrectas = true;
                        ++this.fallos_predictor;
                        final int branch_address = this.stage_regs[0].getPrediccion() ? this.stage_regs[0].branch_address() : (address + 4);
                        final int branch_instruction = this.readAddres(branch_address);
                        this.updateStageRegs(branch_address, branch_instruction, end);
                    }
                }
            }
            else {
                this.stage_regs[0].unused();
                this.diagrama.writeDiagrama(this.cycle);
            }
        }
        else {
            this.stage_regs[2].markAsBubble();
            this.stage_regs[1].getIns_info().setIssue(this.cycle);
            this.stage_regs[0].getIns_info().setIF(this.cycle);
            this.diagrama.writeDiagrama(this.cycle);
            this.updateStageRegs(address, instruction, end);
        }
    }
    
    @Override
    public void finalizar() {
        while (this.inst_onflight != 0) {
            this.updateStageRegs(0, 0, true);
        }
    }
    
    @Override
    public String toString() {
        return "Pipeline: 5 etapas sin anticipaci\u00f3n.\nResoluci\u00f3n de saltos: " + this.BranchResolve + ". Predictor: " + this.bp;
    }
}
