package tc.oc.pgm.timelimit;

import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import java.time.Duration;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.countdowns.MatchCountdown;
import tc.oc.pgm.match.Match;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Like all countdowns, this one cannot be reused. It is created by a {@link TimeLimit}
 * and started immediately. It stores the {@link TimeLimit} that created it, and notifies
 * it when the countdown ends or is cancelled.
 */
public class TimeLimitCountdown extends MatchCountdown {

    private static final BukkitSound NOTICE_SOUND = new BukkitSound(Sound.BLOCK_NOTE_PLING, 1f, 1.19f);           // Significant moments
    private static final BukkitSound IMMINENT_SOUND = new BukkitSound(Sound.UI_BUTTON_CLICK, 0.25f, 2f);       // Last 30 seconds
    private static final BukkitSound CRESCENDO_SOUND = new BukkitSound(Sound.BLOCK_PORTAL_TRIGGER, 1f, 0.78f);    // Last few seconds

    private final TimeLimit timeLimit;

    public TimeLimitCountdown(Match match, TimeLimit timeLimit) {
        super(match);
        this.timeLimit = checkNotNull(timeLimit);
    }

    public @Nullable Duration remaining() {
        return remaining;
    }

    @Override
    public BaseComponent barText(Player viewer) {
        return new Component(new TranslatableComponent("match.timeRemaining", new Component(colonTime(), textColor())),
                             ChatColor.AQUA);
    }

    @Override
    public boolean isVisible(Player viewer) {
        return super.isVisible(viewer) && this.timeLimit.getShow();
    }

    @Override
    public BarColor barColor(Player viewer) {
        long seconds = remaining.getSeconds();
        if(seconds > 60) {
            return BarColor.GREEN;
        } else if(seconds > 30) {
            return BarColor.YELLOW;
        } else {
            return BarColor.RED;
        }
    }

    protected ChatColor textColor() {
        long seconds = remaining.getSeconds();
        if(seconds > 60) {
            return ChatColor.GREEN;
        } else if(seconds > 30) {
            return ChatColor.YELLOW;
        } else if(seconds > 5) {
            return ChatColor.GOLD;
        } else {
            return ChatColor.DARK_RED;
        }
    }

    protected boolean shouldBeep() {
        long secondsLeft = remaining.getSeconds();
        return secondsLeft > 0 &&
               (secondsLeft % 300 == 0 ||                           // every 5 minutes
                (secondsLeft % 60 == 0 && secondsLeft <= 300) ||    // every minute for the last 5 minutes
                (secondsLeft % 10 == 0 && secondsLeft <= 30) ||     // every 10 seconds for the last 30 seconds
                secondsLeft <= 5);                                  // every second for the last 5 seconds
    }

    @Override
    public void onTick(Duration remaining, Duration total) {
        super.onTick(remaining, total);

        if(this.timeLimit.getShow()) {
            long secondsLeft = remaining.getSeconds();
            if(secondsLeft > 30) {
                if(this.shouldBeep()) {
                    this.getMatch().playSound(NOTICE_SOUND);
                }
            }
            else if(secondsLeft > 0) {
                // Tick for the last 30 seconds
                this.getMatch().playSound(IMMINENT_SOUND);
            }
            if(secondsLeft == 5) {
                // Play the portal crescendo sound up to the last moment
                this.getMatch().playSound(CRESCENDO_SOUND);
            }
        }
    }

    protected void freeze(Duration remaining) {
        this.remaining = remaining;
        bbmm.render(this);
    }

    @Override
    public void onEnd(Duration total) {
        super.onEnd(total);
        this.freeze(Duration.ZERO);
        timeLimit.onEnd();
    }

    @Override
    public void onCancel(Duration remaining, Duration total, boolean manual) {
        super.onCancel(remaining, total, manual);
        if(this.getMatch().isFinished()) {
            this.freeze(remaining);
        } else if(manual) {
            timeLimit.onCancel(manual);
        }
    }
}
