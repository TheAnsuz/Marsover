

package mars.tools;

class CaptureModel
{
    private boolean enabled;
    
    public CaptureModel(final boolean set) {
        this.enabled = set;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public void setEnabled(final boolean set) {
        this.enabled = set;
    }
}
