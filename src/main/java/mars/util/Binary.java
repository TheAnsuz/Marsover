

package mars.util;

import java.util.Arrays;
import mars.Globals;

public class Binary
{
    private static char[] chars;
    private static final long UNSIGNED_BASE = 4294967296L;
    
    public static String intToBinaryString(final int value, final int length) {
        final char[] result = new char[length];
        int index = length - 1;
        for (int i = 0; i < length; ++i) {
            result[index] = ((bitValue(value, i) == 1) ? '1' : '0');
            --index;
        }
        return new String(result);
    }
    
    public static String intToBinaryString(final int value) {
        return intToBinaryString(value, 32);
    }
    
    public static String longToBinaryString(final long value, final int length) {
        final char[] result = new char[length];
        int index = length - 1;
        for (int i = 0; i < length; ++i) {
            result[index] = ((bitValue(value, i) == 1) ? '1' : '0');
            --index;
        }
        return new String(result);
    }
    
    public static String longToBinaryString(final long value) {
        return longToBinaryString(value, 64);
    }
    
    public static int binaryStringToInt(final String value) {
        int result = value.charAt(0) - '0';
        for (int i = 1; i < value.length(); ++i) {
            result = (result << 1 | value.charAt(i) - '0');
        }
        return result;
    }
    
    public static long binaryStringToLong(final String value) {
        long result = value.charAt(0) - '0';
        for (int i = 1; i < value.length(); ++i) {
            result = (result << 1 | (long)(value.charAt(i) - '0'));
        }
        return result;
    }
    
    public static String binaryStringToHexString(final String value) {
        final int digits = (value.length() + 3) / 4;
        final char[] hexChars = new char[digits + 2];
        hexChars[0] = '0';
        hexChars[1] = 'x';
        int position = value.length() - 1;
        for (int digs = 0; digs < digits; ++digs) {
            int result = 0;
            int pow = 1;
            for (int rep = 0; rep < 4 && position >= 0; --position, ++rep) {
                if (value.charAt(position) == '1') {
                    result += pow;
                }
                pow *= 2;
            }
            hexChars[digits - digs + 1] = Binary.chars[result];
        }
        return new String(hexChars);
    }
    
    public static String hexStringToBinaryString(String value) {
        String result = "";
        if (value.indexOf("0x") == 0 || value.indexOf("0X") == 0) {
            value = value.substring(2);
        }
        for (int digs = 0; digs < value.length(); ++digs) {
            switch (value.charAt(digs)) {
                case '0': {
                    result += "0000";
                    break;
                }
                case '1': {
                    result += "0001";
                    break;
                }
                case '2': {
                    result += "0010";
                    break;
                }
                case '3': {
                    result += "0011";
                    break;
                }
                case '4': {
                    result += "0100";
                    break;
                }
                case '5': {
                    result += "0101";
                    break;
                }
                case '6': {
                    result += "0110";
                    break;
                }
                case '7': {
                    result += "0111";
                    break;
                }
                case '8': {
                    result += "1000";
                    break;
                }
                case '9': {
                    result += "1001";
                    break;
                }
                case 'A':
                case 'a': {
                    result += "1010";
                    break;
                }
                case 'B':
                case 'b': {
                    result += "1011";
                    break;
                }
                case 'C':
                case 'c': {
                    result += "1100";
                    break;
                }
                case 'D':
                case 'd': {
                    result += "1101";
                    break;
                }
                case 'E':
                case 'e': {
                    result += "1110";
                    break;
                }
                case 'F':
                case 'f': {
                    result += "1111";
                    break;
                }
            }
        }
        return result;
    }
    
    public static char binaryStringToHexDigit(final String value) {
        if (value.length() > 4) {
            return '0';
        }
        int result = 0;
        int pow = 1;
        for (int i = value.length() - 1; i >= 0; --i) {
            if (value.charAt(i) == '1') {
                result += pow;
            }
            pow *= 2;
        }
        return Binary.chars[result];
    }
    
    public static String intToHexString(final int d) {
        final String leadingZero = "0";
        final String leadingX = "0x";
        String t;
        for (t = Integer.toHexString(d); t.length() < 8; t = leadingZero.concat(t)) {}
        t = leadingX.concat(t);
        return t;
    }
    
    public static String intToHalfHexString(final int d) {
        final String leadingZero = "0";
        final String leadingX = "0x";
        String t = Integer.toHexString(d);
        if (t.length() > 4) {
            t = t.substring(t.length() - 4, t.length());
        }
        while (t.length() < 4) {
            t = leadingZero.concat(t);
        }
        t = leadingX.concat(t);
        return t;
    }
    
