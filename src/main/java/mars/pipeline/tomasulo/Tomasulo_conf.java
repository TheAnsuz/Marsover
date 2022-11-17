

package mars.pipeline.tomasulo;

public class Tomasulo_conf
{
    public static final int MARCA_NULA = -1;
    public static int NUM_EST_ENTEROS;
    public static int LAT_ENTEROS;
    public static int NUM_EST_PF;
    public static int LAT_PF;
    public static int NUM_EST_LOAD;
    public static int LAT_LOAD;
    public static int NUM_EST_STORE;
    public static int LAT_STORE;
    public static int NUM_ENTRADAS_ROB;
    public static int PRACTICA;
    public static int NUM_BTB_ENTRIES;
    public static int MAX_CYCLE_DUMPING_BTB;
    
    static {
        Tomasulo_conf.NUM_EST_ENTEROS = 4;
        Tomasulo_conf.LAT_ENTEROS = 1;
        Tomasulo_conf.NUM_EST_PF = 2;
        Tomasulo_conf.LAT_PF = 6;
        Tomasulo_conf.NUM_EST_LOAD = 3;
        Tomasulo_conf.LAT_LOAD = 3;
        Tomasulo_conf.NUM_EST_STORE = 3;
        Tomasulo_conf.LAT_STORE = 3;
        Tomasulo_conf.NUM_ENTRADAS_ROB = 20;
        Tomasulo_conf.NUM_BTB_ENTRIES = 2;
        Tomasulo_conf.MAX_CYCLE_DUMPING_BTB = 10000;
    }
}
