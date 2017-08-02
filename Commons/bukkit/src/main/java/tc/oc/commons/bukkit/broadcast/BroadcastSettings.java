package tc.oc.commons.bukkit.broadcast;

import javax.inject.Inject;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.BooleanType;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.broadcast.model.BroadcastPrefix;
import tc.oc.commons.bukkit.settings.SettingManagerProvider;
import tc.oc.commons.bukkit.util.ItemCreator;

public class BroadcastSettings {

    public static final Setting TIPS = new SettingBuilder()
        .name("Tips")
        .summary("Show tips in chat")
        .type(new BooleanType())
        .defaultValue(true)
        .get();

    public static final Setting NEWS = new SettingBuilder()
        .name("News")
        .summary("Show news and alerts in chat")
        .type(new BooleanType())
        .defaultValue(true)
        .get();

    public static final Setting FACTS = new SettingBuilder()
        .name("Facts")
        .summary("Show facts and knowledge in chat")
        .type(new BooleanType())
        .defaultValue(true)
        .get();

    public static final Setting RANDOM = new SettingBuilder()
        .name("Random")
        .summary("Show random wisdom in chat")
        .type(new BooleanType())
        .defaultValue(true)
        .get();

    private final SettingManagerProvider settings;

    @Inject BroadcastSettings(SettingManagerProvider settings) {
        this.settings = settings;
    }

    public boolean isVisible(BroadcastPrefix prefix, CommandSender viewer) {
        if(!(viewer instanceof Player)) return true;

        final Player player = (Player) viewer;
        final Setting setting;
        switch(prefix) {
            case TIP:
                setting = TIPS;
                break;

            case NEWS:
            case ALERT:
                setting = NEWS;
                break;

            case INFO:
            case FACT:
                setting = FACTS;
                break;

            case CHAT:
                setting = RANDOM;
                break;

            default:
                return true;
        }

        return (boolean) settings.getManager(player).getValue(setting);
    }
}
