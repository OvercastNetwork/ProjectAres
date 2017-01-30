package tc.oc.pgm.utils;

public abstract class Strings {
    public static String progressPercentage(double completion) {
        // This is probably the simplest way to get an accurate percentage while never rounding up to 100%
        int percent = (int) Math.round(100.0 * completion);
        if(percent == 100 && completion < 1d) {
            return "99%";
        } else {
            return percent + "%";
        }
    }
}
