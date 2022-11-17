

package mars.util;

import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import mars.Globals;
import java.io.BufferedReader;

public class SystemIO
{
    public static final int SYSCALL_BUFSIZE = 128;
    public static final int SYSCALL_MAXFILES = 32;
    public static String fileErrorString;
    private static final int O_RDONLY = 0;
    private static final int O_WRONLY = 1;
    private static final int O_RDWR = 2;
    private static final int O_APPEND = 8;
    private static final int O_CREAT = 512;
    private static final int O_TRUNC = 1024;
    private static final int O_EXCL = 2048;
    private static final int STDIN = 0;
    private static final int STDOUT = 1;
    private static final int STDERR = 2;
    private static BufferedReader inputReader;
    
    public static int readInteger(final int serviceNumber) {
        String input = "0";
        if (Globals.getGui() == null) {
            try {
                input = getInputReader().readLine();
            }
            catch (IOException ex) {}
        }
        else if (Globals.getSettings().getBooleanSetting(17)) {
            input = Globals.getGui().getMessagesPane().getInputString("Enter an integer value (syscall " + serviceNumber + ")");
        }
        else {
            input = Globals.getGui().getMessagesPane().getInputString(-1);
        }
        return new Integer(input.trim());
    }
    
    public static float readFloat(final int serviceNumber) {
        String input = "0";
        if (Globals.getGui() == null) {
            try {
                input = getInputReader().readLine();
            }
            catch (IOException ex) {}
        }
        else if (Globals.getSettings().getBooleanSetting(17)) {
            input = Globals.getGui().getMessagesPane().getInputString("Enter a float value (syscall " + serviceNumber + ")");
        }
        else {
            input = Globals.getGui().getMessagesPane().getInputString(-1);
        }
        return new Float(input.trim());
    }
    
    public static double readDouble(final int serviceNumber) {
        String input = "0";
        if (Globals.getGui() == null) {
            try {
                input = getInputReader().readLine();
            }
            catch (IOException ex) {}
        }
        else if (Globals.getSettings().getBooleanSetting(17)) {
            input = Globals.getGui().getMessagesPane().getInputString("Enter a double value (syscall " + serviceNumber + ")");
        }
        else {
            input = Globals.getGui().getMessagesPane().getInputString(-1);
        }
        return new Double(input.trim());
    }
    
    public static void printString(final String string) {
        if (Globals.getGui() == null) {
            System.out.print(string);
        }
        else {
            Globals.getGui().getMessagesPane().postRunMessage(string);
        }
    }
    
    public static String readString(final int serviceNumber, final int maxLength) {
        String input = "";
        if (Globals.getGui() == null) {
            try {
                input = getInputReader().readLine();
            }
            catch (IOException ex) {}
        }
        else if (Globals.getSettings().getBooleanSetting(17)) {
            input = Globals.getGui().getMessagesPane().getInputString("Enter a string of maximum length " + maxLength + " (syscall " + serviceNumber + ")");
        }
        else {
            input = Globals.getGui().getMessagesPane().getInputString(maxLength);
            if (input.endsWith("\n")) {
                input = input.substring(0, input.length() - 1);
            }
        }
        if (input.length() > maxLength) {
            return (maxLength <= 0) ? "" : input.substring(0, maxLength);
        }
        return input;
    }
    
    public static int readChar(final int serviceNumber) {
        String input = "0";
        int returnValue = 0;
        if (Globals.getGui() == null) {
            try {
                input = getInputReader().readLine();
            }
            catch (IOException ex) {}
        }
        else if (Globals.getSettings().getBooleanSetting(17)) {
            input = Globals.getGui().getMessagesPane().getInputString("Enter a character value (syscall " + serviceNumber + ")");
        }
        else {
            input = Globals.getGui().getMessagesPane().getInputString(1);
        }
        try {
            returnValue = input.charAt(0);
        }
        catch (IndexOutOfBoundsException e) {
            throw e;
        }
        return returnValue;
    }
    
    public static int writeToFile(final int fd, final byte[] myBuffer, final int lengthRequested) {
        if ((fd == 1 || fd == 2) && Globals.getGui() != null) {
            final String data = new String(myBuffer);
            Globals.getGui().getMessagesPane().postRunMessage(data);
            return data.length();
        }
        if (!fdInUse(fd, 1)) {
            SystemIO.fileErrorString = new String("File descriptor " + fd + " is not open for writing");
            return -1;
        }
        final OutputStream outputStream = (OutputStream)getStreamInUse(fd);
        try {
            for (int ii = 0; ii < lengthRequested; ++ii) {
                outputStream.write(myBuffer[ii]);
            }
            outputStream.flush();
        }
        catch (IOException e) {
            SystemIO.fileErrorString = new String("IO Exception on write of file with fd " + fd);
            return -1;
        }
        catch (IndexOutOfBoundsException e2) {
            SystemIO.fileErrorString = new String("IndexOutOfBoundsException on write of file with fd" + fd);
            return -1;
        }
        return lengthRequested;
    }
    
