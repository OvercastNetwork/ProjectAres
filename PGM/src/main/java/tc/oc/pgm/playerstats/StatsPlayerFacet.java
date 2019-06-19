package tc.oc.pgm.playerstats;

import java.text.DecimalFormat;
import javax.inject.Inject;

import me.anxuiz.settings.SettingManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.event.targeted.TargetedEventHandler;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerFacet;
import tc.oc.pgm.match.MatchScheduler;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.inject.ForRunningMatch;

import static tc.oc.minecraft.protocol.MinecraftVersion.lessThan;
import static tc.oc.minecraft.protocol.MinecraftVersion.MINECRAFT_1_8;

@ListenerScope(MatchScope.RUNNING)
public class StatsPlayerFacet implements MatchPlayerFacet, Listener {

    private static final int DISPLAY_TICKS = 60;
    private static final int LEGACY_TICKS = 2;
    private static final DecimalFormat FORMAT = new DecimalFormat("0.00");

    private final MatchScheduler scheduler;
    private final StatsUserFacet statsUserFacet;
    private final MatchPlayer player;
    private final SettingManager settings;
    private Task task = null;

    @Inject
    private StatsPlayerFacet(@ForRunningMatch MatchScheduler scheduler, StatsUserFacet statsUserFacet, MatchPlayer player, SettingManager settings) {
        this.scheduler = scheduler;
        this.statsUserFacet = statsUserFacet;
        this.player = player;
        this.settings = settings;
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(final MatchPlayerDeathEvent event) {
        if (event.isVictim(this.player) || event.isKiller(this.player)) update();
    }

    private void update() {
        if (!settings.getValue(StatSettings.STATS, Boolean.class)) return;
        if (task != null) {
            task.cancel();
        }

        task = scheduler.createRepeatingTask(1, 1, new Runnable() {
            int ticks = lessThan(MINECRAFT_1_8, player.getBukkit().getProtocolVersion()) ? LEGACY_TICKS : DISPLAY_TICKS;
            @Override
            public void run() {
                if (--ticks > 0) {
                    player.sendHotbarMessage(getMessage());
                } else {
                    delete();
                }
            }
        });
    }

    protected TranslatableComponent getMessage() {
        TranslatableComponent component = new TranslatableComponent("stats.hotbar",
                new Component(statsUserFacet.matchKills(), ChatColor.GREEN),
                new Component(statsUserFacet.lifeKills(), ChatColor.GREEN),
                new Component(statsUserFacet.deaths(), ChatColor.RED),
                new Component(FORMAT.format((double) statsUserFacet.matchKills() / Math.max(statsUserFacet.deaths(), 1)), ChatColor.AQUA));
        component.setBold(true);
        return component;
    }

    protected void delete() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public void disable() {
        delete();
    }

}
