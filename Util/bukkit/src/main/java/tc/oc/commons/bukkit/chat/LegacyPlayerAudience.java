package tc.oc.commons.bukkit.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.stream.Collectors;

import java.util.stream.Stream;

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
            emphasize(message);
            recentHotbarMessage = message;
        }
    }

    @Override
    public void showTitle(BaseComponent title, BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
        emphasize(Components.join(Components.space(), Stream.of(title, subtitle).filter(msg -> msg != null).collect(Collectors.toImmutableList())));
    }

    protected void emphasize(BaseComponent message) {
        sendMessage(Components.blank());
        sendMessage(message);
        sendMessage(Components.blank());
    }

}
