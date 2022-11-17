

package mars.pipeline.tomasulo.estaciones;

import mars.pipeline.tomasulo.RoB_entry;
import mars.pipeline.tomasulo.registro;
import mars.pipeline.Decode;
import mars.pipeline.tomasulo.ReorderBuffer;
import mars.pipeline.tomasulo.Tomasulo_conf;
import mars.pipeline.tomasulo.Estacion;

public class EstacionSW extends Estacion
{
    public EstacionSW() {
        super(Tomasulo_conf.LAT_STORE);
    }
    
    @Override
    public boolean ready_for_WB() {
        return false;
    }
    
    @Override
    public boolean Ocupar(final int instruction, final int rob_entry_id, final int cycle) {
        if (Tomasulo_conf.PRACTICA != 2) {
            ReorderBuffer.getReorderBuffer().getRoB_entry(rob_entry_id).WBdone();
            this.confirmado = false;
        }
        else {
            this.confirmado = true;
        }
        this.instruction = instruction;
        if (this.busy) {
            return false;
        }
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
        this.desp = Decode.getInm_ExtensionSigno(instruction);
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
        return true;
    }
    
    @Override
    public boolean Execute(final int cycle) {
        final RoB_entry rob_entry = ReorderBuffer.getReorderBuffer().getRoB_entry(this.rob_id);
        if (this.estado == 0) {
            this.dir = this.V_j + this.desp;
            this.ins_info.setAC(cycle);
            if (rob_entry != null) {
                rob_entry.setStage("AC");
            }
            this.estado = 1;
            return false;
        }
        if (this.estado == 1) {
            this.ins_info.setEX_init(cycle);
        }
        if (rob_entry != null) {
            rob_entry.setStage("M" + this.estado);
        }
        this.ins_info.setEX_end(cycle);
        ++this.estado;
        return this.estado == this.latencia;
    }
    
    @Override
    public String toString() {
        return (this.busy ? ("  SI    " + ((this.Q_j != -1) ? Decode.normaliza("#" + this.Q_j, 15) : Decode.normaliza("    0x" + Integer.toHexString(this.V_j), 15)) + Decode.normaliza(Integer.toString(this.desp), 7) + Decode.normaliza((this.estado != 0) ? ("0x" + Integer.toHexString(this.dir)) : "", 12) + Decode.normaliza("#" + this.rob_id, 5) + ((this.Q_k != -1) ? Decode.normaliza("#" + this.Q_k, 15) : Decode.normaliza("    " + this.V_k, 15)) + Decode.normaliza(this.confirmado ? "Si" : "No", 8) + this.estado + "/" + this.latencia) : "  NO    ") + "\n";
    }
}
