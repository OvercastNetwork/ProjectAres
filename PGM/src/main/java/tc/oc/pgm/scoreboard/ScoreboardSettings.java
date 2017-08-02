package tc.oc.pgm.scoreboard;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.BooleanType;
import org.bukkit.Material;
import tc.oc.commons.bukkit.util.ItemCreator;

public class ScoreboardSettings {
    private ScoreboardSettings() {}

    public static final Setting SHOW_SCOREBOARD = new SettingBuilder()
        .name("Scoreboard").alias("sb")
        .summary("See the scoreboard with game information")
        .type(new BooleanType())
        .defaultValue(true).get();
}
