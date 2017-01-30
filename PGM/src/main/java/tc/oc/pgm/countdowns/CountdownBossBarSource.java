package tc.oc.pgm.countdowns;

import java.util.Optional;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import java.time.Duration;
import tc.oc.commons.bukkit.chat.TemplateComponent;
import tc.oc.commons.bukkit.localization.MessageTemplate;
import tc.oc.commons.core.chat.Component;
import tc.oc.time.PeriodConverter;
import tc.oc.time.PeriodRenderer;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.bossbar.BossBarContent;
import tc.oc.pgm.bossbar.BossBarSource;

public abstract class CountdownBossBarSource implements BossBarSource {

    private final ChatColor messageColor, timeColor;
    private final Duration maxTime;
    private final PeriodConverter periodConverter;
    private final PeriodRenderer periodRenderer;

    protected CountdownBossBarSource(Duration maxTime, ChatColor messageColor, ChatColor timeColor, PeriodConverter periodConverter, PeriodRenderer periodRenderer) {
        this.messageColor = messageColor;
        this.timeColor = timeColor;
        this.maxTime = maxTime;
        this.periodConverter = periodConverter;
        this.periodRenderer = periodRenderer;
    }

    protected abstract MessageTemplate barMessage(Player viewer);

    protected abstract Optional<Duration> barTime(Player viewer);

    private BaseComponent renderTime(Duration time) {
        return new Component(
            // Round seconds up so we don't show "0" before the end
            periodRenderer.renderPeriod(periodConverter.toPeriod(TimeUtils.ceilSeconds(time))),
            timeColor
        );
    }

    @Override
    public Optional<BossBarContent> barContent(Player viewer) {
        return barTime(viewer).map(time -> BossBarContent.of(
            new Component(new TemplateComponent(barMessage(viewer), renderTime(time)), messageColor),
            (float) time.toMillis() / (float) maxTime.toMillis()
        ));
    }
}
