

package mars.pipeline.tomasulo;

import mars.pipeline.Decode;
import mars.pipeline.DiagramaMulticiclo.InstructionInfo;
import mars.pipeline.StageRegisters;
import mars.pipeline.BranchPredictor;

public class TomasuloP2_alumnos extends Tomasulo_alumnos
{
    public TomasuloP2_alumnos() {
        super(BranchPredictor.BranchPredictor_type.ideal);
        Tomasulo_conf.PRACTICA = 2;
    }
    
    @Override
    public int getNumInstruccionesConfirmadas() {
        return this.instrucciones;
    }
    
    @Override
    public void IF(final StageRegisters next_if) {
        final StageRegisters if_reg = this.stage_regs[0];
        if_reg.copyTo(this.stage_regs[1]);
        next_if.copyTo(this.stage_regs[0]);
        final int instruction = if_reg.getInstruction();
        final InstructionInfo info = new InstructionInfo(instruction, this.cycle);
        if (Decode.isBranch(instruction)) {
            ++this.instrucciones;
            ++this.saltos;
            ++this.aciertos_predictor;
            if_reg.setPrediccion(this.bp.getPrediction(instruction));
            if_reg.setResultadoSalto(this.bp.is_branch_taken(instruction));
        }
        else {
            if_reg.setPrediccion(false);
            if_reg.setResultadoSalto(false);
            if (!this.no_mas_instrucciones) {
                ++this.instrucciones;
                switch (info.getTipo()) {
                    case enteros: {
                        ++this.enteros;
                        break;
                    }
                    case pflotante: {
                        ++this.pflotante;
                        break;
                    }
                    case carga: {
                        ++this.loads;
                        break;
                    }
                    case almacenamiento: {
                        ++this.stores;
                        break;
                    }
                }
            }
        }
        if (!this.no_mas_instrucciones) {
            this.diagrama.addInstruction(info);
            if_reg.setIns_info(info);
        }
    }
    
    @Override
    protected boolean Issue() {
        final StageRegisters registroIssue = this.stage_regs[1];
        if (this.stage_regs[1].getInstruction() == 0) {
            return true;
        }
        final InstructionInfo.Instruction_type type = registroIssue.getIns_info().getTipo();
        final Estacion estacion = null;
        registroIssue.getIns_info().setIssue(this.cycle);
        estacion.ins_info = registroIssue.getIns_info();
        estacion.resultado = registroIssue.getRdValue();
        return true;
    }
    
    @Override
    protected void Execute() {
        int estacion = this.op_enteros.obtenerEstacion(this.cycle);
        if (estacion != -1 && this.est_enteros[estacion].Execute(this.cycle)) {
            this.op_enteros.LiberarOperador();
        }
        estacion = this.op_pf.obtenerEstacion(this.cycle);
        if (estacion != -1 && this.est_pf[estacion].Execute(this.cycle)) {
            this.op_pf.LiberarOperador();
        }
        estacion = this.op_load.obtenerEstacion(this.cycle);
        if (estacion != -1 && this.est_load[estacion].Execute(this.cycle)) {
            this.op_load.LiberarOperador();
        }
        estacion = this.op_store.obtenerEstacion(this.cycle);
        if (estacion != -1 && this.est_store[estacion].Execute(this.cycle)) {
            this.op_store.LiberarOperador();
            final int rob_id = this.est_store[estacion].rob_id;
            this.est_store[estacion].Libera();
            this.rob.getRoB_entry(rob_id).WBdone();
        }
    }
    
    @Override
    protected void WB() {
        final Estacion estacion = null;
        final RoB_entry entry = null;
        entry.setStage("WB");
        entry.getIns_info().setWB(this.cycle);
    }
    
    @Override
    protected void Commit() {
        this.rob.LimpiaRoB(this.cycle);
    }
    
    @Override
    public int getCycle() {
        return this.cycle - 1;
    }
    
    @Override
    protected boolean UpdatePipeline() {
        this.Commit();
        this.WB();
        this.Execute();
        final boolean retorno = this.Issue();
        this.writeStatus();
        ++this.cycle;
        return retorno;
    }
    
    @Override
    public void finalizar() {
        this.no_mas_instrucciones = true;
        final StageRegisters st = new StageRegisters();
        this.IF(st);
        boolean retorno = false;
        while (!retorno) {
            this.Commit();
            this.WB();
            this.Execute();
            retorno = this.Issue();
            this.writeStatus();
            ++this.cycle;
        }
        this.IF(st);
        while (this.rob.getEntradasOcupadas() != 0) {
            this.Commit();
            this.WB();
            this.Execute();
            this.writeStatus();
            ++this.cycle;
        }
    }
    
    @Override
    public String toString() {
        return "Tomasulo sin especulaci\u00f3n (P2-alumnos). Predicci\u00f3n ideal de saltos\n" + super.toString();
    }
}
