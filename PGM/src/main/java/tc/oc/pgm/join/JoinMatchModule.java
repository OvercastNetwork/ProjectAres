package tc.oc.pgm.join;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.eventbus.EventBus;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.Arena;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.Ticket;
import tc.oc.api.games.TicketStore;
import tc.oc.api.model.ModelDispatcher;
import tc.oc.api.model.ModelListener;
import tc.oc.commons.bukkit.event.PlayerServerChangeEvent;
import tc.oc.commons.bukkit.teleport.Teleporter;
import tc.oc.commons.bukkit.ticket.TicketBooth;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.CommandBinder;
import tc.oc.commons.core.formatting.PeriodFormats;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPlayerAddEvent;
import tc.oc.pgm.events.MatchPreCommitEvent;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.match.inject.MatchModuleFixtureManifest;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.teams.TeamMatchModule;
import tc.oc.pgm.timelimit.TimeLimitMatchModule;

@ModuleDescription(name = "Join")
@ListenerScope(MatchScope.LOADED)
public class JoinMatchModule extends MatchModule implements Listener, JoinHandler, ModelListener {

    public static class Manifest extends MatchModuleFixtureManifest<JoinMatchModule> {
        @Override protected void configure() {
            super.configure();

            new CommandBinder(binder())
                .register(JoinCommands.class);
        }
    }

    public static final String JOIN_PERMISSION = "pgm.join";
    public static final String JOIN_FULL_PERMISSION = "pgm.join.full";
    public static final String PRIORITY_KICK_PERMISSION = JOIN_FULL_PERMISSION;
    public static final String JOIN_OBSERVERS_PERMISSION = "pgm.join.choose.observing";

    @Inject private JoinConfiguration config;
    @Inject private QueuedParticipants queuedParticipants;
    @Inject private Server localServer;
    @Inject private BukkitUserStore userStore;
    @Inject private OnlinePlayers onlinePlayers;
    @Inject private EventBus eventBus;
    @Inject private Teleporter teleporter;
    @Inject private TicketStore tickets;
    @Inject private TicketBooth ticketBooth;
    @Inject private ModelDispatcher modelDispatcher;

    private final Set<JoinHandler> handlers = new LinkedHashSet<>();

    @Override
    public void load() {
        super.load();
        eventBus.register(this);
        getMatch().addParty(queuedParticipants);
        ticketBooth.setPlayHandler(playHandler);
        modelDispatcher.subscribe(this);
    }

    @Override
    public void unload() {
        modelDispatcher.unsubscribe(this);
        ticketBooth.removePlayHandler(playHandler);
        eventBus.unregister(this);
        super.unload();
    }

    public void registerHandler(JoinHandler handler) {
        handlers.add(handler);
    }

    public boolean canJoinFull(MatchPlayer joining) {
        return !config.capacity() || (config.overfill() && joining.getBukkit().hasPermission(JOIN_FULL_PERMISSION));
    }

    public boolean canPriorityKick(MatchPlayer joining) {
        return config.priorityKick() && joining.getBukkit().hasPermission(PRIORITY_KICK_PERMISSION) && !getMatch().hasStarted();
    }

    public boolean canJoinMid() {
        return config.midMatch();
    }

    public boolean isRemoteJoin() {
        return localServer.game_id() != null;
    }

    @Override
    public JoinResult queryJoin(MatchPlayer joining, JoinRequest request) {
        // Player does not have permission to voluntarily join
        if(!joining.getBukkit().hasPermission(JOIN_PERMISSION)) {
            return JoinDenied.error("command.gameplay.join.joinDenied");
        }

        // If mid-match join is disabled, player cannot join for the first time after the match has started
        if(!canJoinMid() && getMatch().isCommitted() && !getMatch().hasEverParticipated(joining.getPlayerId())) {
            return JoinDenied.friendly("command.gameplay.join.matchStarted");
        }

        if(getMatch().isFinished()) {
            // This message should NOT look like an error, because remotely joining players will see it often.
            return JoinDenied.friendly("command.gameplay.join.matchFinished");
        }

        JoinResult best = new JoinDenied(false, true, new TranslatableComponent("command.gameplay.join.notSupported")) {
            @Override public boolean isFallback() { return true; }
        };

        for(JoinHandler handler : handlers) {
            final JoinResult result = handler.queryJoin(joining, request);
            if(result != null && result.compareTo(best) < 0) best = result;
        }

        return best;
    }

