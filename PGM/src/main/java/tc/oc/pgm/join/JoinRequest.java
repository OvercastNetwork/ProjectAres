package tc.oc.pgm.join;

import java.util.Optional;
import javax.annotation.Nullable;

import tc.oc.pgm.match.Competitor;

import static com.google.common.base.Preconditions.checkNotNull;

public class JoinRequest {

    private final Optional<Competitor> competitor;
    private final JoinMethod method;

    public JoinRequest(JoinMethod method) {
        this(method, null);
    }

    public JoinRequest(JoinMethod method, @Nullable Competitor competitor) {
        this.competitor = Optional.ofNullable(competitor);
        this.method = checkNotNull(method);
    }

    public static JoinRequest user(@Nullable Competitor competitor) {
        return new JoinRequest(JoinMethod.USER, competitor);
    }

    public static JoinRequest user() {
        return user(null);
    }

    public JoinMethod method() {
        return method;
    }

    public Optional<Competitor> competitor() {
        return competitor;
    }
}
