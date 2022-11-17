

package mars.pipeline.DiagramaMulticiclo;

import mars.pipeline.Decode;
import mars.pipeline.tomasulo.Tomasulo_conf;

public class InstructionInfo
{
    private final boolean basicMIPS;
    private final int instruction;
    private final Instruction_type tipo;
    private int IF;
    private int Issue;
    private int AC;
    private int EX_init;
    private int EX_end;
    private int WB;
    private int Commit;
    private int inicio;
    private int fin;
    private boolean finalizada;
    private boolean descartada;
    
    public InstructionInfo(final int instruction, final int ciclo_lectura) {
        this.basicMIPS = false;
        this.instruction = instruction;
        if (Decode.isLoad(instruction)) {
            this.tipo = Instruction_type.carga;
        }
        else if (Decode.isStore(instruction)) {
            this.tipo = Instruction_type.almacenamiento;
        }
        else if (Decode.isMult(instruction) || Decode.isDiv(instruction)) {
            this.tipo = Instruction_type.pflotante;
        }
        else {
            this.tipo = Instruction_type.enteros;
        }
        this.finalizada = false;
        this.IF = ciclo_lectura;
        final int n = -1;
        this.fin = n;
        this.inicio = n;
        this.Commit = n;
        this.WB = n;
        this.EX_end = n;
        this.EX_init = n;
        this.AC = n;
        this.Issue = n;
    }
    
    public InstructionInfo(final int instruction, final int ciclo_lectura, final boolean tomasulo) {
        this.basicMIPS = !tomasulo;
        this.instruction = instruction;
        if (Decode.isLoad(instruction)) {
            this.tipo = Instruction_type.carga;
        }
        else if (Decode.isStore(instruction)) {
            this.tipo = Instruction_type.almacenamiento;
        }
        else if (Decode.isMult(instruction) || Decode.isDiv(instruction)) {
            this.tipo = Instruction_type.pflotante;
        }
        else {
            this.tipo = Instruction_type.enteros;
        }
        this.finalizada = false;
        this.inicio = ciclo_lectura;
        this.IF = ciclo_lectura;
        final int issue = -1;
        this.fin = issue;
        this.Commit = issue;
        this.WB = issue;
        this.EX_end = issue;
        this.EX_init = issue;
        this.AC = issue;
        this.Issue = issue;
    }
    
    public boolean isFinalizada() {
        return this.finalizada || this.instruction == 0;
    }
    
    public void finaliza(final int cycle) {
        this.fin = cycle;
        this.finalizada = true;
    }
    
    public Instruction_type getTipo() {
        return this.tipo;
    }
    
    public int getAC() {
        return this.AC;
    }
    
    public void setAC(final int AC) {
        this.AC = AC;
    }
    
    public int getEX_init() {
        return this.EX_init;
    }
    
    public int getEX() {
        return this.EX_init;
    }
    
    public void setEX_init(final int EX_init) {
        this.EX_init = EX_init;
    }
    
    public void setEX(final int EX) {
        this.EX_init = EX;
    }
    
    public int getEX_end() {
        return this.EX_end;
    }
    
    public int getMEM() {
        return this.EX_end;
    }
    
    public void setEX_end(final int EX_end) {
        this.EX_end = EX_end;
    }
    
    public void setMEM(final int MEM) {
        this.EX_end = MEM;
    }
    
    public int getIF() {
        return this.IF;
    }
    
    public void setIF(final int IF) {
        this.IF = IF;
    }
    
    public int getIssue() {
        return this.Issue;
    }
    
    public void setIssue(final int Issue) {
        this.Issue = Issue;
    }
    
    public int getWB() {
        return this.WB;
    }
    
    public void setWB(final int WB) {
        this.WB = WB;
    }
    
    public int getCommit() {
        return this.Commit;
    }
    
    public void setCommit(final int Commit) {
        this.Commit = Commit;
    }
    
    public void setDiscard(final int discard) {
        this.fin = discard;
        if (this.IF == discard) {
            this.IF = (this.basicMIPS ? (this.IF - 1) : -1);
        }
        if (this.Issue == discard) {
            this.Issue = (this.basicMIPS ? (this.Issue - 1) : -1);
        }
        this.finalizada = true;
        this.descartada = true;
    }
    
    public int getDiscard() {
        return this.descartada ? this.fin : -1;
    }
    
