package tc.oc.pgm.time;

public interface TickClock {

    TickTime now();

    default long tick() {
        return now().tick;
    }
}
