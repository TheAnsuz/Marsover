

package mars.pipeline.pipes;

import java.io.IOException;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import mars.mips.hardware.AddressErrorException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mars.mips.hardware.Memory;
import mars.pipeline.BranchPredictor;
import mars.pipeline.Stage;
import mars.pipeline.StageRegisters;
import mars.pipeline.Pipeline;

public abstract class StaticPipe extends Pipeline
{
    public static final int IF = 0;
    public static final int ID = 1;
    public static final int EX = 2;
    public static final int MEM = 3;
    public static final int WB = 4;
    protected int other_stalls;
    protected int branch_stalls;
    protected final StageRegisters[] stage_regs;
    protected int inst_onflight;
    
    protected StaticPipe(final int numStageRegister, final Stage st, final BranchPredictor.BranchPredictor_type bp) {
        super(st, bp);
        this.other_stalls = 0;
        this.branch_stalls = 0;
        this.aciertos_predictor = 0;
        this.fallos_predictor = 0;
        this.instrucciones = 0;
        this.instruccionesConfirmadas = 0;
        this.inst_onflight = 0;
        this.stage_regs = new StageRegisters[numStageRegister];
        for (int i = 0; i < this.stage_regs.length; ++i) {
            this.stage_regs[i] = new StageRegisters();
        }
    }
    
    public int getOther_stalls() {
        return this.other_stalls;
    }
    
    public int getBranch_stalls() {
        return this.branch_stalls;
    }
    
    public int readAddres(final int address) {
        final Memory mem = Memory.getInstance();
        int instruction = 0;
        try {
            instruction = mem.getWordNoNotify(address);
        }
        catch (AddressErrorException ex) {
            Logger.getLogger(StaticPipe.class.getName()).log(Level.SEVERE, null, ex);
        }
        return instruction;
    }
    
    @Override
    public void writeResumen() {
        final String filename = "Resumen.html";
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write("<html>\n<head>\n<title>Resumen ejecuci\u00f3n</title>\n</head>\n<body>\n");
            writer.write("<a>Resumen ejecuci\u00f3n</a>&nbsp;&nbsp;<br>");
            writer.write("<a href=\"Resumen.html\">RESUMEN</a>&nbsp;&nbsp;");
            writer.write("<a href=\"diag1.html\">DIAGRAMA INICIAL</a>&nbsp;&nbsp;");
            final int end = (this.getCycle() < this.diagrama.getLimite()) ? this.getCycle() : this.diagrama.getLimite();
            writer.write("<a href=\"diag" + end + ".html\">DIAGRAMA FINAL</a>&nbsp;&nbsp;<br>");
            writer.write("<pre>");
            writer.write(this.toString() + "\n\n");
            writer.write("   Instrucciones emitidas: " + this.instrucciones + "\n");
            writer.write("    de las cuales\n");
            writer.write("   Emitidas correctamente: " + this.instruccionesConfirmadas + "\n");
            writer.write(" Emitidas incorrectamente: " + (this.instrucciones - this.instruccionesConfirmadas) + "\n\n");
            writer.write("       Saltos Confirmados: " + (this.aciertos_predictor + this.fallos_predictor) + "\n");
            writer.write("  Predichos correctamente: " + this.aciertos_predictor + "\n");
            writer.write("Predichos incorrectamente: " + this.fallos_predictor + "\n");
            writer.write("           Tasa de fallos: " + this.fallos_predictor / (float)(this.aciertos_predictor + this.fallos_predictor) + "\n\n");
            writer.write("        Ciclos ejecutados: " + (this.getCycle() - 1) + "\n");
            writer.write("      Ciclos perdidos por: \n\n");
            writer.write("     Fallos de predicci\u00f3n: " + this.branch_stalls + "\n");
            writer.write("         Riesgos de datos\n          o estructurales: " + this.other_stalls + "\n");
            writer.write("                      CPI: " + (this.getCycle() - 1) / (float)this.instruccionesConfirmadas + "\n");
            writer.write("</pre>");
            writer.write("</body> \n </html>");
            writer.close();
        }
        catch (IOException ex) {}
    }
}
