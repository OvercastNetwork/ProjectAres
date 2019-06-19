package tc.oc.commons.bukkit.bossbar;

import javax.inject.Inject;
import javax.inject.Provider;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Server;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import tc.oc.commons.core.chat.Components;

public class BossBarFactoryImpl implements BossBarFactory {

    private static final BarColor DEFAULT_COLOR = BarColor.PURPLE;

    private final Server server;
    private final Provider<RenderedBossBar> renderedBossBarProvider;
    private final Provider<LegacyBossBar> legacyBossBarProvider;

    @Inject BossBarFactoryImpl(Server server, Provider<RenderedBossBar> renderedBossBarProvider, Provider<LegacyBossBar> legacyBossBarProvider) {
        this.server = server;
        this.renderedBossBarProvider = renderedBossBarProvider;
        this.legacyBossBarProvider = legacyBossBarProvider;
    }

    @Override
    public BossBar createBossBar(BaseComponent title, BarColor color, BarStyle style, boolean legacy, BarFlag... flags) {
        BossBar bar;
        if(legacy) {
            bar = createLegacyBossBar();
            bar.setTitle(title);
        } else {
            bar = server.createBossBar(title, color, style, flags);
        }
        return bar;
    }

    @Override
    public BossBar createBossBar() {
        return createBossBar(Components.blank(), DEFAULT_COLOR, BarStyle.SOLID);
    }

    @Override
    public BossBar createRenderedBossBar() {
        return renderedBossBarProvider.get();
    }

    @Override
    public BossBar createLegacyBossBar() {
        return legacyBossBarProvider.get();
    }
}