    @Override
    public boolean join(MatchPlayer joining, JoinRequest request, JoinResult result) {
        result.output().forEach(joining::sendMessage);

        if(result instanceof JoinQueued) {
            queueToJoin(joining);
            return true;
        }

        if(!result.isAllowed()) return true;

        for(JoinHandler handler : handlers) {
            if(handler.join(joining, request, result)) return true;
        }

        return false;
    }

    public boolean requestJoin(MatchPlayer joining, JoinRequest request) {
        final Player joiner = joining.getBukkit();
        if(isRemoteJoin() && request.method() != JoinMethod.REMOTE && !isLocalParticipant(joining)) {
            ticketBooth.playLocalGame(joiner);
            return true;
        } else {
            final Arena arena = ticketBooth.localArena();
            if(arena == null || !arena.equals(ticketBooth.currentArena(joiner))) {
                ticketBooth.leaveGame(joiner, false);
            }
            return join(joining, request, queryJoin(joining, request));
        }
    }

    public boolean requestJoin(MatchPlayer joining, JoinMethod method, @Nullable Competitor competitor) {
        return requestJoin(joining, new JoinRequest(method, competitor));
    }

    public boolean requestJoin(MatchPlayer joining, JoinMethod method) {
        return requestJoin(joining, method, null);
    }

    public boolean observe(MatchPlayer leaving) {
        final Party observers = getMatch().getDefaultParty();
        leaving.sendMessage(new TranslatableComponent("team.join", observers.getComponentName()));
        return getMatch().setPlayerParty(leaving, observers, false);
    }

    public boolean requestObserve(MatchPlayer leaving) {
        if(cancelQueuedJoin(leaving)) return true;

        if(leaving.isObservingType()) {
            leaving.sendWarning(new TranslatableComponent("command.gameplay.leave.alreadyOnObservers"), false);
            return false;
        }

        if(isRemoteJoin()) {
            ticketBooth.leaveGame(leaving.getBukkit(), false);
        }

        if(!leaving.getBukkit().hasPermission(JOIN_OBSERVERS_PERMISSION)) {
            leaving.sendWarning(new TranslatableComponent("command.gameplay.leave.leaveDenied"), false);
            return false;
        }

        if(config.commitPlayers() && leaving.isCommitted()) {
            leaving.sendWarning(new TranslatableComponent("command.gameplay.leave.leaveDenied"), false);
            return false;
        }

        return observe(leaving);
    }

    public QueuedParticipants getQueuedParticipants() {
        return queuedParticipants;
    }

    public boolean isQueuedToJoin(MatchPlayer joining) {
        return joining.inParty(queuedParticipants);
    }

