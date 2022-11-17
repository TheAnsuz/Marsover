

package mars.pipeline.tomasulo.estaciones_alumnos;

import mars.pipeline.Decode;
import mars.pipeline.tomasulo.Estacion;
import mars.pipeline.tomasulo.ReorderBuffer;
import mars.pipeline.tomasulo.RoB_entry;
import mars.pipeline.tomasulo.Tomasulo_conf;

public class EstacionSW_alumnos extends Estacion
{
    public EstacionSW_alumnos() {
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
