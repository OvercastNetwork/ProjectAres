package tc.oc.commons.bukkit.chat;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.bukkit.nick.Identity;

/**
 * Caches rendered names, both component and legacy. The cache is keyed on
 * the {@link Identity} and {@link NameType} that the name is generated from.
 */
@Singleton
public class CachingNameRenderer implements NameRenderer {

    private final NameRenderer nameRenderer;
    private final Table<Identity, NameType, BaseComponent> components = HashBasedTable.create();
    private final Table<Identity, NameType, String> legacy = HashBasedTable.create();

    @Inject public CachingNameRenderer(NameRenderer nameRenderer) {
        this.nameRenderer = nameRenderer;
    }

    @Override
    public String getLegacyName(Identity identity, NameType type) {
        String rendered = legacy.get(identity, type);
        if(rendered == null) {
            rendered = nameRenderer.getLegacyName(identity, type);
            legacy.put(identity, type, rendered);
        }
        return rendered;
    }

    @Override
    public BaseComponent getComponentName(Identity identity, NameType type) {
        BaseComponent rendered = components.get(identity, type);
        if(rendered == null) {
            rendered = nameRenderer.getComponentName(identity, type);
            components.put(identity, type, rendered);
        }
        return rendered;
    }

    public void invalidateCache(@Nullable Identity identity) {
        if(identity == null) {
            components.clear();
            legacy.clear();
        } else {
            components.rowKeySet().remove(identity);
            legacy.rowKeySet().remove(identity);
        }
    }
}
