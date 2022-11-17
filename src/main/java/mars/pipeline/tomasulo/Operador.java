

package mars.pipeline.tomasulo;

import mars.pipeline.tomasulo.estaciones.EstacionSW;

public class Operador
{
    private int estacion_ex;
    private final Estacion[] estaciones;
    
    public Operador(final Estacion[] estaciones) {
        this.estaciones = estaciones;
        this.estacion_ex = -1;
    }
    
    public int obtenerEstacion(final int cycle) {
        if (this.estacion_ex == -1) {
            int antigua = Integer.MAX_VALUE;
            for (int i = 0; i < this.estaciones.length; ++i) {
                final Estacion check = this.estaciones[i];
                boolean sw_confirmed = true;
                if (check instanceof EstacionSW) {
                    final boolean doing_commit_in_this_cycle = check.ins_info == null || cycle == check.ins_info.getCommit();
                    if (!check.confirmado || doing_commit_in_this_cycle) {
                        sw_confirmed = false;
                    }
                }
                if (!check.EstaLibre() && check.estado == 0 && check.getQj() == -1 && check.getQk() == -1 && check.getMarca_tiempo() <= antigua && sw_confirmed) {
                    this.estacion_ex = i;
                    antigua = check.getMarca_tiempo();
                }
            }
        }
        return this.estacion_ex;
    }
    
    public void LiberarOperador() {
        this.estacion_ex = -1;
    }
}
