package tc.oc.commons.core.random;

public class SaltedEntropy implements Entropy {

    private final Entropy source;
    private final long salt;

    public SaltedEntropy(Entropy source, int salt) {
        this.source = source;
        this.salt = new MutableEntropy(salt).randomLong();
    }

    @Override
    public long randomLong() {
        return salt ^ source.randomLong();
    }

    @Override
    public void advance() {
        source.advance();;
    }
}
