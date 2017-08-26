package tc.oc.commons.bukkit.bossbar;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.core.chat.Components;

public class RenderedBossBar implements BossBar {

    private final ComponentRenderContext renderer;
    private final BossBarFactory bossBarFactory;

    private final Map<Player, BossBar> views = new HashMap<>();

    private BaseComponent title = Components.blank();
    private double progress = 0;
    private final Set<BarFlag> flags = EnumSet.noneOf(BarFlag.class);
    private BarColor color = BarColor.PURPLE;
    private BarStyle style = BarStyle.SOLID;
    private boolean visibile = true;

    @Inject RenderedBossBar(ComponentRenderContext renderer, BossBarFactory bossBarFactory) {
        this.renderer = renderer;
        this.bossBarFactory = bossBarFactory;
    }

    @Override
    public BaseComponent getTitle() {
        return title;
    }

    @Override
    public BarColor getColor() {
        return color;
    }

    @Override
    public BarStyle getStyle() {
        return style;
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public boolean hasFlag(BarFlag flag) {
        return this.flags.contains(flag);
    }

    @Override
    public boolean isVisible() {
        return visibile;
    }

    @Override
    public List<Player> getPlayers() {
        return ImmutableList.copyOf(views.keySet());
    }

    @Override
    public void setTitle(BaseComponent title) {
        this.title = title;
        views.entrySet().forEach(entry -> entry.getValue().setTitle(renderer.render(title, entry.getKey())));
    }

    @Override
    public void setColor(BarColor color) {
        this.color = color;
        views.values().forEach(view -> view.setColor(color));
    }

    @Override
    public void setStyle(BarStyle style) {
        this.style = style;
        views.values().forEach(view -> view.setStyle(style));
    }

    @Override
    public void setFlags(Set<BarFlag> flags) {
        this.flags.clear();
        this.flags.addAll(flags);
        views.values().forEach(view -> view.setFlags(flags));
    }

    @Override
    public void removeFlag(BarFlag flag) {
        this.flags.remove(flag);
        views.values().forEach(view -> view.removeFlag(flag));
    }

    @Override
    public void addFlag(BarFlag flag) {
        this.flags.add(flag);
        views.values().forEach(view -> view.addFlag(flag));
    }

    @Override
    public void setProgress(double progress) {
        this.progress = progress;
        views.values().forEach(view -> view.setProgress(progress));
    }

    @Override
    public void addPlayer(Player player) {
        if(!views.containsKey(player)) {
            final BossBar view = bossBarFactory.createBossBar(player, renderer.render(title, player), color, style, flags.toArray(new BarFlag[flags.size()]));
            view.setVisible(visibile);
            views.put(player, view);
        }
    }

    @Override
    public void removePlayer(Player player) {
        final BossBar view = views.remove(player);
        if(view != null) view.removePlayer(player);
    }

    @Override
    public void removeAll() {
        views.values().forEach(BossBar::removeAll);
        views.clear();
    }

    @Override
    public void setVisible(boolean visible) {
        this.visibile = visible;
        views.values().forEach(view -> view.setVisible(visible));
    }

    @Override
    public void show() {
        views.values().forEach(BossBar::show);
    }

    @Override
    public void hide() {
        views.values().forEach(BossBar::hide);
    }

    @Override
    public void update(BaseComponent title, double progress, BarColor color, BarStyle style, Set<BarFlag> flags) {
        this.title = title;
        this.progress = progress;
        this.color = color;
        this.style = style;
        this.flags.clear();
        this.flags.addAll(flags);

        views.entrySet().forEach(entry -> entry.getValue().update(renderer.render(title, entry.getKey()), progress, color, style, flags));
    }
}
