

package mars.pipeline.tomasulo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import mars.pipeline.Decode;

public class BTB
{
    private final BTB_entry[] entradas;
    private int ultima_actualizacion;
    private int penultima_actualizacion;
    
    public BTB() {
        this.entradas = new BTB_entry[Tomasulo_conf.NUM_BTB_ENTRIES];
        for (int i = 0; i < Tomasulo_conf.NUM_BTB_ENTRIES; ++i) {
            this.entradas[i] = new BTB_entry(Tomasulo_conf.NUM_BTB_ENTRIES - 1);
        }
        final int n = 1;
        this.ultima_actualizacion = n;
        this.penultima_actualizacion = n;
    }
    
    public int branch_addres(final int instruccion, final int address) {
        final int inm = Decode.getInm_ExtensionSigno(instruccion) << 2;
        return address + 4 + inm;
    }
    
    public int getPrediction(final int PC) {
        BTB_entry entry = null;
        for (final BTB_entry t_entry : this.entradas) {
            if (t_entry.PC == PC) {
                entry = t_entry;
                break;
            }
        }
        if (entry == null) {
            return PC + 4;
        }
        return (entry.predictor / 2 == 1) ? entry.destino : (PC + 4);
    }
    
    private void actualizar_LRU(final BTB_entry entry) {
        for (final BTB_entry t_entry : this.entradas) {
            if (t_entry.marca_tiempo < entry.marca_tiempo) {
                final BTB_entry btb_entry = t_entry;
                ++btb_entry.marca_tiempo;
            }
        }
        entry.marca_tiempo = 0;
    }
    
    public void Actualizar(final int PC, final int instruccion, final boolean tomado) {
        for (final BTB_entry entry : this.entradas) {
            if (entry.PC == PC) {
                if (tomado) {
                    if (entry.predictor < 3) {
                        final BTB_entry btb_entry = entry;
                        ++btb_entry.predictor;
                    }
                }
                else if (entry.predictor > 0) {
                    final BTB_entry btb_entry2 = entry;
                    --btb_entry2.predictor;
                }
                this.actualizar_LRU(entry);
                return;
            }
        }
        BTB_entry entry2 = null;
        for (final BTB_entry t_entry : this.entradas) {
            if (t_entry.marca_tiempo == Tomasulo_conf.NUM_BTB_ENTRIES - 1) {
                entry2 = t_entry;
                break;
            }
        }
        entry2.PC = PC;
        entry2.predictor = (tomado ? 2 : 1);
        entry2.destino = this.branch_addres(instruccion, PC);
        entry2.valido = true;
        this.actualizar_LRU(entry2);
    }
    
    public void writeBTB(final int cycle) {
        if (cycle < 0 || cycle > Tomasulo_conf.MAX_CYCLE_DUMPING_BTB) {
            return;
        }
        final String filename = "BTB" + this.ultima_actualizacion + ".html";
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write("<html>\n<head>\n<title>Estado BTB " + this.ultima_actualizacion + "</title>\n</head>\n<body>\n");
            writer.write("<a>Estado BTB ciclo " + this.ultima_actualizacion + "</a>&nbsp;&nbsp;<br>");
            writer.write("<a href=\"Resumen.html\">RESUMEN</a>&nbsp;&nbsp;");
            writer.write("<a href=\"cycle" + this.ultima_actualizacion + ".html\">ESTADO</a>&nbsp;&nbsp;");
            writer.write("<a href=\"diag" + this.ultima_actualizacion + ".html\">DIAGRAMA</a>&nbsp;&nbsp;");
            writer.write("<a href=\"BTB1.html\">INICIO</a>&nbsp;&nbsp;");
            if (this.ultima_actualizacion > 1) {
                writer.write("<a href=\"BTB" + this.penultima_actualizacion + ".html\">[Ciclo " + this.penultima_actualizacion + "]</a>&nbsp;&nbsp;");
            }
            else {
                writer.write("<a>[-]</a>&nbsp;&nbsp;");
            }
            writer.write("<a href=\"BTB" + cycle + ".html\">[Ciclo " + cycle + "]</a>&nbsp;&nbsp;");
            writer.write("<pre>");
            writer.write("ID  PC          DIRECCI\u00d3N   ANTIG\u00dcEDAD  PREDICTOR\n");
            for (int i = 0; i < Tomasulo_conf.NUM_BTB_ENTRIES; ++i) {
                final BTB_entry entry = this.entradas[i];
                String str = Decode.normaliza("" + i, 4);
                if (entry.valido) {
                    str += Decode.normaliza("0x" + Integer.toHexString(entry.PC), 12);
                    str += Decode.normaliza("0x" + Integer.toHexString(entry.destino), 12);
                    str += Decode.normaliza(Integer.toHexString(entry.marca_tiempo), 12);
                    str = str + entry.predictor / 2 + "" + entry.predictor % 2;
                }
                writer.write(str + "\n");
            }
            writer.write("</pre>");
            writer.write("</body> \n </html>");
            writer.close();
            this.penultima_actualizacion = this.ultima_actualizacion;
            this.ultima_actualizacion = cycle;
        }
        catch (IOException ex) {}
    }
    
    class BTB_entry
    {
        boolean valido;
        protected int PC;
        protected int destino;
        protected int predictor;
        protected int marca_tiempo;
        
        public BTB_entry(final int marca_tiempo) {
            this.marca_tiempo = marca_tiempo;
            this.valido = false;
        }
    }
}
