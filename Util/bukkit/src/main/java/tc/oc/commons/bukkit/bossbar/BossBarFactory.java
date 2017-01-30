package tc.oc.commons.bukkit.bossbar;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public interface BossBarFactory {
    BossBar createBossBar();

    BossBar createBossBar(BaseComponent title, BarColor color, BarStyle style, BarFlag...flags);

    BossBar createRenderedBossBar();
}
