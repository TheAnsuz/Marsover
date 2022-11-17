

package mars.pipeline.pipes;

import mars.pipeline.DiagramaMulticiclo.InstructionInfo;
import mars.pipeline.BranchPredictor;
import mars.pipeline.Stage;

public class IdealPipeline extends StaticPipe
{
    public IdealPipeline() {
        super(5, Stage.ID, BranchPredictor.BranchPredictor_type.ideal);
    }
    
    private void updateStageRegs(final int address, final int instruction, final boolean end) {
        ++this.cycle;
        if (!this.stage_regs[4].isUnused()) {
            this.stage_regs[4].getIns_info().finaliza(this.cycle);
            ++this.instruccionesConfirmadas;
        }
        this.stage_regs[3].copyTo(this.stage_regs[4]);
        if (!this.stage_regs[4].isUnused()) {
            this.stage_regs[4].getIns_info().setWB(this.cycle);
        }
        this.stage_regs[2].copyTo(this.stage_regs[3]);
        if (!this.stage_regs[3].isUnused()) {
            this.stage_regs[3].getIns_info().setMEM(this.cycle);
        }
        this.stage_regs[1].copyTo(this.stage_regs[2]);
        if (!this.stage_regs[2].isUnused()) {
            this.stage_regs[2].getIns_info().setEX(this.cycle);
        }
        this.stage_regs[0].copyTo(this.stage_regs[1]);
        if (!this.stage_regs[1].isUnused()) {
            this.stage_regs[1].getIns_info().setIssue(this.cycle);
        }
        if (!end) {
            this.stage_regs[0].update(address, instruction, this.bp);
            final InstructionInfo info = new InstructionInfo(instruction, this.cycle, false);
            this.stage_regs[0].setIns_info(info);
            this.diagrama.addInstruction(info);
            ++this.instrucciones;
            if (this.stage_regs[0].isBranch()) {
                ++this.aciertos_predictor;
            }
        }
        else {
            this.stage_regs[0].unused();
        }
        this.diagrama.writeDiagrama(this.cycle);
    }
    
    @Override
    public void UpdatePipeline(final int address, final int instruction) {
        this.updateStageRegs(address, instruction, false);
    }
    
    @Override
    public void finalizar() {
        while (!this.stage_regs[4].isUnused()) {
            this.updateStageRegs(0, 0, true);
        }
    }
    
    @Override
    public String toString() {
        return "Pipeline ideal (sin riesgos de control o datos)";
    }
}
