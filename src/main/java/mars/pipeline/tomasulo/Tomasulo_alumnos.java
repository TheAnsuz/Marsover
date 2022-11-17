

package mars.pipeline.tomasulo;

import mars.pipeline.tomasulo.estaciones_alumnos.EstacionPF_alumnos;
import mars.pipeline.tomasulo.estaciones_alumnos.EstacionINT_alumnos;
import mars.pipeline.tomasulo.estaciones_alumnos.EstacionSW_alumnos;
import mars.pipeline.tomasulo.estaciones_alumnos.EstacionLW_alumnos;
import mars.pipeline.BranchPredictor;

public abstract class Tomasulo_alumnos extends Tomasulo
{
    protected Tomasulo_alumnos(final BranchPredictor.BranchPredictor_type bp) {
        super(bp);
        for (int i = 0; i < Tomasulo_conf.NUM_EST_LOAD; ++i) {
            this.est_load[i] = new EstacionLW_alumnos();
            this.est_todas[i] = this.est_load[i];
        }
        for (int i = 0; i < Tomasulo_conf.NUM_EST_STORE; ++i) {
            this.est_store[i] = new EstacionSW_alumnos();
            this.est_todas[Tomasulo_conf.NUM_EST_LOAD + i] = this.est_store[i];
        }
        for (int i = 0; i < Tomasulo_conf.NUM_EST_ENTEROS; ++i) {
            this.est_enteros[i] = new EstacionINT_alumnos();
            this.est_todas[Tomasulo_conf.NUM_EST_LOAD + Tomasulo_conf.NUM_EST_STORE + i] = this.est_enteros[i];
        }
        for (int i = 0; i < Tomasulo_conf.NUM_EST_PF; ++i) {
            this.est_pf[i] = new EstacionPF_alumnos();
            this.est_todas[Tomasulo_conf.NUM_EST_LOAD + Tomasulo_conf.NUM_EST_STORE + Tomasulo_conf.NUM_EST_ENTEROS + i] = this.est_pf[i];
        }
    }
}
