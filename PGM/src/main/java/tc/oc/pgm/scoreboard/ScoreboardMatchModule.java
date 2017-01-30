package tc.oc.pgm.scoreboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.Iterables;
import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingCallbackManager;
import me.anxuiz.settings.bukkit.PlayerSettingCallback;
import me.anxuiz.settings.bukkit.PlayerSettings;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.commons.core.util.MapUtils;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PartyAddEvent;
import tc.oc.pgm.events.PartyRemoveEvent;
import tc.oc.pgm.events.PartyRenameEvent;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.ghostsquadron.GhostSquadronMatchModule;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Party;

@ListenerScope(MatchScope.LOADED)
public class ScoreboardMatchModule extends MatchModule implements Listener {

    protected final Map<Party, Scoreboard> partyScoreboards = new HashMap<>();
    protected final Scoreboard hiddenScoreboard;
    private final SettingCallbackManager settingCallbackManager;

    protected final PlayerSettingCallback settingCallback = new PlayerSettingCallback() {
        @Override
        public void notifyChange(Player player, Setting setting, @Nullable Object oldValue, @Nullable Object newValue) {
            MatchPlayer matchPlayer = getMatch().getPlayer(player);
            if(matchPlayer != null) {
                updatePlayer(matchPlayer, matchPlayer.getParty(), (Boolean) newValue);
            }
        }
    };

    @Inject ScoreboardMatchModule(ScoreboardManager scoreboardManager, SettingCallbackManager settingCallbackManager) {
        this.hiddenScoreboard = scoreboardManager.getNewScoreboard();
        this.settingCallbackManager = settingCallbackManager;
    }

    @Override
    public void load() {
        super.load();
        match.parties().forEach(this::getScoreboard);
        settingCallbackManager.addCallback(ScoreboardSettings.SHOW_SCOREBOARD, settingCallback);
    }

    @Override
    public void unload() {
        settingCallbackManager.removeCallback(ScoreboardSettings.SHOW_SCOREBOARD, settingCallback);
        super.unload();
    }

    protected boolean getShowSetting(MatchPlayer player) {
        return PlayerSettings.getManager(player.getBukkit()).getValue(ScoreboardSettings.SHOW_SCOREBOARD, Boolean.class);
    }

    protected String getScoreboardTeamName(@Nullable Party party) {
        return party == null ? null : StringUtils.truncate(party.getDefaultName(), 16);
    }

    protected void updatePartyScoreboardTeam(Party party, Team team, boolean forObservers) {
        logger.fine("Updating scoreboard team " + toString(team) + " for party " + party);

        team.setDisplayName(party.getName());
        team.setPrefix(party.getColor().toString());
        team.setSuffix(ChatColor.WHITE.toString());

        team.setCanSeeFriendlyInvisibles(true);
        team.setAllowFriendlyFire(getMatch().getMapInfo().friendlyFire);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

        if(!forObservers && party instanceof Competitor) {
            Team.OptionStatus nameTags = ((Competitor) party).getNameTagVisibility();

            // #HACK until this is fixed https://bugs.mojang.com/browse/MC-48730 we need to
            // ensure enemy name tags are always hidden for GS.
            if(getMatch().getMatchModule(GhostSquadronMatchModule.class) != null) {
                switch(nameTags) {
                    case ALWAYS: nameTags = Team.OptionStatus.FOR_OWN_TEAM; break;
                    case FOR_OTHER_TEAMS: nameTags = Team.OptionStatus.NEVER; break;
                }
            }

            team.setOption(Team.Option.NAME_TAG_VISIBILITY, nameTags);
        } else {
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        }
    }

    protected Team createPartyScoreboardTeam(Party party, Scoreboard scoreboard, boolean forObservers) {
        logger.fine("Creating team for party " + party + " on scoreboard " + toString(scoreboard));

        Team team = scoreboard.registerNewTeam(getScoreboardTeamName(party));
        updatePartyScoreboardTeam(party, team, forObservers);
        for(MatchPlayer player : party.getPlayers()) {
            team.addPlayer(player.getBukkit());
        }

        return team;
    }