    private String tomasuloString(final int primer_ciclo, final int ultimo_ciclo) {
        if (this.instruction == 0) {
            return "";
        }
        String str = Decode.toString(this.instruction);
        int ciclo_actual;
        for (ciclo_actual = primer_ciclo; ciclo_actual < this.IF; ++ciclo_actual) {
            str += "|    ";
        }
        if (ciclo_actual == this.IF) {
            str += "| IF ";
            ++ciclo_actual;
        }
        if (this.Issue != -1) {
            while (ciclo_actual < this.Issue) {
                str += "|    ";
                ++ciclo_actual;
            }
            if (ciclo_actual == this.Issue) {
                str += "| Is ";
                ++ciclo_actual;
            }
        }
        if (this.tipo == Instruction_type.almacenamiento && this.Commit != -1 && Tomasulo_conf.PRACTICA != 2) {
            while (ciclo_actual < this.Commit) {
                str += "|    ";
                ++ciclo_actual;
            }
            if (ciclo_actual == this.Commit) {
                str += "| CO ";
                ++ciclo_actual;
            }
        }
        if (this.AC != -1) {
            while (ciclo_actual < this.AC) {
                str += "|    ";
                ++ciclo_actual;
            }
            if (ciclo_actual == this.AC) {
                str += "| AC ";
                ++ciclo_actual;
            }
        }
        if (this.EX_init != -1) {
            while (ciclo_actual < this.EX_init) {
                str += "|    ";
                ++ciclo_actual;
            }
            while (ciclo_actual <= this.EX_end) {
                final int aux = ciclo_actual - this.EX_init + 1;
                switch (this.tipo) {
                    case enteros: {
                        str += "| EX ";
                        break;
                    }
                    case pflotante: {
                        str = str + "| X" + aux + " ";
                        break;
                    }
                    case carga:
                    case almacenamiento: {
                        str = str + "| M" + aux + " ";
                        break;
                    }
                }
                ++ciclo_actual;
            }
        }
        if (this.WB != -1) {
            while (ciclo_actual < this.WB) {
                str += "|    ";
                ++ciclo_actual;
            }
            if (ciclo_actual == this.WB) {
                str += "| WB ";
                ++ciclo_actual;
            }
        }
        if (Tomasulo_conf.PRACTICA == 2 || (this.tipo != Instruction_type.almacenamiento && this.Commit != -1)) {
            while (ciclo_actual < this.Commit) {
                str += "|    ";
                ++ciclo_actual;
            }
            if (ciclo_actual == this.Commit) {
                str += "| CO ";
                ++ciclo_actual;
            }
        }
        if (this.descartada) {
            while (ciclo_actual < this.fin) {
                str += "|    ";
                ++ciclo_actual;
            }
            if (ciclo_actual == this.fin) {
                str += "|-xx-";
                ++ciclo_actual;
            }
        }
        while (ciclo_actual <= ultimo_ciclo) {
            str += "|    ";
            ++ciclo_actual;
        }
        str += "|\n";
        return str;
    }
    
    private String basicString(final int primer_ciclo, final int ultimo_ciclo) {
        if (this.instruction == 0) {
            return "";
        }
        String str = Decode.toString(this.instruction);
        final int tipo = Decode.isDiv(this.instruction) ? 2 : (Decode.isMult(this.instruction) ? 1 : 0);
        int ciclo_actual;
        for (ciclo_actual = primer_ciclo; ciclo_actual < this.inicio; ++ciclo_actual) {
            str += "|    ";
        }
        while (ciclo_actual <= this.IF) {
            str += "| IF ";
            ++ciclo_actual;
        }
        if (this.Issue != -1) {
            while (ciclo_actual <= this.Issue) {
                str += "| ID ";
                ++ciclo_actual;
            }
        }
        if (this.EX_init != -1 && !this.descartada) {
            switch (tipo) {
                case 0:
                    if (ciclo_actual == this.EX_init) {
                        str += "| EX ";
                        ++ciclo_actual;
                    }   break;
                case 1:{
                    int i = ciclo_actual - this.Issue;
                    while (ciclo_actual <= this.EX_init) {
                        str = str + "| X" + i + " ";
                        ++i;
                        ++ciclo_actual;
                    }       break;
                    }
                case 2:{
                    int i = ciclo_actual - this.Issue;
                    while (ciclo_actual <= this.EX_init) {
                        str = str + "| D" + i + " ";
                        ++i;
                        ++ciclo_actual;
                    }       break;
                    }
                default:
                    break;
            }
        }
        if (ciclo_actual == this.EX_end) {
            str += "| ME ";
            ++ciclo_actual;
        }
        if (ciclo_actual == this.WB) {
            str += "| WB ";
            ++ciclo_actual;
        }
        if (this.descartada) {
            while (ciclo_actual < this.fin) {
                str += "|    ";
                ++ciclo_actual;
            }
            if (ciclo_actual == this.fin) {
                str += "|-xx-";
                ++ciclo_actual;
            }
        }
        while (ciclo_actual <= ultimo_ciclo) {
            str += "|    ";
            ++ciclo_actual;
        }
        str += "|\n";
        return str;
    }
    
    public String toString(final int primer_ciclo, final int ultimo_ciclo) {
        return this.basicMIPS ? this.basicString(primer_ciclo, ultimo_ciclo) : this.tomasuloString(primer_ciclo, ultimo_ciclo);
    }
    
    public enum Instruction_type
    {
        enteros, 
        pflotante, 
        carga, 
        almacenamiento;
    }
}
