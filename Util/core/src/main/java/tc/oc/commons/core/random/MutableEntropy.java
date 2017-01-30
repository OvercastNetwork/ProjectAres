package tc.oc.commons.core.random;

public class MutableEntropy implements Entropy {

    private static final long MULTIPLIER = 0x5DEECE66DL;
    private static final long INCREMENT = 0xBL;
    private static final long MASK = (1L << 48) - 1;

    private static long initialScramble(long seed) {
        return (seed ^ MULTIPLIER) & MASK;
    }

    private static long nextSeed(long seed) {
        return (seed * MULTIPLIER + INCREMENT) & MASK;
    }

    private long seed;
    private long randomLong;

    public MutableEntropy() {
        this(System.nanoTime());
    }

    public MutableEntropy(long seed) {
        this.seed = initialScramble(seed);
        advance();
    }

    @Override
    public long randomLong() {
        return randomLong;
    }

    @Override
    public void advance() {
        // Since we only get 48 usable random bits per iteration,
        // we iterate twice to generate a single long
        seed = nextSeed(seed);
        randomLong = (seed & ~((1 << 16) - 1)) << 16;
        seed = nextSeed(seed);
        randomLong = randomLong + (seed >> 16);
    }
}
