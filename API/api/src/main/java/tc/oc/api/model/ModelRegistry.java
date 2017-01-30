package tc.oc.api.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.inject.Inject;

import com.google.common.base.Splitter;
import com.google.common.reflect.TypeToken;
import tc.oc.api.docs.virtual.PartialModel;
import tc.oc.api.exceptions.SerializationException;
import tc.oc.commons.core.util.AmbiguousElementException;
import tc.oc.commons.core.util.TypeMap;

public class ModelRegistry {

    private final TypeMap<PartialModel, ModelMeta> byType;
    private final Map<String, ModelMeta> byName;

    @Inject ModelRegistry(TypeMap<PartialModel, ModelMeta> byType, Map<String, ModelMeta> byName) {
        this.byType = byType;
        this.byName = byName;
    }

    public <T extends PartialModel> ModelMeta<?, ? super T> meta(Class<T> model) {
        final TypeToken<T> tt = TypeToken.of(model);
        return meta(tt);
    }

    public <T extends PartialModel> ModelMeta<?, ? super T> meta(TypeToken<T> model) {
        try {
            return byType.oneAssignableFrom(model);
        } catch(NoSuchElementException e) {
            throw new SerializationException(model.getRawType().getName() + " is not a registered model");
        } catch(AmbiguousElementException e) {
            throw new SerializationException(model.getRawType().getName() + " extends from multiple models");
        }
    }

    public ModelMeta resolve(String query) {
        return Splitter.on("::") // Extract embedded Ruby classes
                .splitToList(query)
                .stream()
                .sorted(Comparator.reverseOrder()) // Give priority to descendants
                .filter(byName::containsKey)
                .map(byName::get)
                .findFirst()
                .orElseThrow(() -> new NoSuchModelException("No registered model named '" + query + "'"));
    }

    public Collection<ModelMeta> all() {
        return byName.values();
    }
}
