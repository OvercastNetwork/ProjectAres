package tc.oc.commons.bukkit.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;

public class LegacyPlayerAudience extends PlayerAudience {

    private BaseComponent recentHotbarMessage;

    public LegacyPlayerAudience(Player player) {
        super(player);
    }

    @Override
    public void sendHotbarMessage(BaseComponent message) {
        // Do not spam hot bar messages, as the protocol converts
        // them to regular chat messages.
        if(!Components.equals(message, recentHotbarMessage)) {
            super.sendHotbarMessage(message);
            recentHotbarMessage = message;
        }
    }

    @Override
    public void showTitle(BaseComponent title, BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
        super.sendMessage(new Component(title).extra(" ").extra(subtitle));
    }
}
