package tc.oc.commons.bukkit.chat;

import javax.inject.Inject;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.bukkit.flairs.FlairRenderer;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.nick.UsernameRenderer;
import tc.oc.commons.core.chat.Component;

public class FullNameRenderer implements NameRenderer {

    private final FlairRenderer flairRenderer;
    private final UsernameRenderer usernameRenderer;

    @Inject public FullNameRenderer(FlairRenderer flairRenderer, UsernameRenderer usernameRenderer) {
        this.flairRenderer = flairRenderer;
        this.usernameRenderer = usernameRenderer;
    }

    @Override
    public String getLegacyName(Identity identity, NameType type) {
        return flairRenderer.getLegacyName(identity, type) + usernameRenderer.getLegacyName(identity, type);
    }

    @Override
    public BaseComponent getComponentName(Identity identity, NameType type) {
        return new Component(flairRenderer.getComponentName(identity, type), usernameRenderer.getComponentName(identity, type));
    }
}
