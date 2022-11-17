

package mars.pipeline.tomasulo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import mars.pipeline.BranchPredictor;
import mars.pipeline.Decode;
import mars.pipeline.Pipeline;
import mars.pipeline.Stage;
import mars.pipeline.StageRegisters;

public abstract class Tomasulo extends Pipeline
{
    protected final ReorderBuffer rob;
    protected final Estacion[] est_enteros;
    protected final Estacion[] est_load;
    protected final Estacion[] est_store;
    protected final Estacion[] est_pf;
    protected final Estacion[] est_todas;
    protected final Operador op_enteros;
    protected final Operador op_pf;
    protected final Operador op_store;
    protected final Operador op_load;
    protected final StageRegisters[] stage_regs;
    protected int loads;
    protected int stores;
    protected int enteros;
    protected int pflotante;
    protected int saltos;
    protected boolean no_mas_instrucciones;
    
    protected Tomasulo(final BranchPredictor.BranchPredictor_type bp) {
        super(Stage.ID, bp);
        this.no_mas_instrucciones = false;
        this.cycle = 1;
        this.est_todas = new Estacion[Tomasulo_conf.NUM_EST_LOAD + Tomasulo_conf.NUM_EST_STORE + Tomasulo_conf.NUM_EST_ENTEROS + Tomasulo_conf.NUM_EST_PF];
        this.est_load = new Estacion[Tomasulo_conf.NUM_EST_LOAD];
        this.est_store = new Estacion[Tomasulo_conf.NUM_EST_STORE];
        this.est_enteros = new Estacion[Tomasulo_conf.NUM_EST_ENTEROS];
        this.est_pf = new Estacion[Tomasulo_conf.NUM_EST_PF];
        this.stage_regs = new StageRegisters[2];
        for (int i = 0; i < this.stage_regs.length; ++i) {
            this.stage_regs[i] = new StageRegisters();
        }
        this.op_enteros = new Operador(this.est_enteros);
        this.op_pf = new Operador(this.est_pf);
        this.op_store = new Operador(this.est_store);
        this.op_load = new Operador(this.est_load);
        this.rob = ReorderBuffer.getReorderBuffer();
        this.instrucciones = 0;
        this.saltos = 0;
        this.aciertos_predictor = 0;
        this.fallos_predictor = 0;
        this.instruccionesConfirmadas = 0;
        final int n = 0;
        this.loads = n;
        this.stores = n;
        this.pflotante = n;
        this.enteros = n;
    }
    
    public abstract void IF(final StageRegisters p0);
    
    protected abstract boolean Issue();
    
    protected abstract void Execute();
    
    protected abstract void WB();
    
    protected abstract void Commit();
    
    protected abstract boolean UpdatePipeline();
    
    @Override
    public void UpdatePipeline(final int address, final int instruction) {
        while (!this.UpdatePipeline()) {}
    }
    
    private String header(final String str) {
        return "<html>\n<head>\n<title>" + str + "</title>\n</head>\n<body>\n";
    }
    
    private String end() {
        return "</body> \n </html>";
    }
    
