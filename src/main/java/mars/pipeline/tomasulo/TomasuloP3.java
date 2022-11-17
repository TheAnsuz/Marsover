

package mars.pipeline.tomasulo;

import java.util.logging.Level;
import java.util.logging.Logger;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Memory;
import mars.pipeline.BranchPredictor;
import mars.pipeline.Decode;
import mars.pipeline.DiagramaMulticiclo.InstructionInfo;
import mars.pipeline.StageRegisters;
import mars.pipeline.tomasulo.estaciones.EstacionPF;

public class TomasuloP3 extends Tomasulo_implementado
{
    private boolean emitiendoIncorrectas;
    private int ultimaInstucci\u00f3nIncorrecta;
    
    public TomasuloP3(final BranchPredictor.BranchPredictor_type bp) {
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
        final StageRegisters registroIssue = this.stage_regs[1];
        if (this.stage_regs[1].getInstruction() == 0) {
            return true;
        }
        final InstructionInfo.Instruction_type type = registroIssue.getIns_info().getTipo();
        Estacion estacion = null;
        int estaci\u00f3n_id = 0;
        switch (type) {
            case enteros: {
                for (estaci\u00f3n_id = 0; estaci\u00f3n_id < Tomasulo_conf.NUM_EST_ENTEROS; ++estaci\u00f3n_id) {
                    if (this.est_enteros[estaci\u00f3n_id].EstaLibre()) {
                        estacion = this.est_enteros[estaci\u00f3n_id];
                        break;
                    }
                }
                break;
            }
            case pflotante: {
                for (estaci\u00f3n_id = 0; estaci\u00f3n_id < Tomasulo_conf.NUM_EST_PF; ++estaci\u00f3n_id) {
                    if (this.est_pf[estaci\u00f3n_id].EstaLibre()) {
                        estacion = this.est_pf[estaci\u00f3n_id];
                        break;
                    }
                }
                break;
            }
            case carga: {
                for (estaci\u00f3n_id = 0; estaci\u00f3n_id < Tomasulo_conf.NUM_EST_LOAD; ++estaci\u00f3n_id) {
                    if (this.est_load[estaci\u00f3n_id].EstaLibre()) {
                        estacion = this.est_load[estaci\u00f3n_id];
                        break;
                    }
                }
                break;
            }
            case almacenamiento: {
                for (estaci\u00f3n_id = 0; estaci\u00f3n_id < Tomasulo_conf.NUM_EST_STORE; ++estaci\u00f3n_id) {
                    if (this.est_store[estaci\u00f3n_id].EstaLibre()) {
                        estacion = this.est_store[estaci\u00f3n_id];
                        break;
                    }
                }
                break;
            }
        }
        if (estacion == null || this.rob.EstaLleno()) {
            return false;
        }
        final int rob_id = this.rob.addEntry(registroIssue, this.cycle);
        estacion.Ocupar(registroIssue.getInstruction(), rob_id, this.cycle);
        this.rob.getRoB_entry(rob_id).setEstacion(estaci\u00f3n_id);
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
            this.est_store[estacion].Libera();
        }
    }
    
    @Override
    protected void WB() {
        int marca_actual = Integer.MAX_VALUE;
        Estacion estacion = null;
        for (final Estacion check : this.est_todas) {
            if (!check.EstaLibre() && check.ready_for_WB() && check.marca_tiempo < marca_actual) {
                marca_actual = check.marca_tiempo;
                estacion = check;
            }
        }
        final CDB cdb = CDB.read();
        if (estacion == null) {
            cdb.update(-1, 0);
            return;
        }
        final boolean isPF = estacion.getIns_info().getTipo() == InstructionInfo.Instruction_type.pflotante;
        cdb.update(estacion.rob_id, estacion.resultado);
        for (final Estacion check2 : this.est_todas) {
            if (!check2.EstaLibre() && check2.Q_j == cdb.getRob()) {
                check2.Q_j = -1;
                check2.V_j = cdb.getValor();
            }
            if (!check2.EstaLibre() && check2.Q_k == cdb.getRob()) {
                check2.Q_k = -1;
                check2.V_k = cdb.getValor();
            }
        }
        final RoB_entry entry = this.rob.getRoB_entry(estacion.rob_id);
        if (isPF) {
            entry.setHI(((EstacionPF)estacion).getHI());
            entry.setLOW(((EstacionPF)estacion).getLOW());
        }
        entry.WBdone();
        entry.setResultado(cdb.getValor());
        entry.setStage("WB");
        entry.getIns_info().setWB(this.cycle);
        estacion.Libera();
    }
    
    @Override
    protected void Commit() {
        final RoB_entry head = this.rob.getHead();
        if (!head.EstaOcupada() || !head.isWBdone()) {
            return;
        }
        final InstructionInfo.Instruction_type type = head.getIns_info().getTipo();
        ++this.instruccionesConfirmadas;
        if (head.isBranch()) {
            if (head.resultadoPrediccion() != head.resultadoSalto()) {
                this.emitiendoIncorrectas = false;
                final registro[] regs2;
                final registro[] regs = regs2 = registro.getRegs();
                for (final registro reg : regs2) {
                    reg.Libera();
                }
                for (final Estacion estacion : this.est_enteros) {
                    estacion.Libera();
                }
                for (final Estacion estacion : this.est_pf) {
                    estacion.Libera();
                }
                for (final Estacion estacion : this.est_load) {
                    estacion.Libera();
                }
                for (final Estacion estacion : this.est_store) {
                    if (!estacion.EstaConfirmada()) {
                        estacion.Libera();
                    }
                }
                this.op_enteros.LiberarOperador();
                this.op_pf.LiberarOperador();
                this.op_load.LiberarOperador();
                this.stage_regs[0].setInstruction(0);
                this.stage_regs[1].setInstruction(0);
                this.stage_regs[0].getIns_info().setDiscard(this.cycle);
                this.stage_regs[1].getIns_info().setDiscard(this.cycle);
                ++this.fallos_predictor;
            }
            else {
                ++this.aciertos_predictor;
            }
        }
        else if (type == InstructionInfo.Instruction_type.almacenamiento) {
            this.est_store[head.getEstacion()].Confirma();
        }
        else {
            final int reg_id = head.getDestino();
            if (reg_id != 0) {
                final registro reg2 = registro.getReg(reg_id);
                reg2.setValor(head.getResultado());
                if (reg2.getRob() == this.rob.getHeadID()) {
                    reg2.Libera();
                }
            }
            if (type == InstructionInfo.Instruction_type.pflotante) {
                final registro Rhi = registro.getReg(32);
                final registro Rlow = registro.getReg(33);
                Rhi.setValor(head.getHI());
                Rlow.setValor(head.getLOW());
                if (Rhi.getRob() == this.rob.getHeadID()) {
                    Rhi.Libera();
                    Rlow.Libera();
                }
            }
        }
        this.rob.removeHead(this.cycle);
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
                Logger.getLogger(TomasuloP3.class.getName()).log(Level.SEVERE, null, ex);
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
        return "Tomasulo con especulaci\u00f3n (P3). Predictor: " + this.bp + "\n" + super.toString();
    }
}
