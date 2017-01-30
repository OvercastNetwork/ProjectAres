package tc.oc.pgm.countdowns;

import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.Player;
import java.time.Duration;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.formatting.PeriodFormats;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.bossbar.BossBarMatchModule;
import tc.oc.pgm.bossbar.BossBarSource;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;

public abstract class MatchCountdown implements Countdown, BossBarSource {

    protected final Match match;
    protected final BossBarMatchModule bbmm;
    protected final @Nullable Set<MatchPlayer> viewers;

    protected @Nullable Duration remaining;
    protected @Nullable Duration total;

    public MatchCountdown(Match match) {
        this(match, null);
    }

    public MatchCountdown(Match match, @Nullable Set<MatchPlayer> viewers) {
        this.match = match;
        this.bbmm = match.needMatchModule(BossBarMatchModule.class);
        this.viewers = viewers == null ? null : ImmutableSet.copyOf(viewers);
    }

    public Match getMatch() {
        return this.match;
    }

    protected Set<MatchPlayer> viewers() {
        return viewers != null ? viewers : match.getPlayers();
    }

    @Override
    public boolean isVisible(Player viewer) {
        return remaining != null && (viewers == null || match.player(viewer)
                                                             .filter(viewers::contains)
                                                             .isPresent());
    }

    @Override
    public float barProgress(Player viewer) {
        if(total == null || remaining == null) return 0;

        if(Duration.ZERO.equals(total)) {
            return 0f;
        } else if(TimeUtils.isInfPositive(total)) {
            return 1f;
        } else {
            return barProgress(remaining, total);
        }
    }

    protected float barProgress(Duration remaining, Duration total) {
        return (float) remaining.getSeconds() / total.getSeconds();
    }

    protected boolean shouldShowTitle(MatchPlayer viewer) {
        return false;
    }

    @Override
    public void onStart(Duration remaining, Duration total) {
        this.remaining = remaining;
        this.total = total;

        if(viewers == null) {
            bbmm.add(this);
        } else {
            bbmm.add(this, viewers.stream().map(MatchPlayer::getBukkit));
        }
    }

    @Override
    public void onTick(Duration remaining, Duration total) {
        this.remaining = remaining;
        this.total = total;

        bbmm.render(this);

        final Component title = new Component(String.valueOf(remaining.getSeconds()), ChatColor.YELLOW);
        for(MatchPlayer viewer : viewers()) {
            if(shouldShowTitle(viewer)) {
                viewer.showTitle(title, Components.blank(), 0, 5, 15);
            }
        }
    }

    @Override
    public void onEnd(Duration total) {
        bbmm.remove(this);
    }

    @Override
    public void onCancel(Duration remaining, Duration total, boolean manual) {
        bbmm.remove(this);
    }

    protected BaseComponent secondsRemaining(ChatColor color) {
        long seconds = remaining.getSeconds();
        if(seconds == 1) {
            return new TranslatableComponent("countdown.singularCompound", new Component("1", color));
        } else {
            return new TranslatableComponent("countdown.pluralCompound", new Component(String.valueOf(seconds), color));
        }
    }

    protected BaseComponent colonTime() {
        return PeriodFormats.formatColons(remaining);
    }
}
