

package mars.mips.instructions;

import mars.mips.instructions.syscalls.SyscallNumberOverride;
import mars.Globals;
import mars.mips.instructions.syscalls.Syscall;
import java.util.HashMap;
import mars.util.FilenameFinder;
import java.util.ArrayList;

class SyscallLoader
{
    private static final String CLASS_PREFIX = "mars.mips.instructions.syscalls.";
    private static final String SYSCALLS_DIRECTORY_PATH = "mars/mips/instructions/syscalls";
    private static final String SYSCALL_INTERFACE = "Syscall.class";
    private static final String SYSCALL_ABSTRACT = "AbstractSyscall.class";
    private static final String CLASS_EXTENSION = "class";
    private ArrayList<Syscall> syscallList;
    
    void loadSyscalls() {
        this.syscallList = new ArrayList();
        final ArrayList<String> candidates = FilenameFinder.getFilenameList(this.getClass().getClassLoader(), "mars/mips/instructions/syscalls", "class");
        final HashMap syscalls = new HashMap();
        for (int i = 0; i < candidates.size(); ++i) {
            final String file = candidates.get(i);
            if (!syscalls.containsKey(file)) {
                syscalls.put(file, file);
                if (!file.equals("Syscall.class") && !file.equals("AbstractSyscall.class")) {
                    try {
                        final String syscallClassName = "mars.mips.instructions.syscalls." + file.substring(0, file.indexOf("class") - 1);
                        final Class clas = Class.forName(syscallClassName);
                        if (Syscall.class.isAssignableFrom(clas)) {
                            final Syscall syscall = (Syscall)clas.newInstance();
                            if (this.findSyscall(syscall.getNumber()) != null) {
                                throw new Exception("Duplicate service number: " + syscall.getNumber() + " already registered to " + this.findSyscall(syscall.getNumber()).getName());
                            }
                            this.syscallList.add(syscall);
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Error instantiating Syscall from file " + file + ": " + e);
                        System.exit(0);
                    }
                }
            }
        }
        this.syscallList = this.processSyscallNumberOverrides(this.syscallList);
    }
    
    private ArrayList processSyscallNumberOverrides(final ArrayList<Syscall> syscallList) {
        final ArrayList<SyscallNumberOverride> overrides = new Globals().getSyscallOverrides();
        for (int index = 0; index < overrides.size(); ++index) {
            final SyscallNumberOverride override = overrides.get(index);
            boolean match = false;
            for (int i = 0; i < syscallList.size(); ++i) {
                final Syscall syscall = syscallList.get(i);
                if (override.getName().equals(syscall.getName())) {
                    syscall.setNumber(override.getNumber());
                    match = true;
                }
            }
            if (!match) {
                System.out.println("Error: syscall name '" + override.getName() + "' in config file does not match any name in syscall list");
                System.exit(0);
            }
        }
        boolean duplicates = false;
        for (int j = 0; j < syscallList.size(); ++j) {
            final Syscall syscallA = syscallList.get(j);
            for (int k = j + 1; k < syscallList.size(); ++k) {
                final Syscall syscallB = syscallList.get(k);
                if (syscallA.getNumber() == syscallB.getNumber()) {
                    System.out.println("Error: syscalls " + syscallA.getName() + " and " + syscallB.getName() + " are both assigned same number " + syscallA.getNumber());
                    duplicates = true;
                }
            }
        }
        if (duplicates) {
            System.exit(0);
        }
        return syscallList;
    }
    
    Syscall findSyscall(final int number) {
        Syscall match = null;
        if (this.syscallList == null) {
            this.loadSyscalls();
        }
        for (int index = 0; index < this.syscallList.size(); ++index) {
            final Syscall service = this.syscallList.get(index);
            if (service.getNumber() == number) {
                match = service;
            }
        }
        return match;
    }
}
