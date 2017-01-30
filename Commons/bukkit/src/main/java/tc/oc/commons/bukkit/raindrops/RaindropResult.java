package tc.oc.commons.bukkit.raindrops;

public abstract class RaindropResult implements Runnable {
    protected boolean success = false;

    public void setSuccess(boolean newValue) {
        this.success = newValue;
        run();
    }

    @Override
    public abstract void run();
}
