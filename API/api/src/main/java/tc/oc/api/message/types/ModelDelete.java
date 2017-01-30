package tc.oc.api.message.types;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Model;

@Serialize
public interface ModelDelete<T extends Model> extends ModelMessage<T> {
    String document_id();
}
