

package mars.simulator;

public class DelayedBranch
{
    private static final int CLEARED = 0;
    private static final int REGISTERED = 1;
    private static final int TRIGGERED = 2;
    private static int state;
    private static int branchTargetAddress;
    
    public static void register(final int targetAddress) {
        switch (DelayedBranch.state) {
            case 0: {
                DelayedBranch.branchTargetAddress = targetAddress;
            }
            case 1:
            case 2: {
                DelayedBranch.state = 1;
                break;
            }
        }
    }
    
    static void trigger() {
        switch (DelayedBranch.state) {
            case 1:
            case 2: {
                DelayedBranch.state = 2;
                break;
            }
        }
    }
    
    static void clear() {
        DelayedBranch.state = 0;
        DelayedBranch.branchTargetAddress = 0;
    }
    
    static boolean isRegistered() {
        return DelayedBranch.state == 1;
    }
    
    static boolean isTriggered() {
        return DelayedBranch.state == 2;
    }
    
    static int getBranchTargetAddress() {
        return DelayedBranch.branchTargetAddress;
    }
    
    static {
        DelayedBranch.state = 0;
        DelayedBranch.branchTargetAddress = 0;
    }
}
