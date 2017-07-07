package tc.oc.commons.bukkit.bossbar;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.chat.Components;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LegacyBossBar implements BossBar {

    private final ComponentRenderContext renderer;
    private final Map<Player, NMSHacks.FakeWither> views = new HashMap<>();
    private final Map<Player, Integer> tasks = new HashMap<>();

    private BaseComponent title = Components.blank();
    private double progress = 1;
    private boolean visible = true;

    @Inject LegacyBossBar(ComponentRenderContext renderer) {
        this.renderer = renderer;
    }

    @Override
    public BaseComponent getTitle() {
        return title;
    }

    @Override
    public BarColor getColor() {
        return BarColor.PURPLE;
    }

    @Override
    public BarStyle getStyle() {
        return BarStyle.SOLID;
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public List<Player> getPlayers() {
        return ImmutableList.copyOf(views.keySet());
    }

    @Override
    public void setTitle(BaseComponent title) {
        this.title = title;
        views.forEach((player, wither) -> wither.name(player, renderer.renderLegacy(title, player), isVisible()));
    }

    @Override
    public void setProgress(double progress) {
        this.progress = progress;
        views.forEach((player, wither) -> wither.health(player, progress, isVisible()));
    }

    @Override
    public void addPlayer(Player player) {
        if(!views.containsKey(player)) {
            NMSHacks.FakeWither view = new NMSHacks.FakeWither(player.getWorld(), renderer.renderLegacy(title, player));
            views.put(player, view);
            int task = scheduler().scheduleSyncRepeatingTask(plugin(), () -> { if(isVisible()) view.teleport(player, witherLocation(player)); }, 0, 1);
            tasks.put(player, task);
            if(isVisible()) {
                view.spawn(player, witherLocation(player));
            }
        }
    }

    @Override
    public void removePlayer(Player player) {
        int task = tasks.remove(player);
        if(task != -1) {
            scheduler().cancelTask(task);
        }
        NMSHacks.FakeWither view = views.remove(player);
        if(view != null) {
            view.destroy(player);
        }
    }

    @Override
    public void removeAll() {
        ImmutableSet.copyOf(views.keySet()).forEach(this::removePlayer);
        views.clear();
    }

    @Override
    public void setVisible(boolean visible) {
        boolean previous = isVisible();
        this.visible = visible;
        if(previous && !visible) {
            views.forEach((player, wither) -> wither.destroy(player));
        } else if(!previous && visible) {
            views.forEach((player, wither) -> wither.spawn(player, witherLocation(player)));
        }
    }

    @Override
    public void update(BaseComponent title, double progress, BarColor color, BarStyle style, Set<BarFlag> flags) {
        this.title = title;
        this.progress = progress;
        views.forEach((player, wither) -> wither.update(player, renderer.renderLegacy(title, player), progress, isVisible()));
    }

    @Override
    public void show() {
        setVisible(true);
    }

    @Override
    public void hide() {
        setVisible(false);
    }

    @Override
    public void setColor(BarColor color) {}

    @Override
    public void setStyle(BarStyle style) {}

    @Override
    public void setFlags(Set<BarFlag> flags) {}

    @Override
    public void removeFlag(BarFlag flag) {}

    @Override
    public void addFlag(BarFlag flag) {}

    @Override
    public boolean hasFlag(BarFlag flag) {
        return false;
    }

    protected Location witherLocation(Player player) {
        Location eye = player.getEyeLocation().clone();
        eye.setPitch(eye.getPitch() - 20);
        return player.getEyeLocation().add(eye.getDirection().multiply(32));
    }

    // HACK

    protected Plugin plugin() {
        return Bukkit.getPluginManager().getPlugin("Commons");
    }

    protected BukkitScheduler scheduler() {
        return Bukkit.getScheduler();
    }

}
