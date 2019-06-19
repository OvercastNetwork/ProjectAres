package tc.oc.pgm.ffa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.bukkit.nick.PlayerIdentityChangeEvent;
import tc.oc.commons.bukkit.util.NullCommandSender;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.util.Optionals;
import tc.oc.commons.core.util.UsageCollection;
import tc.oc.commons.bukkit.chat.Links;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PartyRenameEvent;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.join.JoinAllowed;
import tc.oc.pgm.join.JoinConfiguration;
import tc.oc.pgm.join.JoinDenied;
import tc.oc.pgm.join.JoinHandler;
import tc.oc.pgm.join.JoinMatchModule;
import tc.oc.pgm.join.JoinMethod;
import tc.oc.pgm.join.JoinRequest;
import tc.oc.pgm.join.JoinResult;
import tc.oc.pgm.join.QueuedParticipants;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.start.StartMatchModule;
import tc.oc.pgm.start.UnreadyReason;

import static tc.oc.commons.core.util.Utils.getInstanceOf;

@ListenerScope(MatchScope.LOADED)
public class FreeForAllMatchModule extends MatchModule implements Listener, JoinHandler {

    // 10 different colors that tributes are allowed to have
    private static final ImmutableList<ChatColor> COLORS = ImmutableList.of(
            ChatColor.RED,
            ChatColor.BLUE,
            ChatColor.GREEN,
            ChatColor.YELLOW,
            ChatColor.LIGHT_PURPLE,
            ChatColor.GOLD,
            ChatColor.DARK_GREEN,
            ChatColor.DARK_AQUA,
            ChatColor.DARK_PURPLE,
            ChatColor.DARK_RED
    );

    class NeedMorePlayers implements UnreadyReason {
        final int players;

        NeedMorePlayers(int players) {
            this.players = players;
        }

        @Override
        public BaseComponent getReason() {
            if(players == 1) {
                return new TranslatableComponent("start.needMorePlayers.ffa.singular",
                                                 new Component(String.valueOf(players), ChatColor.AQUA));
            } else {
                return new TranslatableComponent("start.needMorePlayers.ffa.plural",
                                                 new Component(String.valueOf(players), ChatColor.AQUA));
            }
        }

        @Override
        public boolean canForceStart() {
            return true;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{players=" + players + "}";
        }
    };

    @Inject private Tribute.Factory tributeFactory;
    @Inject private FreeForAllOptions options;
    @Inject private JoinConfiguration joinConfiguration;

    private JoinMatchModule jmm;

    private @Nullable Integer minPlayers, maxPlayers, maxOverfill;
    private int minPlayersNeeded = Integer.MAX_VALUE;
    private final Map<PlayerId, Tribute> tributes = new HashMap<>();
    private final UsageCollection<ChatColor> colors;

    public FreeForAllMatchModule(Match match) {
        super(match);
        this.colors = new UsageCollection<>(match.getRandom(), COLORS);
    }

    public FreeForAllOptions getOptions() {
        return options;
    }

    public int getMinPlayers() {
        return minPlayers != null ? minPlayers : options.minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers != null ? maxPlayers : options.maxPlayers;
    }

    public int getMaxOverfill() {
        return maxOverfill != null ? maxOverfill : options.maxOverfill;
    }

    private void updatePlayerLimits() {
        getMatch().setPlayerLimits(Range.closed(getMinPlayers(), getMaxPlayers()));
    }

    public void setMinPlayers(@Nullable Integer minPlayers) {
        this.minPlayers = minPlayers;
        updatePlayerLimits();
        updateReadiness();
    }

    public void setMaxPlayers(@Nullable Integer maxPlayers, @Nullable Integer maxOverfill) {
        this.maxPlayers = maxPlayers;
        this.maxOverfill = maxOverfill;
        updatePlayerLimits();
    }

    @Override
    public void load() {
        super.load();

        jmm = match.needMatchModule(JoinMatchModule.class);
        jmm.registerHandler(this);

        updatePlayerLimits();
        updateReadiness();
    }

    protected void updateReadiness() {
        if(getMatch().hasStarted()) return;

        int players = 0;
        for(Competitor competitor : getMatch().getCompetitors()) {
            if(competitor instanceof Tribute) {
                players += competitor.getPlayers().size();
            }
        }

        int playersNeeded = getMinPlayers() - players;

        final StartMatchModule smm = getMatch().needMatchModule(StartMatchModule.class);
        if(playersNeeded > 0) {
            smm.addUnreadyReason(new NeedMorePlayers(playersNeeded));
        } else {
            smm.removeUnreadyReason(NeedMorePlayers.class);

            // Whenever playersNeeded reaches a new minimum, reset the unready timeout
            if(playersNeeded < minPlayersNeeded) {
                minPlayersNeeded = playersNeeded;
                smm.restartUnreadyTimeout();
            }
        }
    }

