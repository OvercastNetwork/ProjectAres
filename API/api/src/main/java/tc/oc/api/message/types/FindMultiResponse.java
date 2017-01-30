package tc.oc.api.message.types;

import java.util.List;
import javax.annotation.Nonnull;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.PartialModel;

@Serialize
public interface FindMultiResponse<T extends PartialModel> extends ModelMessage<T> {
    @Nonnull List<T> documents();
}
