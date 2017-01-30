package tc.oc.api.docs;

import javax.annotation.Nonnull;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.docs.virtual.ServerDoc;

@Serialize
public interface Game extends Model {
    @Nonnull String name();
    int priority();
    @Nonnull ServerDoc.Visibility visibility();

    @Override @Serialize(false)
    default String toShortString() {
        return name();
    }
}