    protected Optional<Tribute> tryTribute(MatchPlayer player) {
        return Optional.ofNullable(tributes.get(player.getPlayerId()));
    }

    protected Tribute getTribute(MatchPlayer player) {
        return tryTribute(player).orElseGet(() -> {
            final Tribute tribute = tributeFactory.create(player, options.colors ? colors.next() : null);
            tributes.put(player.getPlayerId(), tribute);
            logger.fine("Created " + tribute);
            return tribute;
        });
    }

    protected boolean canPriorityKick(MatchPlayer joining) {
        if(!jmm.canPriorityKick(joining)) return false;

        for(MatchPlayer player : getMatch().getParticipatingPlayers()) {
            if(!jmm.canPriorityKick(player)) return true;
        }

        return false;
    }

    protected boolean priorityKick(MatchPlayer joining) {
        if(!jmm.canPriorityKick(joining)) return false;

        List<MatchPlayer> kickable = new ArrayList<>();
        for(MatchPlayer player : getMatch().getParticipatingPlayers()) {
            if(!jmm.canPriorityKick(player)) kickable.add(player);
        }
        if(kickable.isEmpty()) return false;

        MatchPlayer kickMe = kickable.get(getMatch().getRandom().nextInt(kickable.size()));

        kickMe.sendWarning(new TranslatableComponent("gameplay.ffa.kickedForPremium"), false);
        kickMe.sendMessage(Links.shopPlug("shop.plug.ffa.neverKicked"));
        kickMe.playSound(Sound.ENTITY_VILLAGER_HURT, kickMe.getBukkit().getLocation(), 1, 1);

        getMatch().setPlayerParty(kickMe, getMatch().getDefaultParty(), false);

        return true;
    }

    @Override
    public @Nullable JoinResult queryJoin(MatchPlayer joining, JoinRequest request) {
        if(Optionals.isInstance(joining.partyMaybe(), Tribute.class)) {
            return JoinDenied.error("command.gameplay.join.alreadyJoined");
        }

        final Optional<Tribute> chosen = getInstanceOf(request.competitor(), Tribute.class);
        if(chosen.isPresent() && !chosen.get().getPlayerId().equals(joining.getPlayerId())) {
            return null;
        }

        if(request.method() == JoinMethod.USER) {
            int players = getMatch().getParticipatingPlayers().size();

            if(jmm.canJoinFull(joining)) {
                if(players >= getMaxOverfill()) {
                    if(canPriorityKick(joining)) {
                        return JoinAllowed.auto(true);
                    } else {
                        return JoinDenied.unavailable("autoJoin.matchFull");
                    }
                }
            } else {
                if(players >= getMaxPlayers()) {
                    return JoinDenied.unavailable("autoJoin.matchFull")
                        .also(Links.shopPlug("shop.plug.ffa.joinFull"));
                }
            }
        }

        return JoinAllowed.auto(false);
    }

    @Override
    public boolean join(MatchPlayer joining, JoinRequest request, JoinResult result) {
        if(result.isAllowed()) {
            if(!forceJoin(joining)) {
                return false;
            }
            if(result.priorityKickRequired()) {
                priorityKick(joining);
            }
            return true;
        }

        return false;
    }

    @Override
    public void queuedJoin(QueuedParticipants queue) {
        final JoinRequest request = new JoinRequest(JoinMethod.USER, null);
        for(MatchPlayer player : queue.getOrderedPlayers()) {
            join(player, request, queryJoin(player, request));
        }
    }

    public boolean forceJoin(MatchPlayer joining) {
        if(Optionals.isInstance(joining.partyMaybe(), Tribute.class)) {
            joining.sendWarning(new TranslatableComponent("command.gameplay.join.alreadyJoined"), false);
        }

        return getMatch().setPlayerParty(joining, getTribute(joining), false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPartyChange(PlayerPartyChangeEvent event) {
        if(event.getNewParty() instanceof Tribute) {
            event.getPlayer().sendMessage(new TranslatableComponent("ffa.join"));
        }
        updateReadiness();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onIdentityChange(PlayerIdentityChangeEvent event) {
        MatchPlayer player = getMatch().getPlayer(event.getPlayer());
        if(player != null && player.getParty() instanceof Tribute) {
            getMatch().callEvent(new PartyRenameEvent(player.getParty(),
                    event.getOldIdentity().getName(NullCommandSender.INSTANCE),
                    event.getNewIdentity().getName(NullCommandSender.INSTANCE)));
        }
    }
}
