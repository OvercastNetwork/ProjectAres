package tc.oc.api.model;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.ImplementedBy;
import tc.oc.api.docs.virtual.Model;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.reflect.Annotations;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.util.TypeMap;

@ImplementedBy(ModelDispatcherImpl.class)
public interface ModelDispatcher {

    void subscribe(ModelListener listener);

    void unsubscribe(ModelListener listener);

    void modelUpdated(@Nullable Model before, @Nullable Model after, Model latest);
}

@Singleton
class ModelDispatcherImpl implements ModelDispatcher {

    private final Logger logger;
    private final TypeMap<Model, ModelHandler> handlers = TypeMap.create();
    private final SetMultimap<ModelListener, Map.Entry<TypeToken<? extends Model>, ModelHandler>> listeners = HashMultimap.create();

    private static <T extends Model> TypeToken<ModelHandler<T>> handlerType(TypeToken<T> model) {
        return new TypeToken<ModelHandler<T>>(){}.where(new TypeParameter<T>(){}, model);
    }

    @Inject ModelDispatcherImpl(Loggers loggers, TypeMap<Model, ModelHandler> staticHandlers, Set<ModelListener> staticListeners) {
        this.logger = loggers.get(getClass());
        handlers.putAll(staticHandlers); // Add all statically bound ModelHandlers
        staticListeners.forEach(this::subscribe); // Subscribe all statically bound ModelListeners
    }

    @Override
    public void subscribe(ModelListener listener) {
        final TypeToken<? extends ModelListener> listenerType = TypeToken.of(listener.getClass());

        Methods.declaredMethodsInAncestors(listener.getClass())
               .filter(Annotations.annotatedWith(ModelListener.HandleModel.class)).forEach(method -> {

            final TypeToken<? extends Model> model = (TypeToken<? extends Model>) listenerType.method(method).getParameters().get(0).getType();
            final ModelHandler<? extends Model> handler = Methods.lambda(handlerType(model), method, listener);
            handlers.put(model, handler);
            listeners.put(listener, Maps.immutableEntry(model, handler));

            logger.fine(() -> "Dispatching " + model + " updates to " + Methods.describe(listener.getClass(), method.getName()));
        });
    }

    @Override
    public void unsubscribe(ModelListener listener) {
        listeners.removeAll(listener).forEach(entry -> handlers.remove(entry.getKey(), entry.getValue()));
    }

    @Override
    public void modelUpdated(@Nullable Model before, @Nullable Model after, Model latest) {
        for(ModelHandler handler : handlers.allAssignableFrom(latest.getClass())) {
            handler.modelUpdated(before, after, latest);
        }
    }
}
