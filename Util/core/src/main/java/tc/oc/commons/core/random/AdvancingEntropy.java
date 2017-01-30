package tc.oc.commons.core.random;

public class AdvancingEntropy extends MutableEntropy {

    public AdvancingEntropy() {}

    public AdvancingEntropy(long seed) {
        super(seed);
    }

    @Override
    public long randomLong() {
        final long n = super.randomLong();
        advance();
        return n;
    }
}
