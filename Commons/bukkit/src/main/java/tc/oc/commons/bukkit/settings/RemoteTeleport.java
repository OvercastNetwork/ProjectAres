package tc.oc.commons.bukkit.settings;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.BooleanType;

/**
 * TODO: Not implemented yet
 */
public class RemoteTeleport {
    private static final Setting inst = new SettingBuilder()
        .name("RemoteTeleport").alias("rtp")
        .summary("Allow /tp to teleport you across servers, just like /rtp")
        .type(new BooleanType())
        .defaultValue(true).get();

    public static Setting get() {
        return inst;
    }
}
