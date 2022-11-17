

package mars.pipeline.tomasulo.estaciones;

import mars.mips.instructions.BasicInstruction;
import mars.ProcessingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mars.ProgramStatement;
import mars.pipeline.tomasulo.InstructionSetTomasulo;
import mars.pipeline.tomasulo.RoB_entry;
import mars.mips.instructions.BasicInstructionFormat;
import mars.pipeline.tomasulo.registro;
import mars.pipeline.tomasulo.ReorderBuffer;
import mars.pipeline.Decode;
import mars.pipeline.tomasulo.Tomasulo_conf;
import mars.pipeline.tomasulo.Estacion;

public class EstacionINT extends Estacion
{
    public EstacionINT() {
        super(Tomasulo_conf.LAT_ENTEROS);
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
        final BasicInstructionFormat type = Decode.getFormat(instruction);
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
        if (type == BasicInstructionFormat.I_FORMAT) {
            this.Q_k = -1;
            this.V_k = Decode.getInm_ExtensionSigno(instruction);
        }
        else {
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
        }
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
                    Logger.getLogger(EstacionINT.class.getName()).log(Level.SEVERE, null, ex);
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
