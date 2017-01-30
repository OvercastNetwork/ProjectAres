package tc.oc.api.message.types;

import javax.annotation.Nonnull;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.PartialModel;

@Serialize
public interface PartialModelUpdate<T extends PartialModel> extends ModelMessage<T> {
    @Nonnull T document();
}