    public static int readFromFile(final int fd, final byte[] myBuffer, final int lengthRequested) {
        int retValue = -1;
        if (fd == 0 && Globals.getGui() != null) {
            final String input = Globals.getGui().getMessagesPane().getInputString(lengthRequested);
            final byte[] bytesRead = input.getBytes();
            for (int i = 0; i < myBuffer.length; ++i) {
                myBuffer[i] = (byte)((i < bytesRead.length) ? bytesRead[i] : 0);
            }
            return Math.min(myBuffer.length, bytesRead.length);
        }
        if (!fdInUse(fd, 0)) {
            SystemIO.fileErrorString = new String("File descriptor " + fd + " is not open for reading");
            return -1;
        }
        final InputStream InputStream = (InputStream)getStreamInUse(fd);
        try {
            retValue = InputStream.read(myBuffer, 0, lengthRequested);
            if (retValue == -1) {
                retValue = 0;
            }
        }
        catch (IOException e) {
            SystemIO.fileErrorString = new String("IO Exception on read of file with fd " + fd);
            return -1;
        }
        catch (IndexOutOfBoundsException e2) {
            SystemIO.fileErrorString = new String("IndexOutOfBoundsException on read of file with fd" + fd);
            return -1;
        }
        return retValue;
    }
    
    public static int openFile(final String filename, final int flags) {
        int retValue = -1;
        final char[] ch = { ' ' };
        final int fdToUse = retValue = nowOpening(filename, flags);
        if (fdToUse < 0) {
            return -1;
        }
        if (flags == 0) {
            try {
                final FileInputStream inputStream = new FileInputStream(filename);
                setStreamInUse(fdToUse, inputStream);
            }
            catch (FileNotFoundException e) {
                SystemIO.fileErrorString = new String("File " + filename + " not found, open for input.");
                retValue = -1;
            }
        }
        else if ((flags & 0x1) != 0x0) {
            try {
                final FileOutputStream outputStream = new FileOutputStream(filename, (flags & 0x8) != 0x0);
                setStreamInUse(fdToUse, outputStream);
            }
            catch (FileNotFoundException e) {
                SystemIO.fileErrorString = new String("File " + filename + " not found, open for output.");
                retValue = -1;
            }
        }
        return retValue;
    }
    
    public static void closeFile(final int fd) {
        close(fd);
    }
    
    public static void resetFiles() {
        resetFiles();
    }
    
    public static String getFileErrorMessage() {
        return SystemIO.fileErrorString;
    }
    
    private static BufferedReader getInputReader() {
        if (SystemIO.inputReader == null) {
            SystemIO.inputReader = new BufferedReader(new InputStreamReader(System.in));
        }
        return SystemIO.inputReader;
    }
    
    static {
        SystemIO.fileErrorString = new String("File operation OK");
        SystemIO.inputReader = null;
    }
    
    private static class FileIOData
    {
        private static String[] fileNames;
        private static int[] fileFlags;
        private static Object[] streams;
        
        private static void resetFiles() {
            for (int i = 0; i < 32; ++i) {
                close(i);
            }
            setupStdio();
        }
        
        private static void setupStdio() {
            FileIOData.fileNames[0] = "STDIN";
            FileIOData.fileNames[1] = "STDOUT";
            FileIOData.fileNames[2] = "STDERR";
            FileIOData.fileFlags[0] = 0;
            FileIOData.fileFlags[1] = 1;
            FileIOData.fileFlags[2] = 1;
            FileIOData.streams[0] = System.in;
            FileIOData.streams[1] = System.out;
            FileIOData.streams[2] = System.err;
            System.out.flush();
            System.err.flush();
        }
        
        private static void setStreamInUse(final int fd, final Object s) {
            FileIOData.streams[fd] = s;
        }
        
        private static Object getStreamInUse(final int fd) {
            return FileIOData.streams[fd];
        }
        
        private static boolean filenameInUse(final String requestedFilename) {
            for (int i = 0; i < 32; ++i) {
                if (FileIOData.fileNames[i] != null && FileIOData.fileNames[i].equals(requestedFilename)) {
                    return true;
                }
            }
            return false;
        }
        
        private static boolean fdInUse(final int fd, final int flag) {
            return fd >= 0 && fd < 32 && ((FileIOData.fileNames[fd] != null && FileIOData.fileFlags[fd] == 0 && flag == 0) || (FileIOData.fileNames[fd] != null && (FileIOData.fileFlags[fd] & flag & 0x1) == 0x1));
        }
        
        private static void close(final int fd) {
            if (fd <= 2 || fd >= 32) {
                return;
            }
            FileIOData.fileNames[fd] = null;
            if (FileIOData.streams[fd] != null) {
                final int keepFlag = FileIOData.fileFlags[fd];
                final Object keepStream = FileIOData.streams[fd];
                FileIOData.fileFlags[fd] = -1;
                FileIOData.streams[fd] = null;
                try {
                    if (keepFlag == 0) {
                        ((FileInputStream)keepStream).close();
                    }
                    else {
                        ((FileOutputStream)keepStream).close();
                    }
                }
                catch (IOException ex) {}
            }
            else {
                FileIOData.fileFlags[fd] = -1;
            }
        }
        
        private static int nowOpening(final String filename, final int flag) {
            int i = 0;
            if (filenameInUse(filename)) {
                SystemIO.fileErrorString = new String("File name " + filename + " is already open.");
                return -1;
            }
            if (flag != 0 && flag != 1 && flag != 9) {
                SystemIO.fileErrorString = new String("File name " + filename + " has unknown requested opening flag");
                return -1;
            }
            while (FileIOData.fileNames[i] != null && i < 32) {
                ++i;
            }
            if (i >= 32) {
                SystemIO.fileErrorString = new String("File name " + filename + " exceeds maximum open file limit of " + 32);
                return -1;
            }
            FileIOData.fileNames[i] = new String(filename);
            FileIOData.fileFlags[i] = flag;
            SystemIO.fileErrorString = new String("File operation OK");
            return i;
        }
        
        static {
            FileIOData.fileNames = new String[32];
            FileIOData.fileFlags = new int[32];
            FileIOData.streams = new Object[32];
        }
    }
}
