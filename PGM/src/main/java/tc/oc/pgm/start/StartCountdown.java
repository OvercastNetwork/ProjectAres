package tc.oc.pgm.start;

import java.time.Duration;
import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchState;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.teams.TeamMatchModule;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Countdown to team huddle, or match start if huddle is disabled
 */
public class StartCountdown extends PreMatchCountdown {

    // At this duration before match start, broadcast a warning if teams will be auto-balanced
    private static final Duration BALANCE_WARNING_TIME = Duration.ofSeconds(15);
    // TODO: Avoid coupling to the team module, either by subclassing this countdown,
    // or implementing some kind of countdown listener system.
    private final @Nullable TeamMatchModule tmm;
    private final StartMatchModule smm;
    private final Duration huddle;
    private boolean autoBalanced, balanceWarningSent;
    protected final boolean forced;

    public StartCountdown(Match match, boolean forced, Duration huddle) {
        super(match);
        this.huddle = checkNotNull(huddle);
        this.forced = forced;
        this.smm = match.needMatchModule(StartMatchModule.class);
        this.tmm = match.getMatchModule(TeamMatchModule.class);
    }

    protected boolean willHuddle() {
        return Comparables.greaterThan(huddle, Duration.ZERO);
    }

    @Override
    public BaseComponent barText(Player viewer) {
        return new Component(new TranslatableComponent("countdown.matchStart.message",
                                                       secondsRemaining(ChatColor.DARK_RED)),
                             ChatColor.GREEN);
    }

    @Override
    public BarColor barColor(Player viewer) {
        return BarColor.GREEN;
    }

    @Override
    public @Nullable Duration timeUntilMatchStart() {
        return remaining == null ? null : remaining.plus(huddle);
    }

    @Override
    public void onStart(Duration remaining, Duration total) {
        super.onStart(remaining, total);
        match.ensureState(MatchState.Starting);
        this.autoBalanced = false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onTick(Duration remaining, Duration total) {
        super.onTick(remaining, total);

        if(remaining.getSeconds() >= 1 && remaining.getSeconds() <= 3) {
            // Auto-balance runs at match start as well, but try to run it a few seconds in advance
            if(this.tmm != null && !this.autoBalanced) {
                this.autoBalanced = true;
                this.tmm.balanceTeams();
            }
        }

        if(this.tmm != null && !this.autoBalanced && !this.balanceWarningSent && Comparables.lessOrEqual(remaining, BALANCE_WARNING_TIME)) {
            for(Team team : this.tmm.getTeams()) {
                if(team.isStacked()) {
                    this.balanceWarningSent = true;
                    this.getMatch().sendWarning(new TranslatableComponent("team.balanceWarning", team.getComponentName()), false);
                }
            }

            if(this.balanceWarningSent) {
                this.getMatch().playSound(COUNT_SOUND);
            }
        }
    }

    @Override
    public void onEnd(Duration total) {
        super.onEnd(total);

        if(this.tmm != null) this.tmm.balanceTeams();

        if(willHuddle()) {
            match.countdowns().start(new HuddleCountdown(getMatch()), huddle);
        } else {
            match.transitionTo(MatchState.Running);
        }
    }

    @Override
    public void onCancel(Duration remaining, Duration total, boolean manual) {
        super.onCancel(remaining, total, manual);
        if(manual) {
            smm.setAutoStart(false);
        }
        match.ensureState(MatchState.Idle);
    }

    public boolean isForced() {
        return forced;
    }
}
