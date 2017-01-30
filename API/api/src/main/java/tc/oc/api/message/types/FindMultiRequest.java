package tc.oc.api.message.types;

import java.util.Collection;

import com.google.common.reflect.TypeToken;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.PartialModel;

public class FindMultiRequest<T extends PartialModel> extends FindRequest<T> {

    private final Collection<String> ids;
    @Serialize public Collection<String> ids() { return ids; }

    public FindMultiRequest(TypeToken<T> model, Collection<String> ids) {
        super(model);
        this.ids = ids;
    }

    public FindMultiRequest(Class<T> model, Collection<String> ids) {
        this(TypeToken.of(model), ids);
    }
}
