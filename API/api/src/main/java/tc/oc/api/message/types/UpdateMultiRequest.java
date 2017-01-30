package tc.oc.api.message.types;

import java.util.Collection;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface UpdateMultiRequest<T> extends Document {
    Collection<T> documents();
}
