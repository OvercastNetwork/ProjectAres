package tc.oc.api.docs.virtual;

import tc.oc.api.annotations.Serialize;

@Serialize
public interface Model extends PartialModel {
    String _id();
    @Serialize(false) default String toShortString() { return toString(); }
}
