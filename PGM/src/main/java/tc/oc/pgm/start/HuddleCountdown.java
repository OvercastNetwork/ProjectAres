package tc.oc.pgm.start;

import java.time.Duration;
import javax.annotation.Nullable;

import com.github.rmsy.channels.ChannelsPlugin;
import com.github.rmsy.channels.event.ChannelMessageEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import tc.oc.api.docs.virtual.ChatDoc;
import tc.oc.commons.bukkit.channels.ChannelChatEvent;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.formatting.PeriodFormats;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.MatchState;
import tc.oc.pgm.teams.Team;

/**
 * Optional countdown between teams being finalized and match starting
 */
@ListenerScope(MatchScope.LOADED)
public class HuddleCountdown extends PreMatchCountdown implements Listener {

    public HuddleCountdown(Match match) {
        super(match);
    }

    @Override
    public BaseComponent barText(Player viewer) {
        return new Component(new TranslatableComponent("countdown.huddle.message",
                                                       secondsRemaining(ChatColor.DARK_RED)),
                             ChatColor.YELLOW);
    }

    @Override
    public BarColor barColor(Player viewer) {
        return BarColor.YELLOW;
    }

    @Override
    public @Nullable Duration timeUntilMatchStart() {
        return remaining;
    }

    @Override
    public void onStart(Duration remaining, Duration total) {
        super.onStart(remaining, total);
        match.ensureState(MatchState.Huddle);

        match.registerEvents(this);

        if(Comparables.greaterThan(total, Duration.ZERO)) {
            getMatch().getCompetitors().stream().filter(competitor -> competitor instanceof Team).forEach(competitor -> {
                competitor.sendMessage(new Component(
                    new TranslatableComponent("huddle.instructions",
                                              PeriodFormats.briefNaturalPrecise(total)),
                    ChatColor.YELLOW
                ));
            });
        }
    }

    @EventHandler
    public void onChat(ChannelChatEvent event) {
        if(event.channel().type().equals(ChatDoc.Type.SERVER)) {
            event.setCancelled(true);
            MatchPlayer player = getMatch().getPlayer(event.sender());
            if(player != null) {
                player.sendWarning(new TranslatableComponent("huddle.globalChatDisabled"), false);
            }
        }
    }

    protected void cleanup() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void onEnd(Duration total) {
        super.onEnd(total);
        cleanup();
        match.transitionTo(MatchState.Running);
    }

    @Override
    public void onCancel(Duration remaining, Duration total, boolean manual) {
        super.onCancel(remaining, total, manual);
        cleanup();
        match.ensureState(MatchState.Idle);
    }
}
