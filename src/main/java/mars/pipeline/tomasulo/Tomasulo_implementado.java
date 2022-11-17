

package mars.pipeline.tomasulo;

import mars.pipeline.tomasulo.estaciones.EstacionPF;
import mars.pipeline.tomasulo.estaciones.EstacionINT;
import mars.pipeline.tomasulo.estaciones.EstacionSW;
import mars.pipeline.tomasulo.estaciones.EstacionLW;
import mars.pipeline.BranchPredictor;

public abstract class Tomasulo_implementado extends Tomasulo
{
    protected Tomasulo_implementado(final BranchPredictor.BranchPredictor_type bp) {
        super(bp);
        for (int i = 0; i < Tomasulo_conf.NUM_EST_LOAD; ++i) {
            this.est_load[i] = new EstacionLW();
            this.est_todas[i] = this.est_load[i];
        }
        for (int i = 0; i < Tomasulo_conf.NUM_EST_STORE; ++i) {
            this.est_store[i] = new EstacionSW();
            this.est_todas[Tomasulo_conf.NUM_EST_LOAD + i] = this.est_store[i];
        }
        for (int i = 0; i < Tomasulo_conf.NUM_EST_ENTEROS; ++i) {
            this.est_enteros[i] = new EstacionINT();
            this.est_todas[Tomasulo_conf.NUM_EST_LOAD + Tomasulo_conf.NUM_EST_STORE + i] = this.est_enteros[i];
        }
        for (int i = 0; i < Tomasulo_conf.NUM_EST_PF; ++i) {
            this.est_pf[i] = new EstacionPF();
            this.est_todas[Tomasulo_conf.NUM_EST_LOAD + Tomasulo_conf.NUM_EST_STORE + Tomasulo_conf.NUM_EST_ENTEROS + i] = this.est_pf[i];
        }
    }
}
