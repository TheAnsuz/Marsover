

package mars.assembler;

public final class DataTypes
{
    public static final int DOUBLE_SIZE = 8;
    public static final int FLOAT_SIZE = 4;
    public static final int WORD_SIZE = 4;
    public static final int HALF_SIZE = 2;
    public static final int BYTE_SIZE = 1;
    public static final int CHAR_SIZE = 1;
    public static final int MAX_WORD_VALUE = Integer.MAX_VALUE;
    public static final int MIN_WORD_VALUE = Integer.MIN_VALUE;
    public static final int MAX_HALF_VALUE = 32767;
    public static final int MIN_HALF_VALUE = -32768;
    public static final int MAX_UHALF_VALUE = 65535;
    public static final int MIN_UHALF_VALUE = 0;
    public static final int MAX_BYTE_VALUE = 127;
    public static final int MIN_BYTE_VALUE = -128;
    public static final double MAX_FLOAT_VALUE = 3.4028234663852886E38;
    public static final double LOW_FLOAT_VALUE = -3.4028234663852886E38;
    public static final double MAX_DOUBLE_VALUE = Double.MAX_VALUE;
    public static final double LOW_DOUBLE_VALUE = -1.7976931348623157E308;
    
    public static int getLengthInBytes(final Directives direct) {
        if (direct == Directives.FLOAT) {
            return 4;
        }
        if (direct == Directives.DOUBLE) {
            return 8;
        }
        if (direct == Directives.WORD) {
            return 4;
        }
        if (direct == Directives.HALF) {
            return 2;
        }
        if (direct == Directives.BYTE) {
            return 1;
        }
        return 0;
    }
    
    public static boolean outOfRange(final Directives direct, final int value) {
        return (direct == Directives.HALF && (value < -32768 || value > 32767)) || (direct == Directives.BYTE && (value < -128 || value > 127));
    }
    
    public static boolean outOfRange(final Directives direct, final double value) {
        return direct == Directives.FLOAT && (value < -3.4028234663852886E38 || value > 3.4028234663852886E38);
    }
}
