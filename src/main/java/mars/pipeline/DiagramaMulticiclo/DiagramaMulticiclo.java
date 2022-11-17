

package mars.pipeline.DiagramaMulticiclo;

import java.util.Iterator;
import java.io.IOException;
import mars.pipeline.Decode;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class DiagramaMulticiclo
{
    private final List<InstructionInfo> lista_instrucciones;
    private int limite;
    private int ciclos_por_ventana;
    private int primer_ciclo;
    private int ultimo_ciclo;
    private int ultimo_ciclo_escrito;
    
    public DiagramaMulticiclo(final int limite) {
        this.limite = limite;
        this.ciclos_por_ventana = 25;
        this.primer_ciclo = 1;
        this.ultimo_ciclo = this.ciclos_por_ventana;
        this.ultimo_ciclo_escrito = 0;
        this.lista_instrucciones = new ArrayList<InstructionInfo>();
    }
    
    public DiagramaMulticiclo(final int limite, final int ciclos_por_ventana) {
        this.limite = limite;
        this.ciclos_por_ventana = ciclos_por_ventana;
        this.primer_ciclo = 1;
        this.ultimo_ciclo = ciclos_por_ventana;
        this.ultimo_ciclo_escrito = 0;
        this.lista_instrucciones = new ArrayList<InstructionInfo>();
    }
    
    private void reconfigure() {
        final int slot = this.ultimo_ciclo_escrito / this.ciclos_por_ventana;
        this.primer_ciclo = slot * this.ciclos_por_ventana + 1;
        this.ultimo_ciclo = (slot + 1) * this.ciclos_por_ventana;
    }
    
    public void setLimite(final int limite) {
        this.limite = limite;
        this.reconfigure();
    }
    
    public void setVentana(final int ciclos_por_ventana) {
        this.ciclos_por_ventana = ciclos_por_ventana;
        this.reconfigure();
    }
    
    public int getLimite() {
        return this.limite;
    }
    
    public void addInstruction(final InstructionInfo instruction) {
        if (this.ultimo_ciclo_escrito <= this.limite) {
            this.lista_instrucciones.add(instruction);
        }
    }
    
    private String header(final String str) {
        return "<html>\n<head>\n<title>" + str + "</title>\n</head>\n<body>\n";
    }
    
    public void writeDiagrama(final int cycle) {
        this.ultimo_ciclo_escrito = cycle;
        if (cycle > this.limite || cycle < 0) {
            return;
        }
        if (cycle > this.ultimo_ciclo) {
            this.primer_ciclo = this.ultimo_ciclo + 1;
            this.ultimo_ciclo += this.ciclos_por_ventana;
        }
        try {
            final String filename = "diag" + cycle + ".html";
            final BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(this.header("Diagrama multiclo: ciclo " + cycle));
            writer.write("<a>Diagrama multiclo en ciclo " + cycle + "</a>&nbsp;&nbsp;<br>");
            writer.write("<a href=\"Resumen.html\">RESUMEN</a>&nbsp;&nbsp;");
            writer.write("<a href=\"cycle" + cycle + ".html\">ESTADO</a>&nbsp;&nbsp;");
            writer.write("<a href=\"diag1.html\">INICIO</a>&nbsp;&nbsp;");
            if (cycle > 1) {
                writer.write("<a href=\"diag" + (cycle - 1) + ".html\">[-1]</a>&nbsp;&nbsp;");
            }
            else {
                writer.write("<a>[-1]</a>&nbsp;&nbsp;");
            }
            if (cycle > 6) {
                writer.write("<a href=\"diag" + (cycle - 5) + ".html\">[-5]</a>&nbsp;&nbsp;");
            }
            else {
                writer.write("<a>[-5]</a>&nbsp;&nbsp;");
            }
            writer.write("<a href=\"diag" + (cycle + 1) + ".html\">[+1]</a>&nbsp;&nbsp;");
            writer.write("<a href=\"diag" + (cycle + 5) + ".html\">[+5]</a>&nbsp;&nbsp;");
            writer.write("<pre>");
            String cicle_header = Decode.normaliza("", 24);
            for (int i = this.primer_ciclo; i <= this.ultimo_ciclo; ++i) {
                cicle_header = cicle_header + "| " + Decode.normaliza(Integer.toString(i), 3);
            }
            writer.write(cicle_header + "|\n");
            boolean empieza_impresion = false;
            for (final InstructionInfo ins : this.lista_instrucciones) {
                if (!empieza_impresion && ins.isFinalizada()) {
                    int end = Integer.max(ins.getWB(), ins.getEX_end());
                    end = Integer.max(end, ins.getCommit());
                    end = Integer.max(end, ins.getDiscard());
                    if (end < this.primer_ciclo) {
                        continue;
                    }
                }
                writer.write(ins.toString(this.primer_ciclo, this.ultimo_ciclo));
                empieza_impresion = true;
            }
            writer.write("</pre>");
            writer.write("</body> </html>");
            writer.close();
        }
        catch (IOException ex) {}
    }
}
