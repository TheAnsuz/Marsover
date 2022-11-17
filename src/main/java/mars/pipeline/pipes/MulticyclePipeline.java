

package mars.pipeline.pipes;

import mars.pipeline.BranchPredictor;
import mars.pipeline.Decode;
import mars.pipeline.DiagramaMulticiclo.InstructionInfo;
import mars.pipeline.Stage;
import mars.pipeline.StageRegisters;

public class MulticyclePipeline extends StaticPipe
{
    private final int mult_lat;
    private final int div_lat;
    private boolean emitiendoIncorrectas;
    private final especialized_pipe mult_pipe;
    private final especialized_pipe div_pipe;
    
    public MulticyclePipeline(final Stage st, final BranchPredictor.BranchPredictor_type bp, final boolean mult_seg, final int mult_lat, final boolean div_seg, final int div_lat) {
        super(5, st, bp);
        this.emitiendoIncorrectas = false;
        this.mult_lat = mult_lat;
        this.div_lat = div_lat;
        this.mult_pipe = (mult_seg ? new seg_especialized_pipe(mult_lat) : new nonseg_especialized_pipe(mult_lat));
        this.div_pipe = (div_seg ? new seg_especialized_pipe(div_lat) : new nonseg_especialized_pipe(div_lat));
        this.inst_onflight = 0;
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
                    this.stage_regs[i].getIns_info().setDiscard(this.cycle);
                    this.stage_regs[i].markAsBubble();
                    --this.inst_onflight;
                }
            }
            if (mal_lanzadas == 3) {
                this.div_pipe.brach_failed_on_MEM();
                this.mult_pipe.brach_failed_on_MEM();
            }
            return true;
        }
        return false;
    }
    
    private void updateStageRegs(final int address, final int instruction, final boolean end) {
        boolean stop = false;
        final int Rs = this.stage_regs[1].getRsValue();
        final int Rt = this.stage_regs[1].getRtValue();
        final int Rd = this.stage_regs[1].getRdValue();
        final int ID_instruction = this.stage_regs[1].getInstruction();
        final boolean is_sw = Decode.isStore(ID_instruction);
        final int ExRd = this.stage_regs[2].getRdValue();
        if (Decode.isLoad(this.stage_regs[2].getInstruction()) && ExRd != 0 && (ExRd == Rs || ExRd == Rt)) {
            stop = true;
        }
        if (!stop && (this.mult_pipe.check_RAW_risk_Rs(Rs) || this.mult_pipe.check_RAW_risk_Rt(Rt, is_sw) || this.div_pipe.check_RAW_risk_Rs(Rs) || this.div_pipe.check_RAW_risk_Rt(Rt, is_sw))) {
            stop = true;
        }
        if (Decode.isBranch(this.stage_regs[1].getInstruction()) && this.BranchResolve == Stage.ID) {
            if (!stop && ExRd != 0 && (ExRd == Rs || ExRd == Rt)) {
                stop = true;
            }
            final int MemRd = Decode.isLoad(this.stage_regs[3].getInstruction()) ? this.stage_regs[3].getRdValue() : 0;
            if (!stop && MemRd != 0 && (MemRd == Rs || MemRd == Rt)) {
                stop = true;
            }
            if (!stop && (this.mult_pipe.check_RAW_risk_ID_branch(Rs) || this.mult_pipe.check_RAW_risk_ID_branch(Rt) || this.div_pipe.check_RAW_risk_ID_branch(Rs) || this.div_pipe.check_RAW_risk_ID_branch(Rt))) {
                stop = true;
            }
        }
        if (!stop) {
            int new_WB = 0;
            if (Decode.isDiv(ID_instruction)) {
                new_WB = this.cycle + 1 + this.div_lat;
            }
            else if (Decode.isMult(ID_instruction)) {
                new_WB = this.cycle + 1 + this.mult_lat;
            }
            else if (!Decode.isStore(ID_instruction) && !Decode.isBranch(ID_instruction)) {
                new_WB = this.cycle + 3;
            }
            if (new_WB != 0 && (this.mult_pipe.check_WAW_and_WB_risk(new_WB, Rd) || this.div_pipe.check_WAW_and_WB_risk(new_WB, Rd))) {
                stop = true;
            }
        }
        if (this.emitiendoIncorrectas && (this.BranchResolve != Stage.ID || !stop) && this.check_misprediction()) {
            return;
        }
        ++this.cycle;
        if (!this.stage_regs[4].isBubble()) {
            ++this.instruccionesConfirmadas;
            --this.inst_onflight;
            this.stage_regs[4].getIns_info().finaliza(this.cycle);
            this.stage_regs[4].markAsBubble();
        }
        if (!this.stage_regs[3].isBubble()) {
            final int MEM_instruction = this.stage_regs[3].getInstruction();
            if (Decode.isStore(MEM_instruction) || Decode.isBranch(MEM_instruction)) {
                this.stage_regs[3].getIns_info().finaliza(this.cycle);
                this.stage_regs[3].markAsBubble();
                --this.inst_onflight;
            }
            else {
                this.stage_regs[3].copyTo(this.stage_regs[4]);
                this.stage_regs[4].getIns_info().setWB(this.cycle);
            }
        }
        if (this.mult_pipe.ready2WB()) {
            final StageRegisters reg = this.mult_pipe.extractReg4WB();
            reg.copyTo(this.stage_regs[4]);
            this.stage_regs[4].getIns_info().setWB(this.cycle);
        }
        if (this.div_pipe.ready2WB()) {
            final StageRegisters reg = this.div_pipe.extractReg4WB();
            reg.copyTo(this.stage_regs[4]);
            this.stage_regs[4].getIns_info().setWB(this.cycle);
        }
        this.mult_pipe.run();
        this.div_pipe.run();
        this.stage_regs[2].copyTo(this.stage_regs[3]);
        if (!this.stage_regs[3].isBubble()) {
            this.stage_regs[3].getIns_info().setMEM(this.cycle);
        }
        if ((Decode.isMult(ID_instruction) && !this.mult_pipe.CanExecuteNewInstruction()) || (Decode.isDiv(ID_instruction) && !this.div_pipe.CanExecuteNewInstruction())) {
            stop = true;
        }
        if (stop && !this.stage_regs[1].isMissSpeculated()) {
            ++this.other_stalls;
        }
        if (!stop) {
            if (Decode.isMult(ID_instruction)) {
                this.mult_pipe.addInst(this.cycle, this.stage_regs[1]);
                this.stage_regs[2].markAsBubble();
            }
            else if (Decode.isDiv(ID_instruction)) {
                this.div_pipe.addInst(this.cycle, this.stage_regs[1]);
                this.stage_regs[2].markAsBubble();
            }
            else {
                this.stage_regs[1].copyTo(this.stage_regs[2]);
                if (!this.stage_regs[2].isBubble()) {
                    this.stage_regs[2].getIns_info().setEX(this.cycle);
                }
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
                this.diagrama.writeDiagrama(this.cycle);
                ++this.inst_onflight;
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
        return "Pipeline: 5 etapas con anticipaci\u00f3n completa y\nunidades multiciclo de multiplicaci\u00f3n y divisi\u00f3n.\nResoluci\u00f3n de saltos: " + this.BranchResolve + ". Predictor: " + this.bp + "\nMultiplicador segmentando: " + ((this.mult_pipe instanceof seg_especialized_pipe) ? "si" : "no") + ". Latencia del multiplicador: " + this.mult_lat + " ciclos\nDivisor segmentando: " + ((this.div_pipe instanceof seg_especialized_pipe) ? "si" : "no") + ". Latencia del divisor: " + this.div_lat + " ciclos";
    }
    
    abstract class especialized_pipe
    {
        boolean enUso;
        int latencia;
        
        public especialized_pipe(final int latencia) {
            this.latencia = latencia;
            this.enUso = false;
        }
        
        public abstract boolean check_RAW_risk_Rs(final int p0);
        
        public abstract boolean check_RAW_risk_Rt(final int p0, final boolean p1);
        
        public abstract boolean check_RAW_risk_ID_branch(final int p0);
        
        public abstract boolean check_WAW_and_WB_risk(final int p0, final int p1);
        
        public abstract boolean CanExecuteNewInstruction();
        
        public abstract void run();
        
        public abstract void addInst(final int p0, final StageRegisters p1);
        
        public abstract boolean ready2WB();
        
        public abstract StageRegisters extractReg4WB();
        
        public abstract void brach_failed_on_MEM();
    }
    
    class seg_especialized_pipe extends especialized_pipe
    {
        StageRegisters[] pipe;
        int[] timestamps;
        int used_stages;
        
        public seg_especialized_pipe(final int latencia) {
            super(latencia);
            this.timestamps = new int[latencia];
            this.pipe = new StageRegisters[latencia];
            this.used_stages = 0;
            for (int i = 0; i < latencia; ++i) {
                this.pipe[i] = new StageRegisters();
            }
        }
        
        @Override
        public boolean check_RAW_risk_Rs(final int register) {
            if (!this.enUso) {
                return false;
            }
            if (register == 32 || register == 33) {
                return true;
            }
            for (int regID = 0; regID < this.latencia - 1; ++regID) {
                final StageRegisters reg = this.pipe[regID];
                final int Rd = reg.getRdValue();
                if (Rd != 0 && register == Rd) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public boolean check_RAW_risk_Rt(final int register, final boolean is_sw) {
            if (!this.enUso) {
                return false;
            }
            if (register == 32 || register == 33) {
                return true;
            }
            for (int regID = 0; regID < this.latencia - 2; ++regID) {
                final StageRegisters reg = this.pipe[regID];
                final int Rd = reg.getRdValue();
                if (Rd != 0 && register == Rd) {
                    return true;
                }
            }
            final int Rd2 = this.pipe[this.latencia - 2].getRdValue();
            return Rd2 != 0 && register == Rd2 && !is_sw;
        }
        
        @Override
        public boolean check_RAW_risk_ID_branch(final int register) {
            if (!this.enUso) {
                return false;
            }
            for (int regID = 0; regID < this.latencia; ++regID) {
                final StageRegisters reg = this.pipe[regID];
                final int Rd = reg.getRdValue();
                if (Rd != 0 && register == Rd) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public boolean check_WAW_and_WB_risk(final int new_write_cycle, final int register) {
            if (!this.enUso) {
                return false;
            }
            for (int regID = this.latencia - 1; regID >= 0; --regID) {
                final StageRegisters reg = this.pipe[regID];
                final int Rd = reg.getRdValue();
                if (register != 0) {
                    final int previous_write = this.timestamps[regID] + this.latencia;
                    if (new_write_cycle == previous_write) {
                        return true;
                    }
                    if (Rd == register && new_write_cycle < previous_write) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        @Override
        public boolean CanExecuteNewInstruction() {
            return true;
        }
        
        @Override
        public void run() {
            if (this.enUso) {
                for (int dst_reg = this.latencia - 1; dst_reg > 0; --dst_reg) {
                    final int src_reg = dst_reg - 1;
                    this.pipe[src_reg].copyTo(this.pipe[dst_reg]);
                    this.timestamps[dst_reg] = this.timestamps[src_reg];
                    if (!this.pipe[dst_reg].isBubble()) {
                        this.pipe[dst_reg].getIns_info().setEX(MulticyclePipeline.this.cycle);
                    }
                }
                this.pipe[0].markAsBubble();
                this.timestamps[0] = Integer.MAX_VALUE;
            }
        }
        
        @Override
        public void addInst(final int cycle, final StageRegisters reg) {
            reg.copyTo(this.pipe[0]);
            this.pipe[0].getIns_info().setEX(cycle);
            this.timestamps[0] = cycle;
            ++this.used_stages;
            this.enUso = true;
        }
        
        @Override
        public boolean ready2WB() {
            return this.enUso && !this.pipe[this.latencia - 1].isBubble();
        }
        
        @Override
        public StageRegisters extractReg4WB() {
            if (!this.enUso || this.pipe[this.latencia - 1].isBubble()) {
                return null;
            }
            final StageRegisters reg = new StageRegisters();
            this.pipe[this.latencia - 1].copyTo(reg);
            this.pipe[this.latencia - 1].markAsBubble();
            this.timestamps[this.latencia - 1] = Integer.MAX_VALUE;
            --this.used_stages;
            this.enUso = (this.used_stages != 0);
            return reg;
        }
        
        @Override
        public void brach_failed_on_MEM() {
            if (!this.pipe[0].isBubble()) {
                this.pipe[0].getIns_info().setDiscard(MulticyclePipeline.this.cycle);
                this.pipe[0].markAsBubble();
                this.timestamps[0] = Integer.MAX_VALUE;
                --this.used_stages;
                this.enUso = (this.used_stages != 0);
            }
        }
    }
    
    class nonseg_especialized_pipe extends especialized_pipe
    {
        StageRegisters pipe;
        int estado;
        int WB_time;
        
        public nonseg_especialized_pipe(final int latencia) {
            super(latencia);
            this.estado = 0;
            this.WB_time = Integer.MAX_VALUE;
            this.pipe = new StageRegisters();
        }
        
        @Override
        public boolean check_RAW_risk_Rs(final int register) {
            if (!this.enUso || this.estado == this.latencia) {
                return false;
            }
            if (register == 32 || register == 33) {
                return true;
            }
            final int Rd = this.pipe.getRdValue();
            return Rd != 0 && register == Rd;
        }
        
        @Override
        public boolean check_RAW_risk_Rt(final int register, final boolean is_sw) {
            if (!this.enUso || this.estado == this.latencia || (is_sw && this.estado == this.latencia - 1)) {
                return false;
            }
            if (register == 32 || register == 33) {
                return true;
            }
            final int Rd = this.pipe.getRdValue();
            return Rd != 0 && register == Rd;
        }
        
        @Override
        public boolean check_RAW_risk_ID_branch(final int register) {
            if (!this.enUso || this.estado == this.latencia) {
                return false;
            }
            final int Rd = this.pipe.getRdValue();
            return Rd != 0 && register == Rd;
        }
        
        @Override
        public boolean check_WAW_and_WB_risk(final int new_write_cycle, final int register) {
            if (!this.enUso) {
                return false;
            }
            final int Rd = this.pipe.getRdValue();
            if (register != 0) {
                if (new_write_cycle == this.WB_time) {
                    return true;
                }
                if (Rd == register && new_write_cycle < this.WB_time) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public boolean CanExecuteNewInstruction() {
            return !this.enUso;
        }
        
        @Override
        public void run() {
            if (this.enUso && this.estado < this.latencia) {
                ++this.estado;
                this.pipe.getIns_info().setEX(MulticyclePipeline.this.cycle);
            }
        }
        
        @Override
        public void addInst(final int cycle, final StageRegisters reg) {
            if (!this.enUso) {
                reg.copyTo(this.pipe);
                this.pipe.getIns_info().setEX(cycle);
                this.WB_time = cycle + this.latencia;
                this.estado = 1;
                this.enUso = true;
            }
        }
        
        @Override
        public boolean ready2WB() {
            return this.enUso && this.estado == this.latencia;
        }
        
        @Override
        public StageRegisters extractReg4WB() {
            if (this.enUso && this.estado == this.latencia) {
                final StageRegisters reg = new StageRegisters();
                this.pipe.copyTo(reg);
                this.pipe.markAsBubble();
                this.enUso = false;
                return reg;
            }
            return null;
        }
        
        @Override
        public void brach_failed_on_MEM() {
            if (this.enUso && this.estado == 1) {
                this.pipe.getIns_info().setDiscard(MulticyclePipeline.this.cycle);
                this.pipe.markAsBubble();
                this.enUso = false;
            }
        }
    }
}
