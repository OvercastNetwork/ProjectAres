package tc.oc.commons.bukkit.chat;

import net.md_5.bungee.api.chat.BaseComponent;

public abstract class ComponentPaginator extends Paginator<BaseComponent> {
    @Override
    protected BaseComponent entry(BaseComponent entry, int index) {
        return entry;
    }
}
