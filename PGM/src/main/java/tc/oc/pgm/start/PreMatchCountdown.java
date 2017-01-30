package tc.oc.pgm.start;

import java.time.Duration;
import javax.annotation.Nullable;

import com.google.common.collect.Range;
import org.bukkit.Sound;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.pgm.countdowns.MatchCountdown;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;

/**
 * Common base for countdowns leading up to match start
 */
public abstract class PreMatchCountdown extends MatchCountdown {

    protected static final BukkitSound COUNT_SOUND = new BukkitSound(Sound.BLOCK_NOTE_PLING, Float.MAX_VALUE, 1.19f);

    public PreMatchCountdown(Match match) {
        super(match);
    }

    @Override
    public void onTick(Duration remaining, Duration total) {
        super.onTick(remaining, total);

        if(matchAboutToStart()) {
            getMatch().playSound(COUNT_SOUND);
        }
    }

    public abstract @Nullable Duration timeUntilMatchStart();

    protected boolean matchAboutToStart() {
        return remaining != null &&
               Range.openClosed(Duration.ZERO, Duration.ofSeconds(3))
                    .contains(timeUntilMatchStart());
    }

    @Override
    protected boolean shouldShowTitle(MatchPlayer viewer) {
        return viewer.isParticipatingType() && matchAboutToStart();
    }
}
