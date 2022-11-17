

package mars.pipeline;

import mars.pipeline.DiagramaMulticiclo.DiagramaMulticiclo;
import mars.pipeline.predictors.IdealPredictor;
import mars.pipeline.predictors.NotTakenPredictor;
import mars.pipeline.predictors.StopPredictor;
import mars.pipeline.predictors.TakenPredictor;
import mars.pipeline.tomasulo.Tomasulo;

public abstract class Pipeline
{
    protected Stage BranchResolve;
    protected BranchPredictor.BranchPredictor_type preditor_type;
    protected BranchPredictor bp;
    protected int cycle;
    protected int instrucciones;
    protected int instruccionesConfirmadas;
    protected int aciertos_predictor;
    protected int fallos_predictor;
    protected int limite;
    protected final DiagramaMulticiclo diagrama;
    
    public void configureLimite(final int limite) {
        this.diagrama.setLimite(limite);
        this.limite = limite;
    }
    
    public void configureVentana(final int ventana) {
        this.diagrama.setVentana(ventana);
    }
    
    public int getCycle() {
        return this.cycle;
    }
    
    public int getInstrucciones() {
        return this.instrucciones;
    }
    
    public int getNumInstruccionesConfirmadas() {
        return this.instruccionesConfirmadas;
    }
    
    public Stage getBranchResolve() {
        return this.BranchResolve;
    }
    
    public BranchPredictor.BranchPredictor_type getPreditor_type() {
        return this.preditor_type;
    }
    
    public planificacion_t getPlanificacion() {
        return (this instanceof Tomasulo) ? planificacion_t.dinamica : planificacion_t.estatica;
    }
    
    protected Pipeline(final Stage st, final BranchPredictor.BranchPredictor_type bp) {
        this.cycle = 0;
        this.preditor_type = bp;
        switch (bp) {
            case ideal:
            case delayedBranch: {
                this.bp = new IdealPredictor();
                break;
            }
            case notTaken: {
                this.bp = new NotTakenPredictor();
                break;
            }
            case taken: {
                this.bp = new TakenPredictor();
                break;
            }
            case stop: {
                this.bp = new StopPredictor();
                break;
            }
        }
        this.BranchResolve = st;
        this.diagrama = new DiagramaMulticiclo(100, 25);
    }
    
    public abstract void UpdatePipeline(final int p0, final int p1);
    
    public abstract void finalizar();
    
    public abstract void writeResumen();
    
    @Override
    public abstract String toString();
    
    public enum planificacion_t
    {
        estatica, 
        dinamica;
    }
    
    public enum Pipeline_type
    {
        ideal("Ideal"), 
        nonforward("Non Forward"), 
        forward("Full forward"), 
        mult("Full forward and multicicle ops"), 
        tomasuloP2("TomasuloP2"), 
        tomasuloP3("TomasuloP3"), 
        tomasuloP4("TomasuloP4");
        
        private final String name;
        
        private Pipeline_type(final String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return this.name;
        }
    }
}
