package tc.oc.api.model;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import tc.oc.api.docs.virtual.Model;
import tc.oc.commons.core.inject.SetBinder;
import tc.oc.commons.core.inject.TypeMapBinder;

public class ModelListenerBinder {

    private final TypeMapBinder<Model, ModelHandler> handlers;
    private final SetBinder<ModelListener> listeners;

    public ModelListenerBinder(Binder binder) {
        binder = binder.skipSources(ModelListenerBinder.class);
        this.handlers = new TypeMapBinder<Model, ModelHandler>(binder){};
        this.listeners = new SetBinder<ModelListener>(binder){};
    }

    public <M extends Model> LinkedBindingBuilder<ModelHandler<? super M>> bindHandler(Class<M> model) {
        return (LinkedBindingBuilder) handlers.addBinding(model);
    }

    public <M extends Model> LinkedBindingBuilder<ModelHandler<? super M>> bindHandler(TypeLiteral<M> model) {
        return (LinkedBindingBuilder) handlers.addBinding(model);
    }

    public LinkedBindingBuilder<ModelListener> bindListener() {
        return listeners.addBinding();
    }
}
