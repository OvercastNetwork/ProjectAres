package tc.oc.pgm.channels;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import com.github.rmsy.channels.Channel;
import com.github.rmsy.channels.ChannelsPlugin;
import com.github.rmsy.channels.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import tc.oc.commons.bukkit.chat.ComponentRenderers;
import tc.oc.commons.bukkit.util.NullCommandSender;
import tc.oc.commons.bukkit.util.OnlinePlayerMapAdapter;
import tc.oc.commons.core.util.DefaultMapAdapter;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PartyAddEvent;
import tc.oc.pgm.events.PartyRemoveEvent;
import tc.oc.pgm.events.PlayerJoinPartyEvent;
import tc.oc.pgm.events.PlayerLeavePartyEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.MultiPlayerParty;
import tc.oc.pgm.match.Party;

@ListenerScope(MatchScope.LOADED)
public class ChannelMatchModule extends MatchModule implements Listener {

    public static final String RECEIVE_ALL_PERMISSION = "pgm.chat.all.receive";
    public static final String TEAM_RECEIVE_PERMISSION = "pgm.chat.team.receive";
    public static final String TEAM_SEND_PERMISSION = "pgm.chat.team.send";

    // This dynamic permission has all party channel listening permissions as children.
    // A player with this permission receives all channels simultaneously. It is granted
    // automatically to observers that have the RECEIVE_ALL_PERMISSION.
    private final Permission matchListeningPermission;

    private final ChannelsPlugin channelsPlugin = ChannelsPlugin.get();
    private final Map<MultiPlayerParty, PartyChannel> partyChannels = new HashMap<>();

    // This is used to keep track of players' global/team chat preference. We can't just
    // check their channel through the Channels plugin because FFA players always use the
    // global channel. All players are set to team chat when they join a match, and the /t
    // command sets them to team chat. They can switch to global chat with /g, but we don't
    // know when this happens, so we check for it whenever they switch parties.
    private final Map<Player, Boolean> teamChatters;

    @Inject ChannelMatchModule(Match match, Plugin plugin) {
        this.matchListeningPermission = new Permission("pgm.chat.all." + match.getId() + ".receive", PermissionDefault.FALSE);

        final OnlinePlayerMapAdapter<Boolean> map = new OnlinePlayerMapAdapter<>(plugin);
        map.enable();
        this.teamChatters = new DefaultMapAdapter<>(map, true, false);
    }

    protected void updatePlayerChannel(MatchPlayer player) {
        if(teamChatters.get(player.getBukkit())) {
            channelsPlugin.getPlayerManager().setMembershipChannel(player.getBukkit(), getChannel(player.getParty()));
        } else {
            channelsPlugin.getPlayerManager().setMembershipChannel(player.getBukkit(), channelsPlugin.getGlobalChannel());
        }
    }

    public void setTeamChat(MatchPlayer player, boolean teamChat) {
        teamChatters.put(player.getBukkit(), teamChat);
        updatePlayerChannel(player);
    }

    public Channel getChannel(Party party) {
        if(party instanceof MultiPlayerParty) {
            return partyChannels.get(party);
        } else {
            return channelsPlugin.getGlobalChannel();
        }
    }

    protected Permission createChannelPermission(Party party) {
        Permission permission = new Permission("pgm.chat.team." + this.match.getId() + '-' + party.hashCode() + ".receive", PermissionDefault.FALSE);
        getMatch().getPluginManager().addPermission(permission);
        permission.addParent(matchListeningPermission, true);
        return permission;
    }

    protected void removeChannelPermission(Channel channel) {
        matchListeningPermission.getChildren().remove(channel.getListeningPermission().getName());
        matchListeningPermission.recalculatePermissibles();
        getMatch().getPluginManager().removePermission(channel.getListeningPermission());
    }