    private void print_regs(final BufferedWriter writer) throws IOException {
        writer.write("</pre><a>Registros</a><pre>");
        writer.write("REG   ROB  Valor       REG   ROB  Valor       REG   ROB  Valor       REG   ROB  Valor      \n");
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 4; ++j) {
                final int reg = 4 * i + j;
                writer.write(Decode.normaliza(Decode.getRegisterName(reg), 6) + registro.getReg(reg).toString());
            }
            writer.write("\n");
        }
        writer.write(Decode.normaliza(Decode.getRegisterName(32), 6) + registro.getReg(32).toString());
        writer.write(Decode.normaliza(Decode.getRegisterName(33), 6) + registro.getReg(33).toString() + "\n");
    }
    
    private void print_operadores(final BufferedWriter writer) throws IOException {
        writer.write("</pre><a>Operadores</a><pre>");
        writer.write("Entrada Ocupado Qj  Vj         Qk  Vk         rob  Resultado  Estado\n");
        for (int i = 0; i < this.est_enteros.length; ++i) {
            writer.write(Decode.normaliza(" E" + i, 8) + this.est_enteros[i].toString());
        }
        for (int i = 0; i < this.est_pf.length; ++i) {
            writer.write(Decode.normaliza(" M" + i, 8) + this.est_pf[i].toString());
        }
    }
    
    private void print_load(final BufferedWriter writer) throws IOException {
        writer.write("</pre><a>Estaciones de Lectura</a><pre>");
        writer.write("Entrada Ocupado Qj  Vj         Desp.  Direccci\u00f3n  rob  Resultado  Estado\n");
        for (int i = 0; i < this.est_load.length; ++i) {
            writer.write(Decode.normaliza(" L" + i, 8) + this.est_load[i].toString());
        }
    }
    
    private void print_stores(final BufferedWriter writer) throws IOException {
        writer.write("</pre><a>Estaciones de Escritura</a><pre>");
        writer.write("Entrada Ocupado Qj  Vj         Desp.  Direccci\u00f3n  rob  Qj  Vj         Confir. Estado\n");
        for (int i = 0; i < this.est_store.length; ++i) {
            writer.write(Decode.normaliza(" S" + i, 8) + this.est_store[i].toString());
        }
    }
    
    public void writeStatus() {
        if (this.cycle <= this.limite && this.cycle >= 0) {
            try {
                final String filename = "cycle" + this.cycle + ".html";
                final BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
                writer.write(this.header("Estado procesador: ciclo " + this.cycle));
                writer.write("<a>Estado a final del ciclo " + this.cycle + "</a>&nbsp;&nbsp;<br>");
                writer.write("<a href=\"Resumen.html\">RESUMEN</a>&nbsp;&nbsp;");
                writer.write("<a href=\"diag" + this.cycle + ".html\">DIAGRAMA</a>&nbsp;&nbsp;");
                writer.write("<a href=\"cycle1.html\">INICIO</a>&nbsp;&nbsp;");
                if (this.cycle > 1) {
                    writer.write("<a href=\"cycle" + (this.cycle - 1) + ".html\">[-1]</a>&nbsp;&nbsp;");
                }
                else {
                    writer.write("<a>[-1]</a>&nbsp;&nbsp;");
                }
                if (this.cycle > 6) {
                    writer.write("<a href=\"cycle" + (this.cycle - 5) + ".html\">[-5]</a>&nbsp;&nbsp;");
                }
                else {
                    writer.write("<a>[-5]</a>&nbsp;&nbsp;");
                }
                writer.write("<a href=\"cycle" + (this.cycle + 1) + ".html\">[+1]</a>&nbsp;&nbsp;");
                writer.write("<a href=\"cycle" + (this.cycle + 5) + ".html\">[+5]</a>&nbsp;&nbsp;");
                writer.write("<pre>");
                this.print_regs(writer);
                writer.write("</pre><a>Reorder Buffer</a><pre>");
                writer.write(this.rob.toString() + "\n");
                this.print_operadores(writer);
                this.print_stores(writer);
                this.print_load(writer);
                writer.write("</pre>");
                writer.write(this.end());
                writer.close();
            }
            catch (IOException ex) {}
            this.diagrama.writeDiagrama(this.cycle);
        }
    }
    
    @Override
    public void writeResumen() {
        final String filename = "Resumen.html";
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write("<html>\n<head>\n<title>Resumen ejecuci\u00f3n</title>\n</head>\n<body>\n");
            writer.write("<a>Resumen ejecuci\u00f3n</a>&nbsp;&nbsp;<br>");
            writer.write("<a href=\"Resumen.html\">RESUMEN</a>&nbsp;&nbsp;");
            writer.write("<a href=\"cycle1.html\">ESTADO INICIAL</a>&nbsp;&nbsp;");
            final int end = (this.getCycle() < this.diagrama.getLimite()) ? this.getCycle() : this.diagrama.getLimite();
            writer.write("<a href=\"cycle" + end + ".html\"> ESTADO FINAL</a>&nbsp;&nbsp;");
            writer.write("<a href=\"diag1.html\">DIAGRAMA INICIAL</a>&nbsp;&nbsp;");
            writer.write("<a href=\"diag" + end + ".html\">DIAGRAMA FINAL</a>&nbsp;&nbsp;<br>");
            if (Tomasulo_conf.PRACTICA == 4) {
                writer.write("<a href=\"BTB1.html\">BTB INICIAL</a>&nbsp;&nbsp;");
                writer.write("<a href=\"BTB" + this.getCycle() + ".html\">BTB FINAL</a>&nbsp;&nbsp;<br>");
            }
            writer.write("<pre>");
            writer.write(this.toString() + "\n\n");
            writer.write("   Instrucciones emitidas: " + this.instrucciones + "\n");
            writer.write("    de las cuales\n");
            writer.write("                  Enteros: " + this.enteros + "\n");
            writer.write("           Punto flotante: " + this.pflotante + "\n");
            writer.write("           Almacenamiento: " + this.stores + "\n");
            writer.write("                    Carga: " + this.loads + "\n");
            writer.write("                   Saltos: " + this.saltos + "\n");
            writer.write("       Saltos Confirmados: " + (this.aciertos_predictor + this.fallos_predictor) + "\n");
            writer.write("  Predichos correctamente: " + this.aciertos_predictor + "\n");
            writer.write("Predichos incorrectamente: " + this.fallos_predictor + "\n");
            writer.write("           Tasa de fallos: " + this.fallos_predictor / (float)(this.aciertos_predictor + this.fallos_predictor) + "\n\n");
            writer.write("Instrucciones especuladas\n         incorrectamente: " + (this.instrucciones - this.instruccionesConfirmadas) + "\n\n");
            writer.write("        Ciclos ejecutados: " + this.getCycle() + "\n");
            writer.write("                      CPI: " + this.getCycle() / (float)this.instruccionesConfirmadas + "\n");
            writer.write("</pre>");
            writer.write("</body> \n </html>");
            writer.close();
        }
        catch (IOException ex) {}
    }
    
    @Override
    public String toString() {
        return "N\u00famero de entradas del Reorder Buffer: " + Tomasulo_conf.NUM_ENTRADAS_ROB + "\n\nEstaci\u00f3n\tCantidad\tLat. Operador\nEnteros    \t   " + Tomasulo_conf.NUM_EST_ENTEROS + "\t\t     " + Tomasulo_conf.LAT_ENTEROS + "\nP. flotante\t   " + Tomasulo_conf.NUM_EST_PF + "\t\t     " + Tomasulo_conf.LAT_PF + "\nCarga      \t   " + Tomasulo_conf.NUM_EST_LOAD + "\t\t     " + Tomasulo_conf.LAT_LOAD + "\nAlmacenamiento\t   " + Tomasulo_conf.NUM_EST_STORE + "\t\t     " + Tomasulo_conf.LAT_STORE + "\n";
    }
}
