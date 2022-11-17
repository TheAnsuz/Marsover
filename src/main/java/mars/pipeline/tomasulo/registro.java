

package mars.pipeline.tomasulo;

import mars.pipeline.Decode;

public class registro
{
    private int valor;
    private int rob;
    private static registro[] registros;
    public static final int HI = 32;
    public static final int LOW = 33;
    public static final int NUM_REGISTROS = 34;
    
    private registro() {
        this.valor = 0;
        this.rob = -1;
    }
    
    public static registro getReg(final int id) {
        if (registro.registros == null) {
            registro.registros = new registro[34];
            for (int i = 0; i < 34; ++i) {
                registro.registros[i] = new registro();
            }
        }
        return (id >= 0 && id <= 34) ? registro.registros[id] : null;
    }
    
    public static registro[] getRegs() {
        if (registro.registros == null) {
            registro.registros = new registro[34];
            for (int i = 0; i < 34; ++i) {
                registro.registros[i] = new registro();
            }
        }
        return registro.registros;
    }
    
    public int getValor() {
        return this.valor;
    }
    
    public void setValor(final int valor) {
        this.valor = valor;
    }
    
    public int getRob() {
        return this.rob;
    }
    
    public void marcaRob(final int rob) {
        this.rob = rob;
    }
    
    public void Libera() {
        this.rob = -1;
    }
    
    @Override
    public String toString() {
        return Decode.normaliza((this.rob == -1) ? " " : ("#" + Integer.toString(this.rob)), 5) + Decode.normaliza(Integer.toString(this.valor), 12);
    }
    
    static {
        registro.registros = null;
    }
}
