package tc.oc.api.model;

import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.docs.virtual.PartialModel;
import tc.oc.api.message.types.FindMultiResponse;
import tc.oc.api.message.types.ModelUpdate;
import tc.oc.api.message.types.PartialModelUpdate;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.commons.core.reflect.Types;

import static tc.oc.commons.core.reflect.Types.assertFullySpecified;

public class ModelMeta<M extends Model, P extends PartialModel> {

    private final TypeToken<P> partialType;
    private final TypeToken<M> completeType;

    private final TypeToken<PartialModelUpdate<P>> partialUpdateType;
    private final TypeToken<ModelUpdate<M>> completeUpdateType;
    private final TypeToken<FindMultiResponse<M>> multiResponseType;

    private final Provider<Optional<ModelStore<M>>> store;

    private final String name;
    private final String singularName;
    private final String pluralName;

    @Inject ModelMeta(TypeLiteral<M> completeType, TypeLiteral<P> partialType, Provider<Optional<ModelStore<M>>> store) {
        this.partialType = Types.toToken(partialType);
        this.completeType = Types.toToken(completeType);

        partialUpdateType = assertFullySpecified(new TypeToken<PartialModelUpdate<P>>(){}.where(new TypeParameter<P>(){}, this.partialType));
        completeUpdateType = assertFullySpecified(new TypeToken<ModelUpdate<M>>(){}.where(new TypeParameter<M>(){}, this.completeType));
        multiResponseType = assertFullySpecified(new TypeToken<FindMultiResponse<M>>(){}.where(new TypeParameter<M>(){}, this.completeType));

        this.store = store;

        final ModelName annot = completeType.getRawType().getAnnotation(ModelName.class);
        if(annot != null) {
            name = annot.value();
            singularName = annot.singular().length() > 0 ? annot.singular() : name.toLowerCase();
            pluralName = annot.plural().length() > 0 ? annot.plural() : StringUtils.pluralize(singularName);
        } else {
            name = completeType.getRawType().getSimpleName();
            singularName = name.toLowerCase();
            pluralName = StringUtils.pluralize(singularName);
        }
    }

    public TypeToken<P> partialType() {
        return partialType;
    }

    public TypeToken<M> completeType() {
        return completeType;
    }

    public Class<P> partialTypeRaw() {
        return (Class<P>) partialType().getRawType();
    }

    public Class<M> completeTypeRaw() {
        return (Class<M>) completeType().getRawType();
    }

    public TypeToken<PartialModelUpdate<P>> partialUpdateType() {
        return partialUpdateType;
    }

    public TypeToken<ModelUpdate<M>> completeUpdateType() {
        return completeUpdateType;
    }

    public TypeToken<FindMultiResponse<M>> multiResponseType() {
        return multiResponseType;
    }

    public Optional<ModelStore<M>> store() {
        return store.get();
    }

    public String name() {
        return name;
    }

    public String singularName() {
        return singularName;
    }

    public String pluralName() {
        return pluralName;
    }

    public interface Builder<M extends Model> {
        Builder<M> partial(Class<? extends PartialModel> type);
        Builder<M> store(Class<? extends ModelStore<M>> type);
    }
}
