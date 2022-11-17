

package mars.pipeline.tomasulo.estaciones;

import mars.mips.instructions.BasicInstruction;
import mars.ProcessingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mars.ProgramStatement;
import mars.pipeline.tomasulo.InstructionSetTomasulo;
import mars.pipeline.tomasulo.RoB_entry;
import mars.pipeline.tomasulo.registro;
import mars.pipeline.Decode;
import mars.pipeline.tomasulo.ReorderBuffer;
import mars.pipeline.tomasulo.Tomasulo_conf;
import mars.pipeline.tomasulo.Estacion;

public class EstacionLW extends Estacion
{
    public EstacionLW() {
        super(Tomasulo_conf.LAT_LOAD);
        this.Q_k = -1;
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
        final int inm_ExtensionSigno = Decode.getInm_ExtensionSigno(instruction);
        this.desp = inm_ExtensionSigno;
        this.V_k = inm_ExtensionSigno;
        final int destino = Decode.getDestination(instruction);
        if (destino != 0) {
            registro.getReg(destino).marcaRob(rob_entry_id);
        }
        return true;
    }
    
    @Override
    public boolean Execute(final int cycle) {
        final RoB_entry rob_entry = ReorderBuffer.getReorderBuffer().getRoB_entry(this.rob_id);
        if (this.estado == 0) {
            this.dir = this.V_j + this.desp;
            this.ins_info.setAC(cycle);
            rob_entry.setStage("AC");
            this.estado = 1;
            final InstructionSetTomasulo set = InstructionSetTomasulo.getInstance();
            final BasicInstruction instr = set.findByBinaryCode(this.instruction);
            if (instr != null) {
                set.setOp2(this.V_j);
                set.setOp1(this.desp);
                try {
                    instr.getSimulationCode().simulate(null);
                }
                catch (ProcessingException ex) {
                    Logger.getLogger(EstacionLW.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.resultado = set.getResult();
            }
            return false;
        }
        if (this.estado == 1) {
            this.ins_info.setEX_init(cycle);
        }
        rob_entry.setStage("M" + this.estado);
        ++this.estado;
        this.ins_info.setEX_end(cycle);
        return this.estado == this.latencia;
    }
    
    @Override
    public String toString() {
        return (this.busy ? ("  SI    " + ((this.Q_j != -1) ? Decode.normaliza("#" + this.Q_j, 15) : Decode.normaliza("    0x" + Integer.toHexString(this.V_j), 15)) + Decode.normaliza(Integer.toString(this.desp), 7) + Decode.normaliza((this.estado != 0) ? ("0x" + Integer.toHexString(this.dir)) : "", 12) + Decode.normaliza("#" + this.rob_id, 4) + Decode.normaliza((this.estado == this.latencia) ? ("" + this.resultado) : "", 13) + this.estado + "/" + this.latencia) : "  NO    ") + "\n";
    }
}
