

package mars.pipeline.tomasulo.estaciones_alumnos;

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

public class EstacionINT_alumnos extends Estacion
{
    public EstacionINT_alumnos() {
        super(Tomasulo_conf.LAT_ENTEROS);
    }
    
    @Override
    public boolean Ocupar(final int instruction, final int rob_entry_id, final int cycle) {
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
                if (Decode.hasShamt(this.instruction)) {
                    set.setOp1(this.V_k);
                    set.setOp2(Decode.getShamt(this.instruction));
                }
                else {
                    set.setOp1(this.V_j);
                    set.setOp2(this.V_k);
                }
                try {
                    instr.getSimulationCode().simulate(null);
                }
                catch (ProcessingException ex) {
                    Logger.getLogger(EstacionINT_alumnos.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.resultado = set.getResult();
            }
        }
        rob_entry.setStage("EX");
        ++this.estado;
        if (this.estado == this.latencia) {
            this.ins_info.setEX_end(cycle);
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return (this.busy ? ("  SI    " + ((this.Q_j != -1) ? Decode.normaliza("#" + this.Q_j, 15) : Decode.normaliza("    " + this.V_j, 15)) + ((this.Q_k != -1) ? Decode.normaliza("#" + this.Q_k, 15) : Decode.normaliza("    " + this.V_k, 15)) + Decode.normaliza("#" + this.rob_id, 5) + Decode.normaliza((this.estado == this.latencia) ? ("" + this.resultado) : "", 13) + this.estado + "/" + this.latencia) : "  NO    ") + "\n";
    }
}
