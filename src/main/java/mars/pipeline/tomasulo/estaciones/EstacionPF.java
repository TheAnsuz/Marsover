

package mars.pipeline.tomasulo.estaciones;

import java.util.logging.Level;
import java.util.logging.Logger;
import mars.ProcessingException;
import mars.mips.instructions.BasicInstruction;
import mars.pipeline.Decode;
import mars.pipeline.tomasulo.Estacion;
import mars.pipeline.tomasulo.InstructionSetTomasulo;
import mars.pipeline.tomasulo.ReorderBuffer;
import mars.pipeline.tomasulo.RoB_entry;
import mars.pipeline.tomasulo.Tomasulo_conf;
import mars.pipeline.tomasulo.registro;

public class EstacionPF extends Estacion
{
    private int HI;
    private int LOW;
    
    public EstacionPF() {
        super(Tomasulo_conf.LAT_PF);
    }
    
    @Override
    public boolean Ocupar(final int instruction, final int rob_entry_id, final int cycle) {
        if (this.busy) {
            return false;
        }
        this.instruction = instruction;
        this.busy = true;
        this.marca_tiempo = cycle;
        this.estado = 0;
        this.rob_id = rob_entry_id;
        final ReorderBuffer rob_instance = ReorderBuffer.getReorderBuffer();
        final int Rs = Decode.getRs(instruction);
        if (Rs != 0) {
            final registro reg = registro.getReg(Rs);
            final int rob = reg.getRob();
            if (rob == -1) {
                this.Q_j = -1;
                this.V_j = reg.getValor();
            }
            else {
                final RoB_entry rob_entry = rob_instance.getRoB_entry(rob);
                if (rob_entry.isWBdone()) {
                    this.Q_j = -1;
                    this.V_j = rob_entry.getResultado();
                }
                else {
                    this.Q_j = rob;
                }
            }
        }
        else {
            this.Q_j = -1;
            this.V_j = 0;
        }
        final int Rt = Decode.getRt(instruction);
        if (Rt != 0) {
            final registro reg2 = registro.getReg(Rt);
            final int rob2 = reg2.getRob();
            if (rob2 == -1) {
                this.Q_k = -1;
                this.V_k = reg2.getValor();
            }
            else {
                final RoB_entry rob_entry2 = rob_instance.getRoB_entry(rob2);
                if (rob_entry2.isWBdone()) {
                    this.Q_k = -1;
                    this.V_k = rob_entry2.getResultado();
                }
                else {
                    this.Q_k = rob2;
                }
            }
        }
        else {
            this.Q_k = -1;
            this.V_k = 0;
        }
        final int destino = Decode.getDestination(instruction);
        if (destino != 0) {
            registro.getReg(destino).marcaRob(rob_entry_id);
        }
        registro.getReg(32).marcaRob(rob_entry_id);
        registro.getReg(33).marcaRob(rob_entry_id);
        return true;
    }
    
    @Override
    public boolean Execute(final int cycle) {
        final RoB_entry rob_entry = ReorderBuffer.getReorderBuffer().getRoB_entry(this.rob_id);
        if (this.estado == 0) {
            this.ins_info.setEX_init(cycle);
            final InstructionSetTomasulo set = InstructionSetTomasulo.getInstance();
            final BasicInstruction instr = set.findByBinaryCode(this.instruction);
            if (instr != null) {
                set.setOp1(this.V_j);
                set.setOp2(this.V_k);
                try {
                    instr.getSimulationCode().simulate(null);
                }
                catch (ProcessingException ex) {
                    Logger.getLogger(EstacionPF.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.resultado = set.getLOW();
                this.HI = set.getHI();
                this.LOW = set.getLOW();
            }
        }
        this.ins_info.setEX_end(cycle);
        ++this.estado;
        rob_entry.setStage("X" + this.estado);
        return this.estado == this.latencia;
    }
    
    public int getHI() {
        return this.HI;
    }
    
    public void setHI(final int HI) {
        this.HI = HI;
    }
    
    public int getLOW() {
        return this.LOW;
    }
    
    public void setLOW(final int LOW) {
        this.LOW = LOW;
    }
    
    @Override
    public String toString() {
        return (this.busy ? ("  SI    " + ((this.Q_j != -1) ? Decode.normaliza("#" + this.Q_j, 15) : Decode.normaliza("    " + this.V_j, 15)) + ((this.Q_k != -1) ? Decode.normaliza("#" + this.Q_k, 15) : Decode.normaliza("    " + this.V_k, 15)) + Decode.normaliza("#" + this.rob_id, 5) + Decode.normaliza((this.estado == this.latencia) ? ("" + this.resultado) : "", 13) + this.estado + "/" + this.latencia) : "  NO    ") + "\n";
    }
}
