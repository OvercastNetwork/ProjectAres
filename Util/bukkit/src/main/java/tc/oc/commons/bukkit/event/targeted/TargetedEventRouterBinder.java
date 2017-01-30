package tc.oc.commons.bukkit.event.targeted;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import tc.oc.commons.core.inject.TypeMapBinder;

public class TargetedEventRouterBinder {

    private final TypeMapBinder<Object, TargetedEventRouter<?>> typeMapBinder;

    public TargetedEventRouterBinder(Binder binder) {
        typeMapBinder = new TypeMapBinder<Object, TargetedEventRouter<?>>(binder){};
    }

    public <T> LinkedBindingBuilder<TargetedEventRouter<? super T>> bindEvent(Class<T> type) {
        return bindEvent(TypeLiteral.get(type));
    }

    public <T> LinkedBindingBuilder<TargetedEventRouter<? super T>> bindEvent(TypeLiteral<T> type) {
        return (LinkedBindingBuilder) typeMapBinder.addBinding(type);
    }
}
