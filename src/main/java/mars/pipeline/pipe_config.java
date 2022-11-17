

package mars.pipeline;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import mars.pipeline.pipes.ForwardPipeline;
import mars.pipeline.pipes.IdealPipeline;
import mars.pipeline.pipes.MulticyclePipeline;
import mars.pipeline.pipes.StallPipeline;
import mars.pipeline.tomasulo.TomasuloP2;
import mars.pipeline.tomasulo.TomasuloP3;
import mars.pipeline.tomasulo.TomasuloP4;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class pipe_config
{
    private static final boolean enableTomasuloConfig = false;
    private static boolean concurso;
    private static boolean magicNumber;
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private Document documento;
    
    public static boolean isConcurso() {
        return pipe_config.concurso;
    }
    
    public static boolean isMagic() {
        return pipe_config.magicNumber;
    }
    
    public pipe_config(final String filename) {
        this.factory = DocumentBuilderFactory.newInstance();
        try {
            this.builder = this.factory.newDocumentBuilder();
            this.documento = this.builder.parse(new File(filename));
        }
        catch (Exception ex) {
            Logger.getLogger(pipe_config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean getBooleanOption(final Element e, final String option, final boolean defecto) throws ExceptionXML {
        final NodeList list = e.getElementsByTagName(option);
        if (list.getLength() == 0) {
            return defecto;
        }
        final String value = list.item(0).getTextContent().toLowerCase();
        if (value.equals("true")) {
            return true;
        }
        if (value.equals("false")) {
            return false;
        }
        throw new ExceptionXML(" Opci\u00f3n de " + option + " no v\u00e1lida (valor booleano distinto de true o false)");
    }
    
    private int getIntegerOption(final Element e, final String option, final int defecto) throws ExceptionXML {
        final NodeList list = e.getElementsByTagName(option);
        if (list.getLength() == 0) {
            return defecto;
        }
        final String str_value = list.item(0).getTextContent();
        final int value = Integer.parseInt(str_value);
        if (value < 1) {
            throw new ExceptionXML(option + " debe ser un entero mayor que cero");
        }
        return value;
    }
    
    private BranchPredictor.BranchPredictor_type getPredictor(final NodeList list) throws ExceptionXML {
        if (list.getLength() == 0) {
            return BranchPredictor.BranchPredictor_type.ideal;
        }
        final String option = list.item(0).getTextContent().toLowerCase();
        if (option.equals("ideal")) {
            return BranchPredictor.BranchPredictor_type.ideal;
        }
        if (option.equals("taken")) {
            return BranchPredictor.BranchPredictor_type.taken;
        }
        if (option.equals("nottaken")) {
            return BranchPredictor.BranchPredictor_type.notTaken;
        }
        if (option.equals("stop")) {
            return BranchPredictor.BranchPredictor_type.stop;
        }
        if (option.equals("delayed")) {
            return BranchPredictor.BranchPredictor_type.delayedBranch;
        }
        if (option.equals("btb")) {
            return BranchPredictor.BranchPredictor_type.btb;
        }
        throw new ExceptionXML("Predictor especificado no v\u00e1lido");
    }
    
    private Stage getBranches(final NodeList list) throws ExceptionXML {
        if (list.getLength() == 0) {
            return Stage.EX;
        }
        final String option = list.item(0).getTextContent().toLowerCase();
        if (option.equals("id")) {
            return Stage.ID;
        }
        if (option.equals("ex")) {
            return Stage.EX;
        }
        if (option.equals("mem")) {
            return Stage.MEM;
        }
        throw new ExceptionXML("Etapa de resoluci\u00f3n de saltos no v\u00e1lida");
    }
    
    private int getPracticaTomasulo(final Element e) throws ExceptionXML {
        if (!e.hasAttribute("practica")) {
            throw new ExceptionXML("Pr\u00e1ctica de Tomasulo no especificada");
        }
        final int practica = Integer.parseInt(e.getAttribute("practica"));
        if (practica < 2 || practica > 4) {
            throw new ExceptionXML("N\u00famero de pr\u00e1ctica de Tomasulo no v\u00e1lido");
        }
        return practica;
    }
    
    public Pipeline getPipeline() throws ExceptionXML {
        final NodeList pipes = this.documento.getElementsByTagName("pipeline");
        if (pipes.getLength() == 0) {
            System.out.println("No se ha configurado un pipeline. Seleccionando pipeline ideal por defecto");
            return new IdealPipeline();
        }
        if (pipes.getLength() > 1) {
            System.out.println("Se han especifido varios pipelines en un \u00fanico fichero xml. Se usar\u00e1 \u00fanicamente el primer pipeline especificado, el resto ser\u00e1n ignorados");
        }
        if (pipes.item(0).getNodeType() == 1) {
            final Element Epipe = (Element)pipes.item(0);
            if (Epipe.hasAttribute("tipo")) {
                final String tipo = Epipe.getAttribute("tipo");
                final String lowerCase = tipo.toLowerCase();
                int n = -1;
                switch (lowerCase.hashCode()) {
                    case 100048981: {
                        if (lowerCase.equals("ideal")) {
                            n = 0;
                            break;
                        }
                        break;
                    }
                    case -902286926: {
                        if (lowerCase.equals("simple")) {
                            n = 1;
                            break;
                        }
                        break;
                    }
                    case 1252972903: {
                        if (lowerCase.equals("multiciclo")) {
                            n = 2;
                            break;
                        }
                        break;
                    }
                    case -1050069708: {
                        if (lowerCase.equals("tomasulo")) {
                            n = 3;
                            break;
                        }
                        break;
                    }
                }
                Pipeline pipe = null;
                Label_0560: {
                    switch (n) {
                        case 0: {
                            pipe = new IdealPipeline();
                            break;
                        }
                        case 1: {
                            final boolean forward = this.getBooleanOption(Epipe, "forwarding", true);
                            final BranchPredictor.BranchPredictor_type bp = this.getPredictor(Epipe.getElementsByTagName("predictor"));
                            final Stage st = this.getBranches(Epipe.getElementsByTagName("branches"));
                            if (bp == BranchPredictor.BranchPredictor_type.delayedBranch && st != Stage.ID) {
                                throw new ExceptionXML(" Delayed branch solo es compatible con resoluci\u00f3n de saltos en etapa ID");
                            }
                            pipe = (forward ? new ForwardPipeline(st, bp) : new StallPipeline(st, bp));
                            break;
                        }
                        case 2: {
                            pipe = new MulticyclePipeline(this.getBranches(Epipe.getElementsByTagName("branches")), this.getPredictor(Epipe.getElementsByTagName("predictor")), this.getBooleanOption(Epipe, "mul_seg", true), this.getIntegerOption(Epipe, "mul_lat", 3), this.getBooleanOption(Epipe, "div_seg", false), this.getIntegerOption(Epipe, "div_lat", 6));
                            break;
                        }
                        case 3: {
                            final int practica = this.getPracticaTomasulo(Epipe);
                            switch (practica) {
                                case 2: {
                                    pipe = new TomasuloP2();
                                    break Label_0560;
                                }
                                case 3: {
                                    final BranchPredictor.BranchPredictor_type b_type = this.getPredictor(Epipe.getElementsByTagName("predictor"));
                                    if (b_type == BranchPredictor.BranchPredictor_type.btb) {
                                        pipe = new TomasuloP4();
                                        break Label_0560;
                                    }
                                    pipe = new TomasuloP3(this.getPredictor(Epipe.getElementsByTagName("predictor")));
                                    break Label_0560;
                                }
                                default: {
                                    throw new ExceptionXML("Pr\u00e1ctica Tomasulo desconocida. Valores v\u00e1lidos: 2 y 3");
                                }
                            }
                        }
                        default: {
                            throw new ExceptionXML("El tipo de pipeline especificado no es v\u00e1lido");
                        }
                    }
                }
                if (Epipe.hasAttribute("ventana")) {
                    pipe.configureVentana(Integer.parseInt(Epipe.getAttribute("ventana")));
                }
                if (Epipe.hasAttribute("limite")) {
                    pipe.configureLimite(Integer.parseInt(Epipe.getAttribute("limite")));
                }
                if (Epipe.hasAttribute("concurso")) {
                    pipe.configureLimite(0);
                    pipe_config.concurso = true;
                }
                if (Epipe.hasAttribute("magic")) {
                    pipe.configureLimite(0);
                    pipe_config.magicNumber = true;
                }
                return pipe;
            }
        }
        System.out.println("No se ha especificado el tipo del pipeline. Seleccionado pipeline ideal por defecto");
        return new IdealPipeline();
    }
    
    static {
        pipe_config.concurso = false;
        pipe_config.magicNumber = false;
    }
    
    public class ExceptionXML extends Exception
    {
        public ExceptionXML(final String msg) {
            super("Excepci\u00f3n configuraci\u00f3n XML del pipe: " + msg);
        }
    }
}
