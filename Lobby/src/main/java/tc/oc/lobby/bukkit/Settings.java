package tc.oc.lobby.bukkit;

import me.anxuiz.settings.*;
import me.anxuiz.settings.bukkit.PlayerSettingCallback;
import me.anxuiz.settings.bukkit.PlayerSettings;
import me.anxuiz.settings.types.EnumType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.hologram.Hologram;
import tc.oc.commons.bukkit.hologram.content.HologramAnimation;
import tc.oc.commons.bukkit.util.ItemCreator;

import javax.annotation.Nonnull;

public class Settings {

    private final Lobby plugin;

    public Settings(Lobby plugin) {
        this.plugin = plugin;
    }

    public void register() {
        SettingRegistry registry = PlayerSettings.getRegistry();
        SettingCallbackManager callbacks = PlayerSettings.getCallbackManager();

        registry.register(HOLOGRAMS);
        /* 1.8 - disable
        callbacks.addCallback(HOLOGRAMS, new PlayerSettingCallback() {
            @Override
            public void notifyChange(@Nonnull Player player, @Nonnull Setting setting, @Nonnull Object o, @Nonnull Object o2) {
                HologramOption option = (HologramOption) o2;

                for (Hologram hologram : plugin.holograms) {
                    if (option == Settings.HologramOption.ALL || (!(hologram.getContent() instanceof HologramAnimation) && option == Settings.HologramOption.STATIC)) {
                        hologram.show(player);
                    } else {
                        hologram.hide(player);
                    }
                }
            }
        });
        */
    }

    public static enum HologramOption {ALL, STATIC, NONE}

    public static final Setting HOLOGRAMS = new SettingBuilder()
        .name("Holograms").alias("hologram").alias("holo")
        .summary("Enables the visibility of holograms")
        .type(
                new EnumType<HologramOption>("hologram options", HologramOption.class) {
                    // migration
                    @Override
                    public Object parse(String raw) throws TypeParseException {
                        try {
                            return super.parse(raw);
                        } catch (TypeParseException e) {
                            if (raw.equalsIgnoreCase("true")) {
                                return HologramOption.STATIC;
                            } else if (raw.equalsIgnoreCase("false")) {
                                return HologramOption.NONE;
                            } else {
                                throw e;
                            }
                        }
                    }
                }
        )
        .defaultValue(HologramOption.NONE).get();
}
