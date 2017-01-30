package tc.oc.commons.core.chat;

public class MultiAudience extends AbstractMultiAudience {

    private final Iterable<? extends Audience> audiences;

    public MultiAudience(Iterable<? extends Audience> audiences) {
        this.audiences = audiences;
    }

    @Override
    protected Iterable<? extends Audience> getAudiences() {
        return audiences;
    }
}
