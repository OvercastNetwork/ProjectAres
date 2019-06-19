package tc.oc.commons.bukkit.bossbar;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import tc.oc.minecraft.protocol.MinecraftVersion;

import static tc.oc.minecraft.protocol.MinecraftVersion.MINECRAFT_1_9;


public interface BossBarFactory {
    BossBar createBossBar();

    BossBar createBossBar(BaseComponent title, BarColor color, BarStyle style, boolean legacy, BarFlag...flags);

    default BossBar createBossBar(Player player, BaseComponent title, BarColor color, BarStyle style, BarFlag...flags) {
        BossBar bar = createBossBar(title, color, style, MinecraftVersion.lessThan(MINECRAFT_1_9, player.getProtocolVersion()), flags);
        bar.addPlayer(player);
        return bar;
    }

    default BossBar createBossBar(BaseComponent title, BarColor color, BarStyle style, BarFlag...flags) {
        return createBossBar(title, color, style, false, flags);
    }

    BossBar createRenderedBossBar();

    BossBar createLegacyBossBar();
}
