package tc.oc.pgm.playerstats;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.BooleanType;
import org.bukkit.Material;
import tc.oc.commons.bukkit.util.ItemCreator;

public class StatSettings {

    public static final Setting STATS = new SettingBuilder()
            .name("Stats")
            .summary("Show kill and death stats on pvp encounters")
            .type(new BooleanType())
            .defaultValue(true).get();

}
