package tc.oc.api.message.types;

import javax.annotation.Nullable;

import com.google.common.reflect.TypeToken;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.PartialModel;
import tc.oc.commons.core.reflect.Types;

public class FindRequest<T extends PartialModel> implements ModelMessage<T> {

    private final TypeToken<T> model;

    public FindRequest(@Nullable TypeToken<T> model) {
        this.model = model != null ? model : Types.assertFullySpecified(new TypeToken<T>(getClass()){});
    }

    public FindRequest(@Nullable Class<T> model) {
        this(model == null ? null : TypeToken.of(model));
    }

    protected FindRequest() {
        this((Class) null);
    }

    @Serialize public @Nullable Integer skip() {
        return null;
    }

    @Serialize public @Nullable Integer limit() {
        return null;
    }

    @Override
    public TypeToken<T> model() {
        return model;
    }
}