    public Scoreboard getScoreboard(Party party) {
        return MapUtils.computeIfAbsent(partyScoreboards, party, () -> {
            // Create the new party's scoreboard
            Scoreboard scoreboard = getMatch().getServer().getScoreboardManager().getNewScoreboard();
            logger.fine("Created scoreboard " + toString(scoreboard) + " for party " + party);

            // Add all other party scoreboards to the new party's scoreboard
            for(Party oldParty : partyScoreboards.keySet()) {
                createPartyScoreboardTeam(oldParty, scoreboard, !(party instanceof Competitor));
            }

            // Add the new party to its own scoreboard
            createPartyScoreboardTeam(party, scoreboard, !(party instanceof Competitor));

            // Add the new party to the hidden scoreboard
            createPartyScoreboardTeam(party, hiddenScoreboard, false);

            // Add the new party to all other party scoreboards
            for(Map.Entry<Party, Scoreboard> entry : partyScoreboards.entrySet()) {
                createPartyScoreboardTeam(party, entry.getValue(), !(entry.getKey() instanceof Competitor));
            }
            return scoreboard;
        });
    }

    protected void removePartyScoreboard(Party party) {
        // Remove and tear down the leaving party's scoreboard
        Scoreboard scoreboard = partyScoreboards.remove(party);
        if(scoreboard != null) {
            for(Objective objective : scoreboard.getObjectives()) {
                objective.unregister();
            }
            for(Team team : scoreboard.getTeams()) {
                team.unregister();
            }
        }

        logger.fine("Removed scoreboard " + toString(scoreboard) + " for party " + party);

        // Remove the leaving party from all other scoreboards
        String name = getScoreboardTeamName(party);
        for(Scoreboard otherScoreboard : getScoreboards()) {
            Team team = otherScoreboard.getTeam(name);
            if(team != null) {
                logger.fine("Unregistering team " + toString(team) + " from scoreboard " + toString(otherScoreboard));
                team.unregister();
            }
        }

    }

    protected void changePlayerScoreboard(MatchPlayer player, @Nullable Party oldParty, @Nullable Party newParty) {
        // Change the player's team in all scoreboards
        String teamName = getScoreboardTeamName(newParty != null ? newParty : oldParty);
        for(Scoreboard scoreboard : getScoreboards()) {
            if(newParty != null) {
                Team team = scoreboard.getTeam(teamName);
                logger.fine("Adding player " + player + " to team " + toString(team) + " on scoreboard " + toString(scoreboard));
                team.addPlayer(player.getBukkit()); // This also removes the player from their old team, if any
            } else if(oldParty != null) {
                Team team = scoreboard.getTeam(teamName);
                logger.fine("Removing player " + player + " from team " + toString(team) + " on scoreboard " + toString(scoreboard));
                team.removePlayer(player.getBukkit());
            }
        }

        // Set the player's scoreboard
        if(newParty != null) {
            updatePlayer(player, newParty, getShowSetting(player));
        }
    }

    protected void updatePlayer(MatchPlayer player, Party party, boolean show) {
        if(show) {
            Scoreboard scoreboard = partyScoreboards.get(party);
            logger.fine("Setting player " + player + " to scoreboard " + toString(scoreboard));
            player.getBukkit().setScoreboard(scoreboard);
        } else {
            logger.fine("Setting player " + player + " to hidden scoreboard");
            player.getBukkit().setScoreboard(getHiddenScoreboard());
        }
    }

    public Scoreboard getHiddenScoreboard() {
        return hiddenScoreboard;
    }

    public Iterable<Scoreboard> getScoreboards() {
        return Iterables.concat(partyScoreboards.values(), Collections.singleton(hiddenScoreboard));
    }

    public void updatePartyScoreboardTeam(Party party) {
        String teamName = getScoreboardTeamName(party);
        updatePartyScoreboardTeam(party, hiddenScoreboard.getTeam(teamName), false);
        for(Map.Entry<Party, Scoreboard> entry : partyScoreboards.entrySet()) {
            updatePartyScoreboardTeam(party, entry.getValue().getTeam(teamName), !(entry.getKey() instanceof Competitor));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPartyAdd(PartyAddEvent event) {
        getScoreboard(event.getParty());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPartyRemove(PartyRemoveEvent event) {
        removePartyScoreboard(event.getParty());
    }

    @EventHandler
    public void onPartyRename(PartyRenameEvent event) {
        updatePartyScoreboardTeam(event.getParty());
    }

    @EventHandler
    public void onPlayerChangeParty(PlayerPartyChangeEvent event) {
        changePlayerScoreboard(event.getPlayer(), event.getOldParty(), event.getNewParty());
    }

    private static String toString(Scoreboard scoreboard) {
        return scoreboard == null ? "null" : "bukkit." + scoreboard.getClass().getSimpleName() + "{" + scoreboard.hashCode() + "}";
    }

    private static String toString(Team team) {
        return team == null ? "null" : "bukkit." + team.getClass().getSimpleName() + "{" + team.getName() + "}";
    }
}
