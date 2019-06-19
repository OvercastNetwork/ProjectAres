package tc.oc.pgm.bossbar;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.bossbar.BossBarFactory;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.util.MapUtils;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;

@ListenerScope(MatchScope.LOADED)
public class BossBarMatchModule extends MatchModule implements Listener {

    // Sources that are automatically rendered for all players, even those
    // who join after the source is added. The source can still hide itself for
    // specific players at render time.
    private final Set<BossBarSource> globalSources = new HashSet<>();

    // Views by viewer and source, including global sources
    private final Table<Player, BossBarSource, View> views = HashBasedTable.create();

    @Inject private BossBarFactory bossBarFactory;
    @Inject private ComponentRenderContext renderer;

    public void add(BossBarSource source) {
        if(globalSources.add(source)) {
            match.players().forEach(
                player -> views.put(player.getBukkit(), source,
                                    new View(source, player.getBukkit()))
            );
        }
    }

    public void remove(BossBarSource source) {
        globalSources.remove(source);
        final Map<Player, View> playerViews = views.columnMap().remove(source);
        if(playerViews != null) {
            playerViews.values().forEach(View::destroy);
        }
    }

    public void add(BossBarSource source, Stream<Player> viewers) {
        viewers.forEach(viewer -> {
            views.row(viewer).computeIfAbsent(source, s -> new View(source, viewer));
        });
    }

    public void remove(BossBarSource source, Stream<Player> viewers) {
        viewers.forEach(viewer -> {
            final View view = views.remove(viewer, source);
            if(view != null) view.destroy();
        });
    }

    public void render(BossBarSource source) {
        views.column(source).values().forEach(View::render);
    }

    public void invalidate(BossBarSource source) {
        views.column(source).values().forEach(View::invalidate);
    }

    public void render(BossBarSource source, Player viewer) {
        MapUtils.value(views, viewer, source).ifPresent(View::render);
    }

    public void invalidate(BossBarSource source, Player viewer) {
        MapUtils.value(views, viewer, source).ifPresent(View::invalidate);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinLeave(PlayerChangePartyEvent event) throws EventException {
        final Player viewer = event.getPlayer().getBukkit();

        if(event.isLeavingMatch()) {
            Optional.ofNullable(views.rowMap().remove(viewer))
                    .ifPresent(row -> row.values().forEach(View::destroy));
        }

        event.yield();

        if(event.isJoiningMatch()) {
            for(BossBarSource source : globalSources) {
                views.put(viewer, source, new View(source, viewer));
            }
        }
    }

    private class View {

        final BossBarSource source;
        final Player viewer;
        final BossBar bar;

        View(BossBarSource source, Player viewer) {
            this.source = source;
            this.viewer = viewer;
            this.bar = bossBarFactory.createBossBar(viewer, Components.blank(), BarColor.WHITE, BarStyle.SOLID);
            render();
        }

        void destroy() {
            bar.removePlayer(viewer);
        }

        public void invalidate() {
            match.getScheduler(MatchScope.LOADED).debounceTask(this::render);
        }

        public void render() {
            final Optional<BossBarContent> content = source.barContent(viewer);

            if(!content.isPresent()) {
                bar.setVisible(false);
                return; // Don't try to get any other properties when hidden
            }

            bar.update(renderer.render(content.get().text(), viewer),
                       content.get().progress(),
                       source.barColor(viewer),
                       source.barStyle(viewer),
                       source.barFlags(viewer));

            bar.setVisible(true);
        }
    }
}
