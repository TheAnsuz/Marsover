

package mars.pipeline.tomasulo;

import mars.ProgramStatement;
import mars.mips.hardware.AddressErrorException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mars.mips.hardware.Memory;
import mars.pipeline.Decode;
import mars.pipeline.DiagramaMulticiclo.InstructionInfo;
import mars.pipeline.StageRegisters;
import mars.pipeline.BranchPredictor;

public class TomasuloP4_alumnos extends Tomasulo_alumnos
{
    private boolean emitiendoIncorrectas;
    private int ultimaInstucci\u00f3nIncorrecta;
    private BTB btb;
    
    public TomasuloP4_alumnos() {
        super(BranchPredictor.BranchPredictor_type.ideal);
        this.emitiendoIncorrectas = false;
        Tomasulo_conf.PRACTICA = 4;
        this.btb = new BTB();
    }
    
    @Override
    public void IF(final StageRegisters next_if) {
        final StageRegisters if_reg = this.stage_regs[0];
        if_reg.copyTo(this.stage_regs[1]);
        next_if.copyTo(this.stage_regs[0]);
        final int instruction = if_reg.getInstruction();
        final int address = if_reg.getAddress();
        final InstructionInfo info = new InstructionInfo(instruction, this.cycle);
        if (Decode.isBranch(instruction)) {
            ++this.instrucciones;
            ++this.saltos;
            final int direccionSalto = this.btb.getPrediction(address);
            final boolean resultado = this.bp.is_branch_taken(instruction);
            final boolean pred = direccionSalto != address + 4;
            if_reg.setPrediccion(pred);
            if_reg.setResultadoSalto(resultado);
            if (pred != resultado) {
                this.emitiendoIncorrectas = true;
                this.ultimaInstucci\u00f3nIncorrecta = direccionSalto;
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
        if (head.isBranch()) {
            if (head.resultadoPrediccion() != head.resultadoSalto()) {
                ++this.fallos_predictor;
            }
            else {
                ++this.aciertos_predictor;
            }
            this.btb.writeBTB(this.cycle);
            this.btb.Actualizar(head.getAddress(), head.getInstruction(), head.resultadoSalto());
        }
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
            final ProgramStatement statement = null;
            int instruction = 0;
            try {
                instruction = mem.getWordNoNotify(this.ultimaInstucci\u00f3nIncorrecta);
                rIF.setInstruction(instruction);
            }
            catch (AddressErrorException ex) {
                Logger.getLogger(TomasuloP4_alumnos.class.getName()).log(Level.SEVERE, null, ex);
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
        this.btb.writeBTB(this.cycle - 1);
        this.btb.writeBTB(this.cycle - 1);
    }
    
    @Override
    public String toString() {
        return "Tomasulo con especulaci\u00f3n (P4-alumnos). Predictor: BTB de " + Tomasulo_conf.NUM_BTB_ENTRIES + " entradas\n" + super.toString();
    }
}
