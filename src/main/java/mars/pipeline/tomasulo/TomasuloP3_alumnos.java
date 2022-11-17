

package mars.pipeline.tomasulo;

import java.util.logging.Level;
import java.util.logging.Logger;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Memory;
import mars.pipeline.BranchPredictor;
import mars.pipeline.Decode;
import mars.pipeline.DiagramaMulticiclo.InstructionInfo;
import mars.pipeline.StageRegisters;

public class TomasuloP3_alumnos extends Tomasulo_alumnos
{
    private boolean emitiendoIncorrectas;
    private int ultimaInstucci\u00f3nIncorrecta;
    
    public TomasuloP3_alumnos(final BranchPredictor.BranchPredictor_type bp) {
        super(bp);
        this.emitiendoIncorrectas = false;
        Tomasulo_conf.PRACTICA = 3;
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
            final boolean prediccion = this.bp.getPrediction(instruction);
            final boolean resultado = this.bp.is_branch_taken(instruction);
            if_reg.setPrediccion(prediccion);
            if_reg.setResultadoSalto(resultado);
            if (prediccion != resultado) {
                this.emitiendoIncorrectas = true;
                if (prediccion) {
                    this.ultimaInstucci\u00f3nIncorrecta = if_reg.branch_address();
                }
                else {
                    this.ultimaInstucci\u00f3nIncorrecta = if_reg.getAddress() + 4;
                }
            }
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
            this.est_store[estacion].Libera();
        }
    }
    
    @Override
    protected void WB() {
    }
    
    @Override
    protected void Commit() {
        final RoB_entry head = null;
        final InstructionInfo.Instruction_type type = head.getIns_info().getTipo();
        ++this.instruccionesConfirmadas;
        this.emitiendoIncorrectas = false;
        this.stage_regs[0].getIns_info().setDiscard(this.cycle);
        this.stage_regs[1].getIns_info().setDiscard(this.cycle);
        head.setStage("CO");
        head.getIns_info().setCommit(this.cycle);
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
        final boolean retornoIssue = this.Issue();
        this.writeStatus();
        ++this.cycle;
        if (this.emitiendoIncorrectas && retornoIssue) {
            final StageRegisters rIF = new StageRegisters();
            rIF.setAddress(this.ultimaInstucci\u00f3nIncorrecta);
            final Memory mem = Memory.getInstance();
            int instruction = 0;
            try {
                instruction = mem.getWordNoNotify(this.ultimaInstucci\u00f3nIncorrecta);
                rIF.setInstruction(instruction);
            }
            catch (AddressErrorException ex) {
                Logger.getLogger(TomasuloP3_alumnos.class.getName()).log(Level.SEVERE, null, ex);
            }
            rIF.setIns_info(new InstructionInfo(rIF.getInstruction(), this.cycle));
            this.ultimaInstucci\u00f3nIncorrecta += 4;
            this.IF(rIF);
            return false;
        }
        return retornoIssue;
    }
    
    private boolean check_estacionSW() {
        for (final Estacion estacion : this.est_store) {
            if (estacion.busy) {
                return true;
            }
        }
        return false;
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
        while (this.rob.getEntradasOcupadas() != 0 || this.check_estacionSW()) {
            this.Commit();
            this.WB();
            this.Execute();
            this.writeStatus();
            ++this.cycle;
        }
    }
    
    @Override
    public String toString() {
        return "Tomasulo con especulaci\u00f3n (P3-alumnos). Predictor: " + this.bp + "\n" + super.toString();
    }
}
