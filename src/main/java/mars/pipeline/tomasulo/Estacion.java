

package mars.pipeline.tomasulo;

import mars.pipeline.DiagramaMulticiclo.InstructionInfo;

public abstract class Estacion
{
    protected final int latencia;
    protected int instruction;
    protected int rob_id;
    protected int Q_j;
    protected int V_j;
    protected int Q_k;
    protected int V_k;
    protected int resultado;
    protected int estado;
    protected boolean busy;
    protected boolean confirmado;
    protected int marca_tiempo;
    protected int desp;
    protected int dir;
    protected InstructionInfo ins_info;
    
    protected Estacion(final int ciclos) {
        this.latencia = ciclos;
        this.busy = false;
    }
    
    public int getMarca_tiempo() {
        return this.marca_tiempo;
    }
    
    public void setMarca_tiempo(final int marca_tiempo) {
        this.marca_tiempo = marca_tiempo;
    }
    
    public int getRob_entry() {
        return this.rob_id;
    }
    
    public int getQj() {
        return this.Q_j;
    }
    
    public int getVj() {
        return this.V_j;
    }
    
    public int getQk() {
        return this.Q_k;
    }
    
    public int getVk() {
        return this.V_k;
    }
    
    public int getResultado() {
        return this.resultado;
    }
    
    public int getEstado() {
        return this.estado;
    }
    
    public boolean EstaLibre() {
        return !this.busy;
    }
    
    public boolean EstaConfirmada() {
        return this.confirmado;
    }
    
    public void Confirma() {
        this.confirmado = true;
    }
    
    public void Libera() {
        this.busy = false;
        final int n = -1;
        this.Q_j = n;
        this.Q_k = n;
        this.estado = 0;
    }
    
    public boolean ready_for_WB() {
        return this.estado == this.latencia;
    }
    
    public InstructionInfo getIns_info() {
        return this.ins_info;
    }
    
    public void leeCDB() {
        final int rob = CDB.read().getRob();
        if (rob != -1) {
            if (this.Q_j == rob) {
                this.Q_j = -1;
                this.Q_j = CDB.read().getValor();
            }
            if (this.Q_k == rob) {
                this.Q_k = -1;
                this.Q_k = CDB.read().getValor();
            }
        }
    }
    
    public abstract boolean Execute(final int p0);
    
    public abstract boolean Ocupar(final int p0, final int p1, final int p2);
}
