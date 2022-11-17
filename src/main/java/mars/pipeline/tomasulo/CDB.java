

package mars.pipeline.tomasulo;

public class CDB
{
    private int rob;
    private int value;
    private static CDB INSTANCE;
    
    public static CDB read() {
        if (CDB.INSTANCE == null) {
            CDB.INSTANCE = new CDB();
        }
        return CDB.INSTANCE;
    }
    
    private CDB() {
    }
    
    public int getRob() {
        return this.rob;
    }
    
    public int getValor() {
        return this.value;
    }
    
    public void update(final int rob, final int value) {
        this.rob = rob;
        this.value = value;
    }
    
    static {
        CDB.INSTANCE = null;
    }
}