    public static String longToHexString(final long value) {
        return binaryStringToHexString(longToBinaryString(value));
    }
    
    public static String unsignedIntToIntString(final int d) {
        return (d >= 0) ? Integer.toString(d) : Long.toString(4294967296L + d);
    }
    
    public static String intToAscii(final int d) {
        final StringBuilder result = new StringBuilder(8);
        for (int i = 3; i >= 0; --i) {
            final int byteValue = getByte(d, i);
            result.append((byteValue < Globals.ASCII_TABLE.length) ? Globals.ASCII_TABLE[byteValue] : Globals.ASCII_NON_PRINT);
        }
        return result.toString();
    }
    
    public static int stringToInt(final String s) throws NumberFormatException {
        String work = s;
        int result = 0;
        try {
            result = Integer.decode(s);
        }
        catch (NumberFormatException nfe) {
            work = work.toLowerCase();
            if (work.length() == 10 && work.startsWith("0x")) {
                String bitString = "";
                for (int i = 2; i < 10; ++i) {
                    final int index = Arrays.binarySearch(Binary.chars, work.charAt(i));
                    if (index < 0) {
                        throw new NumberFormatException();
                    }
                    bitString += intToBinaryString(index, 4);
                }
                result = binaryStringToInt(bitString);
            }
            else {
                if (work.startsWith("0x")) {
                    throw new NumberFormatException();
                }
                result = 0;
                for (int j = 0; j < work.length(); ++j) {
                    final char c = work.charAt(j);
                    if ('0' > c || c > '9') {
                        throw new NumberFormatException();
                    }
                    result *= 10;
                    result += c - '0';
                }
            }
        }
        return result;
    }
    
    public static long stringToLong(final String s) throws NumberFormatException {
        String work = s;
        long result = 0L;
        try {
            result = Long.decode(s);
        }
        catch (NumberFormatException nfe) {
            work = work.toLowerCase();
            if (work.length() != 18 || !work.startsWith("0x")) {
                throw new NumberFormatException();
            }
            String bitString = "";
            for (int i = 2; i < 18; ++i) {
                final int index = Arrays.binarySearch(Binary.chars, work.charAt(i));
                if (index < 0) {
                    throw new NumberFormatException();
                }
                bitString += intToBinaryString(index, 4);
            }
            result = binaryStringToLong(bitString);
        }
        return result;
    }
    
    public static int highOrderLongToInt(final long longValue) {
        return (int)(longValue >> 32);
    }
    
    public static int lowOrderLongToInt(final long longValue) {
        return (int)(longValue << 32 >> 32);
    }
    
    public static long twoIntsToLong(final int highOrder, final int lowOrder) {
        return (long)highOrder << 32 | ((long)lowOrder & 0xFFFFFFFFL);
    }
    
    public static int bitValue(final int value, final int bit) {
        return 0x1 & value >> bit;
    }
    
    public static int bitValue(final long value, final int bit) {
        return (int)(0x1L & value >> bit);
    }
    
    public static int setBit(final int value, final int bit) {
        return value | 1 << bit;
    }
    
    public static int clearBit(final int value, final int bit) {
        return value & ~(1 << bit);
    }
    
    public static int setByte(final int value, final int bite, final int replace) {
        return (value & ~(255 << (bite << 3))) | (replace & 0xFF) << (bite << 3);
    }
    
    public static int getByte(final int value, final int bite) {
        return value << (3 - bite << 3) >>> 24;
    }
    
    public static boolean isHex(final String v) {
        try {
            try {
                stringToInt(v);
            }
            catch (NumberFormatException nfe) {
                try {
                    stringToLong(v);
                }
                catch (NumberFormatException e) {
                    return false;
                }
            }
            if (v.charAt(0) == '-' && v.charAt(1) == '0' && Character.toUpperCase(v.charAt(1)) == 'X') {
                return true;
            }
            if (v.charAt(0) == '0' && Character.toUpperCase(v.charAt(1)) == 'X') {
                return true;
            }
        }
        catch (StringIndexOutOfBoundsException e2) {
            return false;
        }
        return false;
    }
    
    public static boolean isOctal(final String v) {
        try {
            final int dontCare = stringToInt(v);
            if (isHex(v)) {
                return false;
            }
            if (v.charAt(0) == '-' && v.charAt(1) == '0' && v.length() > 1) {
                return true;
            }
            if (v.charAt(0) == '0' && v.length() > 1) {
                return true;
            }
        }
        catch (StringIndexOutOfBoundsException e) {
            return false;
        }
        catch (NumberFormatException e2) {
            return false;
        }
        return false;
    }
    
    static {
        Binary.chars = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    }
}
