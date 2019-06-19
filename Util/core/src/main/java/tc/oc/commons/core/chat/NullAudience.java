package tc.oc.commons.core.chat;

import java.util.Optional;

public interface NullAudience extends ForwardingAudience {

    @Override
    default Optional<Audience> audience() {
        return Optional.empty();
    }

    NullAudience INSTANCE = new NullAudience(){};

}