    public boolean queueToJoin(MatchPlayer joining) {
        boolean joined = getMatch().setPlayerParty(joining, queuedParticipants, false);
        if(joined) {
            joining.sendMessage(new TranslatableComponent("ffa.join"));
        }

        joining.sendMessage(new Component(new TranslatableComponent("team.join.deferred.request"), ChatColor.YELLOW)); // Always show this message

        if(getMatch().hasMatchModule(TeamMatchModule.class)) {
            // If they are joining a team, show them a scary warning about leaving the match
            joining.sendMessage(
                new Component(new TranslatableComponent(
                    "team.join.forfeitWarning",
                    new Component(new TranslatableComponent("team.join.forfeitWarning.emphasis.warning"), ChatColor.RED),
                    new Component(new TranslatableComponent("team.join.forfeitWarning.emphasis.playUntilTheEnd"), ChatColor.RED),
                    new Component(new TranslatableComponent("team.join.forfeitWarning.emphasis.doubleLoss"), ChatColor.RED),
                    new Component(new TranslatableComponent("team.join.forfeitWarning.emphasis.suspension"), ChatColor.RED)
                ), ChatColor.DARK_RED)
            );

            TimeLimitMatchModule tlmm = getMatch().getMatchModule(TimeLimitMatchModule.class);
            if(tlmm != null && tlmm.getTimeLimit() != null) {
                joining.sendMessage(new Component(new TranslatableComponent(
                    "team.join.forfeitWarning.timeLimit",
                    new Component(PeriodFormats.briefNaturalPrecise(tlmm.getTimeLimit().getDuration()), ChatColor.AQUA),
                    new Component("/" + JoinCommands.OBSERVE_COMMAND, ChatColor.GOLD)
                ), ChatColor.DARK_RED, ChatColor.BOLD));
            } else {
                joining.sendMessage(new Component(new TranslatableComponent(
                    "team.join.forfeitWarning.noTimeLimit",
                    new Component("/" + JoinCommands.OBSERVE_COMMAND, ChatColor.GOLD)
                ), ChatColor.DARK_RED, ChatColor.BOLD));
            }
        }

        return joined;
    }

    public boolean cancelQueuedJoin(MatchPlayer joining) {
        if(!isQueuedToJoin(joining)) return false;
        if(getMatch().setPlayerParty(joining, getMatch().getDefaultParty(), false)) {
            joining.sendMessage(new Component(new TranslatableComponent("team.join.deferred.cancel"), ChatColor.YELLOW));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void queuedJoin(QueuedParticipants queue) {
        // Give all handlers a chance to bulk join
        for(JoinHandler handler : handlers) {
            if(queue.getPlayers().isEmpty()) break;
            handler.queuedJoin(queue);
        }

        // Send any leftover players to obs
        for(MatchPlayer joining : queue.getOrderedPlayers()) {
            getMatch().setPlayerParty(joining, getMatch().getDefaultParty(), false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchCommit(MatchPreCommitEvent event) {
        queuedJoin(queuedParticipants);
    }

    @EventHandler
    public void onServerChange(PlayerServerChangeEvent event) {
        MatchPlayer player = getMatch().getPlayer(event.getPlayer());
        if(config.commitPlayers() && player != null && player.isCommitted() && !getMatch().isFinished()) {
            event.setCancelled(true, new TranslatableComponent("engagement.committed"));
        }
    }

    private boolean isLocalParticipant(MatchPlayer player) {
        return isLocalParticipant(tickets.tryUser(player.getPlayerId()));
    }

    private boolean isLocalParticipant(@Nullable Ticket ticket) {
        return ticket != null && localServer._id().equals(ticket.server_id());
    }

    @EventHandler
    public void onLogin(MatchPlayerAddEvent event) {
        if(config.commitPlayers() && getMatch().isCommitted() && !getMatch().isFinished()) {
            final Competitor competitor = getMatch().getLastCompetitor(event.getPlayerId());
            if(competitor != null) {
                // Committed player is reconnecting
                getMatch().setPlayerParty(event.getPlayer(), competitor, false);
                return;
            }
        }

        if(isRemoteJoin() && isLocalParticipant(event.getPlayer())) {
            // Player has a remote ticket to play on this server
            requestJoin(event.getPlayer(), JoinMethod.REMOTE);
        }
    }

    @HandleModel
    public void ticketUpdated(@Nullable Ticket before, @Nullable Ticket after, Ticket latest) {
        if(isRemoteJoin()) match.player(latest.user()).ifPresent(player -> {
            final boolean isPlaying = isLocalParticipant(after);
            if(!player.isParticipatingType() && isPlaying) {
                requestJoin(player, JoinMethod.REMOTE);
            } else if(player.isParticipatingType() && !isPlaying) {
                observe(player);
            }
        });
    };

    private final TicketBooth.PlayHandler playHandler = player -> {
        final MatchPlayer mp = match.getPlayer(player);
        if(mp != null) {
            requestJoin(mp, JoinMethod.USER);
            return true;
        }
        return false;
    };
}
