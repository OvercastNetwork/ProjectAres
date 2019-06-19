package tc.oc.pgm.join;

import javax.inject.Singleton;

import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.users.FriendJoinResponse;
import tc.oc.api.users.UserService;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.concurrent.Flexecutor;
import tc.oc.minecraft.scheduler.Sync;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.commands.CommandUtils;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.teams.TeamMatchModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Singleton
public class JoinCommands implements Commands {

    private final Flexecutor flexecutor;
    private final BukkitUserStore userStore;
    private final UserService userService;
    private final UserFinder userFinder;
    private final Map<PlayerId, PlayerId> friendJoins;

    @Inject JoinCommands(@Sync Flexecutor flexecutor, BukkitUserStore userStore, UserService userService, UserFinder userFinder) {
        this.flexecutor = flexecutor;
        this.userStore = userStore;
        this.userService = userService;
        this.userFinder = userFinder;
        this.friendJoins = new HashMap<>();
    }

    @Command(
        aliases = { "join", "jugar", "jouer", "spielen" },
        desc = "Joins the current match",
        usage = "[team] [friends...]",
        flags = "f",
        min = 0,
        max = -1
    )
    @CommandPermissions(JoinMatchModule.JOIN_PERMISSION)
    public void join(CommandContext args, CommandSender sender) throws CommandException {
        MatchPlayer player = CommandUtils.senderToMatchPlayer(sender);
        PlayerId playerId = player.getPlayerId();
        Match match = player.getMatch();
        JoinMatchModule jmm = match.needMatchModule(JoinMatchModule.class);
        TeamMatchModule tmm = match.getMatchModule(TeamMatchModule.class);

        boolean force = sender.hasPermission("pgm.join.force") && args.hasFlag('f');
        Competitor chosenParty = null;

        if(args.argsLength() > 0) {
            String team = args.getString(0);
            if(team.toLowerCase().equals("confirm")) {
                if(friendJoins.containsKey(playerId)) {
                    PlayerId requestedId = friendJoins.remove(playerId);
                    Optional<MatchPlayer> requestedPlayer = match.player(requestedId);
                    boolean success;
                    if(requestedPlayer.isPresent()) {
                        MatchPlayer requested = requestedPlayer.get();
                        success = jmm.requestJoin(player, JoinMethod.FORCE, requested.getCompetitor());
                        if(success) {
                            requested.sendMessage(new TranslatableComponent(
                                "join.friend.accepted",
                                player.getStyledName(NameStyle.VERBOSE)
                            ));
                        }
                    } else {
                        success = false;
                        jmm.requestJoin(player, JoinMethod.USER, null);
                    }
                    if(!success) {
                        friendJoins.put(playerId, requestedId);
                    }
                    return;
                } else {
                    throw new CommandException(PGMTranslations.get().t("command.confirmNotFound", sender));
                }
            } else if(team.toLowerCase().equals("obs")) {
                observe(args, sender);
                return;
            } else if(tmm != null) {
                chosenParty = tmm.bestFuzzyMatch(team);
                if(chosenParty == null) {
                    throw new CommandException(PGMTranslations.get().t("command.teamNotFound", sender));
                }
            }
            List<MatchPlayer> friends = new ArrayList<>();
            for(int i : IntStream.range(1, args.argsLength()).toArray()) {
                friends.add(match.getPlayer(Futures.getUnchecked(userFinder.findLocalPlayer(sender, args, i)).player()));
            }
            if(!friends.isEmpty()) {
                FriendJoinResponse response = Futures.getUnchecked(userService.joinFriend(playerId, friends::size));
                boolean authorized = response.authorized();
                sender.sendMessage((authorized ? ChatColor.GREEN : ChatColor.RED) + response.message());
                if(!authorized) {
                    return; // An error message was already sent
                }
            }
            friends.stream().forEach(friend -> {
                friendJoins.put(friend.getPlayerId(), playerId);
                friend.sendMessage(new TranslatableComponent(
                    "join.friend.requested",
                    player.getStyledName(NameStyle.VERBOSE)
                ));
            });
        }

        jmm.requestJoin(player, force ? JoinMethod.FORCE : JoinMethod.USER, chosenParty);
    }

    public static final String OBSERVE_COMMAND = "observe";

    @Command(
        aliases = { OBSERVE_COMMAND, "obs", "spectate", "leave" },
        desc = "Observe the current match",
        min = 0,
        max = 0
    )
    @CommandPermissions(JoinMatchModule.JOIN_PERMISSION)
    public void observe(CommandContext args, CommandSender sender) throws CommandException {
        final MatchPlayer player = CommandUtils.senderToMatchPlayer(sender);
        player.getMatch().needMatchModule(JoinMatchModule.class).requestObserve(player);
    }
}
