package tc.oc.commons.bukkit.chat;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;

@Singleton
public class ComponentRendererRegistry implements ComponentRenderContext {

    private final Injector injector;

    private final LoadingCache<Class<? extends BaseComponent>, ComponentRenderer> renderers = CacheBuilder.newBuilder().build(new CacheLoader<Class<? extends BaseComponent>, ComponentRenderer>() {
        @Override
        public ComponentRenderer load(final Class<? extends BaseComponent> type) throws Exception {
            ConfigurationException originalException = null;
            for(Class c = type; BaseComponent.class.isAssignableFrom(c); c = c.getSuperclass()) {
                try {
                    return (ComponentRenderer) injector.getInstance(Key.get(ComponentRenderers.rendererType(c)));
                } catch(ConfigurationException e) {
                    if(originalException == null) originalException = e;
                }
            }
            throw new IllegalStateException("Can't find a renderer for component type " + type, originalException);
        }
    });

    @Inject ComponentRendererRegistry(Injector injector) {
        this.injector = injector;
    }

    public <T extends BaseComponent> ComponentRenderer<? super T> getRenderer(T component) {
        return (ComponentRenderer<? super T>) renderers.getUnchecked(component.getClass());
    }

    @Override
    public BaseComponent render(BaseComponent component, CommandSender viewer) {
        if(component instanceof RenderableComponent) {
            return ((RenderableComponent) component).render(this, viewer);
        }

        ComponentRenderer renderer = getRenderer(component);
        if(renderer != null) {
            return renderer.render(this, component, viewer);
        }

        return component;
    }
}
