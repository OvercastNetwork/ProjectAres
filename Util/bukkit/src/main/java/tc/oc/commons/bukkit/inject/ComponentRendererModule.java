package tc.oc.commons.bukkit.inject;

import com.google.inject.AbstractModule;
import com.google.inject.binder.AnnotatedBindingBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.bukkit.chat.ComponentRenderer;
import tc.oc.commons.bukkit.chat.ComponentRenderers;

public abstract class ComponentRendererModule extends AbstractModule {
    protected <T extends BaseComponent> AnnotatedBindingBuilder<ComponentRenderer<? super T>> bindComponent(Class<T> componentType) {
        return bind(ComponentRenderers.rendererType(componentType));
    }
}
