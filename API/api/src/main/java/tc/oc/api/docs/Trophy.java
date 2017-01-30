package tc.oc.api.docs;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Model;

import javax.annotation.Nonnull;

@Serialize
public interface Trophy extends Model {

    @Nonnull String name();
    @Nonnull String description();

    @Override @Serialize(false)
    default String toShortString() {
        return _id();
    }

}
