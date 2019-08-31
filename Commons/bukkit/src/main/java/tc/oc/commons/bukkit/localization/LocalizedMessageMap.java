package tc.oc.commons.bukkit.localization;

import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.chat.RenderableComponent;
import tc.oc.commons.core.chat.ImmutableComponent;
import tc.oc.commons.core.localization.Locales;

public class LocalizedMessageMap extends AbstractMap<String, BaseComponent> {

    public interface Factory {
        LocalizedMessageMap create(@Assisted("source") Path sourcePath,
                                   @Assisted("localized") Path localizedPath);
    }

    private final LocalizedDocument<Map<String, BaseComponent>> document;

    @Inject LocalizedMessageMap(@Assisted("source") Path sourcePath, @Assisted("localized") Path localizedPath, LocalizedDocument.Factory<Map<String, BaseComponent>> factory) {
        this.document = factory.create(sourcePath, localizedPath);
    }

    public void disable() {
        document.disable();
    }

    @Override
    public int size() {
        return document.getDefault().map(Map::size).orElse(0);
    }

    @Override
    public boolean isEmpty() {
        return !document.getDefault().filter(map -> !map.isEmpty()).isPresent();
    }

    @Override
    public boolean containsKey(Object key) {
        return document.getDefault().filter(map -> map.containsKey(key)).isPresent();
    }

    @Override
    public Set<String> keySet() {
        return document.getDefault().map(Map::keySet).orElseGet(ImmutableSet::of);
    }

    @Override
    public Collection<BaseComponent> values() {
        return Collections2.transform(keySet(), this::get);
    }

    @Override
    public Set<Entry<String, BaseComponent>> entrySet() {
        return Maps.asMap(keySet(), this::get).entrySet();
    }

    @Override
    public BaseComponent get(Object key) {
        return key instanceof String ? new Component((String) key) : null;
    }

    private class Component extends ImmutableComponent implements RenderableComponent {
        final String key;

        Component(String key) {
            this.key = key;
        }

        @Override
        public BaseComponent render(ComponentRenderContext context, CommandSender viewer) {
            final Locale locale = Locales.locale(viewer);
            return document.get(locale)
                           .map(map -> map.get(key))
                           .filter(c -> c != null)
                           .findFirst()
                           .map(c -> context.render(c, viewer))
                           .orElseThrow(() -> new IllegalStateException("Can't find localized message " + key +
                                                                        " for locale " + locale));
        }

        @Override
        public BaseComponent duplicateWithoutFormatting() {
            return duplicate();
        }
    }
}