    protected void createChannel(Party party) {
        if(party instanceof MultiPlayerParty) {
            logger.fine("Creating channel for " + party);

            String format = ComponentRenderers.toLegacyText(party.getChatPrefix(), NullCommandSender.INSTANCE) + "{1}§f: {3}";

            PartyChannel channel = new UnfilteredPartyChannel(format, createChannelPermission(party), party);
            /*if (getMatch().getPluginManager().getPlugin("ChatModerator") == null) {
                channel = new UnfilteredPartyChannel(format,
                                                     createChannelPermission(party),
                                                     party);
            } else {
                channel = new FilteredPartyChannel(format,
                                                   createChannelPermission(party),
                                                   party,
                                                   ChatModeratorPlugin.MINIMUM_SCORE_NO_SEND,
                                                   ChatModeratorPlugin.PARTIALLY_OFFENSIVE_RATIO);
            }*/

            if(partyChannels.put((MultiPlayerParty) party, channel) != null) {
                throw new IllegalStateException("Party added multiple times");
            }
        }
    }

    @Override
    public void load() {
        super.load();

        // Let the console receive all channels
        match.getPluginManager().addPermission(matchListeningPermission);
        getMatch().getServer().getConsoleSender().addAttachment(getMatch().getPlugin()).setPermission(matchListeningPermission, true);

        // Parties may be created before the module loads
        for(Party party : getMatch().getParties()) {
            createChannel(party);
        }
    }

    @Override
    public void unload() {
        getMatch().getServer().getConsoleSender().removeAttachments(matchListeningPermission);
        getMatch().getPluginManager().removePermission(matchListeningPermission);

        super.unload();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void partyAdd(final PartyAddEvent event) {
        createChannel(event.getParty());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void partyRemove(final PartyRemoveEvent event) {
        if(event.getParty() instanceof MultiPlayerParty) {
            PartyChannel channel = partyChannels.remove(event.getParty());
            if(channel != null) {
                removeChannelPermission(channel);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void partyJoin(PlayerJoinPartyEvent event) {
        if(event.getNewParty() instanceof MultiPlayerParty) {
            PlayerManager playerManager = channelsPlugin.getPlayerManager();
            Player bukkitPlayer = event.getPlayer().getBukkit();
            PartyChannel channel = partyChannels.get(event.getNewParty());

            if(channel != null) {
                if(event.getNewParty().isObservingType() && bukkitPlayer.hasPermission(RECEIVE_ALL_PERMISSION)) {
                    // If the player is joining observers and they have the receive-all perm, let them listen to all channels
                    bukkitPlayer.addAttachment(getMatch().getPlugin()).setPermission(matchListeningPermission, true);
                } else if(bukkitPlayer.hasPermission(TEAM_RECEIVE_PERMISSION)) {
                    // Give the player listening permission for the team's channel
                    bukkitPlayer.addAttachment(getMatch().getPlugin()).setPermission(channel.getListeningPermission(), true);
                }

                // If their sending channel was previously set to a team channel, switch it to the new team's channel
                if(playerManager.getMembershipChannel(bukkitPlayer) instanceof PartyChannel) {
                    playerManager.setMembershipChannel(bukkitPlayer, channel);
                }
            }
        }

        updatePlayerChannel(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void partyLeave(PlayerLeavePartyEvent event) {
        if(event.getOldParty() instanceof MultiPlayerParty) {
            PlayerManager playerManager = channelsPlugin.getPlayerManager();
            Player bukkitPlayer = event.getPlayer().getBukkit();
            PartyChannel channel = partyChannels.get(event.getOldParty());

            if(channel != null) {
                bukkitPlayer.removeAttachments(channel.getListeningPermission());
                bukkitPlayer.removeAttachments(matchListeningPermission);

                // Whenever the player leaves a party with its own channel, check if that is the player's current channel,
                // and if it's not, then set their team chat setting to false. This is the only way to find out when they
                // switch to global chat, because the Channels plugin doesn't provide any notifcation.
                if(playerManager.getMembershipChannel(bukkitPlayer) != channel) {
                    teamChatters.put(bukkitPlayer, false);
                }
            }
        }
    }
}
