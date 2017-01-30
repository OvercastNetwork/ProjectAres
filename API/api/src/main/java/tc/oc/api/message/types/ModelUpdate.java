package tc.oc.api.message.types;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Model;

@Serialize
public interface ModelUpdate<T extends Model> extends PartialModelUpdate<T> {
}
