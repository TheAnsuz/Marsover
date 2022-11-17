

package mars.pipeline.tomasulo;

import mars.pipeline.Decode;
import mars.pipeline.DiagramaMulticiclo.InstructionInfo;
import mars.pipeline.StageRegisters;

public class ReorderBuffer
{
    private final RoB_entry[] rob;
    private final int num_entradas;
    private int entradas_ocupadas;
    private int cabeza;
    private int cola;
    private static ReorderBuffer rob_instance;
    
    private ReorderBuffer(final int num_entradas) {
        this.num_entradas = num_entradas;
        this.rob = new RoB_entry[num_entradas];
        for (int i = 0; i < this.rob.length; ++i) {
            this.rob[i] = new RoB_entry();
        }
        final int cabeza = 0;
        this.entradas_ocupadas = cabeza;
        this.cola = cabeza;
        this.cabeza = cabeza;
    }
    
    public static ReorderBuffer getReorderBuffer() {
        if (ReorderBuffer.rob_instance == null) {
            ReorderBuffer.rob_instance = new ReorderBuffer(Tomasulo_conf.NUM_ENTRADAS_ROB);
        }
        return ReorderBuffer.rob_instance;
    }
    
    public int getEntradasOcupadas() {
        return this.entradas_ocupadas;
    }
    
    public boolean EstaLleno() {
        return this.entradas_ocupadas == this.num_entradas;
    }
    
    public int addEntry(final StageRegisters st_reg, final int cycle) {
        if (this.entradas_ocupadas == this.num_entradas) {
            return -1;
        }
        final int entry = this.cola;
        this.rob[entry].add_instruction(st_reg, cycle);
        this.cola = (this.cola + 1) % this.num_entradas;
        ++this.entradas_ocupadas;
        return entry;
    }
    
    public boolean LimpiaRoB(final int cycle) {
        if (this.entradas_ocupadas == 0) {
            return false;
        }
        int i = this.cabeza;
        do {
            final RoB_entry re = this.rob[i];
            if (re.isWBdone() && re.EstaOcupada()) {
                final int dest = re.getDestino();
                if (dest != 0 && registro.getReg(dest).getRob() == i) {
                    registro.getReg(dest).setValor(re.getResultado());
                    registro.getReg(dest).Libera();
                }
                if (re.getIns_info().getTipo() == InstructionInfo.Instruction_type.pflotante) {
                    if (registro.getReg(32).getRob() == i) {
                        registro.getReg(32).setValor(re.getHI());
                        registro.getReg(32).Libera();
                    }
                    if (registro.getReg(33).getRob() == i) {
                        registro.getReg(33).setValor(re.getLOW());
                        registro.getReg(33).Libera();
                    }
                }
                re.setStage("CO");
                re.getIns_info().setCommit(cycle);
                re.getIns_info().finaliza(cycle);
                re.Libera();
            }
            i = (i + 1) % this.num_entradas;
        } while (i != this.cola);
        while (this.entradas_ocupadas != 0 && !this.rob[this.cabeza].EstaOcupada()) {
            this.cabeza = (this.cabeza + 1) % this.num_entradas;
            --this.entradas_ocupadas;
        }
        return true;
    }
    
    public int clean(final int ciclo) {
        this.entradas_ocupadas = 1;
        int ret = 0;
        for (int i = (this.cabeza + 1) % this.num_entradas; i != this.cola; i = (i + 1) % this.num_entradas) {
            this.rob[i].Libera();
            this.rob[i].getIns_info().setDiscard(ciclo);
            ++ret;
        }
        this.cola = (this.cabeza + 1) % this.num_entradas;
        return ret;
    }
    
    public RoB_entry getHead() {
        return this.rob[this.cabeza];
    }
    
    public int getHeadID() {
        return this.cabeza;
    }
    
    public void removeHead(final int cycle) {
        this.rob[this.cabeza].Libera();
        this.rob[this.cabeza].getIns_info().finaliza(cycle);
        this.cabeza = (this.cabeza + 1) % this.num_entradas;
        --this.entradas_ocupadas;
    }
    
    public RoB_entry getRoB_entry(final int rob_id) {
        return (rob_id < 0 || rob_id >= this.num_entradas || !this.rob[rob_id].EstaOcupada()) ? null : this.rob[rob_id];
    }
    
    @Override
    public String toString() {
        String str = "Entrada Ocupado " + Decode.normaliza("Instrucci\u00f3n", 24) + "  Estado  Destino  Valor         Predic. PC\n";
        for (int i = 0; i < this.rob.length; ++i) {
            str = str + "  " + Decode.normaliza(Integer.toString(i), 6) + this.rob[i].toString();
        }
        return str;
    }
    
    static {
        ReorderBuffer.rob_instance = null;
    }
}
