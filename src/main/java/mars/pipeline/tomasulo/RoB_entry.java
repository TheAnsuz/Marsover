

package mars.pipeline.tomasulo;

import mars.pipeline.Decode;
import mars.pipeline.DiagramaMulticiclo.InstructionInfo;
import mars.pipeline.StageRegisters;

public class RoB_entry
{
    private int address;
    private int instruction;
    private boolean busy;
    private String stage;
    private int estacion;
    private int destino;
    private int resultado;
    private boolean prediccion;
    private boolean resultadoSalto;
    private boolean hasWB;
    private boolean WBdone;
    private boolean branch;
    private int HI;
    private int LOW;
    private InstructionInfo ins_info;
    
    public boolean EstaOcupada() {
        return this.busy;
    }
    
    public void Libera() {
        this.busy = false;
    }
    
    public void add_instruction(final StageRegisters st_reg, final int cycle) {
        this.busy = true;
        this.instruction = st_reg.getInstruction();
        this.address = st_reg.getAddress();
        this.stage = "Is";
        this.destino = Decode.getDestination(this.instruction);
        this.hasWB = (Decode.isBranch(this.instruction) || Decode.getDestination(this.instruction) != 0);
        this.WBdone = false;
        this.resultado = st_reg.getRdValue();
        this.branch = Decode.isBranch(this.instruction);
        this.ins_info = st_reg.getIns_info();
        this.prediccion = st_reg.getPrediccion();
        this.resultadoSalto = st_reg.getResultadoSalto();
    }
    
    public InstructionInfo getIns_info() {
        return this.ins_info;
    }
    
    public int getAddress() {
        return this.address;
    }
    
    public int getInstruction() {
        return this.instruction;
    }
    
    public void setStage(final String st) {
        this.stage = st;
    }
    
    public String getStage() {
        return this.stage;
    }
    
    public void setResultado(final int resultado) {
        this.resultado = resultado;
    }
    
    public void WBdone() {
        this.WBdone = true;
    }
    
    public boolean isWBdone() {
        return this.WBdone;
    }
    
    public int getResultado() {
        return this.resultado;
    }
    
    public int getDestino() {
        return this.destino;
    }
    
    public boolean resultadoPrediccion() {
        return this.prediccion;
    }
    
    public boolean resultadoSalto() {
        return this.resultadoSalto;
    }
    
    public boolean isBranch() {
        return this.branch;
    }
    
    public int getEstacion() {
        return this.estacion;
    }
    
    public void setEstacion(final int estacion) {
        this.estacion = estacion;
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
        boolean imprimir = this.busy;
        final String estado_a_imprimir = this.stage;
        if ("CO".equals(this.stage)) {
            this.stage = "";
            imprimir = true;
        }
        String res = "";
        if (this.hasWB && this.WBdone) {
            res = (this.branch ? (this.resultadoSalto ? "T" : "NT") : Integer.toString(this.resultado));
        }
        final String str = (imprimir ? ("  SI    " + Decode.toString(this.instruction) + "    " + Decode.normaliza(estado_a_imprimir, 7) + Decode.normaliza((this.hasWB & this.destino != 0) ? Decode.getRegisterName(this.destino) : "", 8) + Decode.normaliza(res, 11) + Decode.normaliza(this.branch ? (this.prediccion ? "     T" : "     NT") : "   ", 11) + "0x" + Integer.toHexString(this.address)) : "  NO    ") + "\n";
        return str;
    }
}
