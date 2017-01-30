package tc.oc.commons.bukkit.tablist;

import net.md_5.bungee.api.chat.TextComponent;
import tc.oc.commons.core.util.DefaultProvider;

public class BlankTabEntry extends StaticTabEntry {

    public static class Factory implements DefaultProvider<Integer, TabEntry> {
        @Override
        public TabEntry get(Integer key) {
            return new BlankTabEntry();
        }
    }

    private final static TextComponent BLANK_COMPONENT = new TextComponent("");

    public BlankTabEntry() {
        super(BLANK_COMPONENT);
    }
}
