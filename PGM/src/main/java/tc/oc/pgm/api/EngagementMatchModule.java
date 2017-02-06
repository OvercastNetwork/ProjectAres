package tc.oc.pgm.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.ListenableFuture;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.virtual.EngagementDoc;
import tc.oc.api.docs.virtual.EngagementDocBase;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.engagement.EngagementService;
import tc.oc.api.message.types.Reply;
import tc.oc.api.model.IdFactory;
import tc.oc.commons.bukkit.chat.Links;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.formatting.PeriodFormats;
import tc.oc.commons.core.util.Comparables;
import tc.oc.commons.core.util.Numbers;
import tc.oc.commons.core.util.PunchClock;
import tc.oc.commons.core.util.RankedSet;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.pgm.Config;
import tc.oc.pgm.broadcast.Broadcast;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.MatchPostCommitEvent;
import tc.oc.pgm.events.PlayerJoinPartyEvent;
import tc.oc.pgm.join.JoinConfiguration;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.victory.VictoryMatchModule;

/**
 * Responsible for creating/updating {@link EngagementDoc}s.
 */
@ListenerScope(MatchScope.LOADED)
public class EngagementMatchModule extends MatchModule implements Listener {

    abstract class Engagement extends EngagementDocBase {
        final PlayerId playerId;
        final Competitor competitor;
        final Instant commitTime;

        Engagement(PlayerId playerId, Competitor competitor) {
            super(idFactory.newId(), EngagementMatchModule.this.matchDocument);
            this.playerId = playerId;
            this.competitor = competitor;
            this.commitTime = getMatch().getClock().now().instant;
        }

        public PlayerId getPlayerId() {
            return playerId;
        }

        @Override
        public String user_id() {
            return playerId._id();
        }

        @Override
        public Instant match_joined_at() {
            return commitTime;
        }

        @Override
        public @Nullable Duration match_participation() {
            if(!getMatch().isFinished()) return null;
            return getMatch().getParticipationClock().getCumulativePresence(playerId);
        }

        @Override
        public boolean committed() {
            return joinConfiguration.commitPlayers();
        }

        @Override
        public @Nullable Integer rank() {
            if(!getMatch().isFinished()) return null;
            int rank = match.needMatchModule(VictoryMatchModule.class).rankedCompetitors().getPosition(competitor);
            return rank == -1 ? null : rank;
        }

        @Override
        public @Nullable Integer tied_count() {
            if(!getMatch().isFinished()) return null;
            final RankedSet<Competitor> ranks = match.needMatchModule(VictoryMatchModule.class).rankedCompetitors();
            int rank = ranks.getPosition(competitor);
            return rank == -1 ? null : ranks.getRank(rank).size();
        }

        boolean isWin() {
            return getMatch().isFinished() && rank() == 0 && tied_count() < competitor_count();
        }

        boolean isLoss() {
            return getMatch().isFinished() && rank() > 0;
        }

        boolean isTie() {
            return getMatch().isFinished() && rank() == 0 && tied_count().equals(competitor_count());
        }

        boolean isForfeit() {
            return forfeit_reason() != null;
        }
    }

    class TeamEngagement extends Engagement {
        final Team team;

        TeamEngagement(PlayerId playerId, Team team) {
            super(playerId, team);
            this.team = team;
        }

        @Override
        public String team_pgm_id() {
            return team.slug();
        }

        @Override
        public Integer team_size() {
            return team.getPastPlayers().size();
        }

        @Override
        public Duration team_participation() {
            return team.getParticipationClock().getCumulativePresence(playerId);
        }

        @Override
        public @Nullable ForfeitReason forfeit_reason() {
            return getForfeitReason(playerId, team.getParticipationClock());
        }
    }

    class FreeForAllEngagement extends Engagement {
        FreeForAllEngagement(PlayerId playerId) {
            super(playerId, getMatch().getLastCompetitor(playerId));
        }

        @Override
        public @Nullable ForfeitReason forfeit_reason() {
            return getForfeitReason(playerId, getMatch().getParticipationClock());
        }

        @Override public @Nullable String team_pgm_id() { return null; }
        @Override public @Nullable Integer team_size() { return null; }
        @Override public @Nullable Duration team_participation() { return null; }
    }

    private final EngagementService engagementService;
    private final BukkitUserStore userStore;
    private final IdFactory idFactory;
    private final SyncExecutor syncExecutor;
    private final JoinConfiguration joinConfiguration;
    private final MatchDoc matchDocument;

    private final Table<PlayerId, Competitor, Engagement> engagements = HashBasedTable.create();

    @Inject EngagementMatchModule(Match match, EngagementService engagementService, BukkitUserStore userStore, IdFactory idFactory, SyncExecutor syncExecutor, JoinConfiguration joinConfiguration, MatchDoc matchDocument) {
        super(match);
        this.engagementService = engagementService;
        this.userStore = userStore;
        this.idFactory = idFactory;
        this.syncExecutor = syncExecutor;
        this.joinConfiguration = joinConfiguration;
        this.matchDocument = matchDocument;
    }

    @Override
    public boolean shouldLoad() {
        return super.shouldLoad() && Config.Stats.Engagements.enabled();
    }

    static @Nullable Engagement.ForfeitReason getForfeitReason(PlayerId playerId, PunchClock<PlayerId> clock) {
        if(Duration.ZERO.equals(Config.Stats.Engagements.maxContinuousAbsence()) ||
           Duration.ZERO.equals(Config.Stats.Engagements.maxCumulativeAbsence()) ||
           Config.Stats.Engagements.minParticipationPercent() == 1) {

            // If any limit is at the extreme then no absence is allowed, which requires a special message
            if(Comparables.greaterThan(clock.getCumulativeAbsence(playerId), Duration.ZERO)) {
                return Engagement.ForfeitReason.ABSENCE;
            }

        } else {
            // Since we only show one of these at most, they are ordered roughly by importance/relevance.
            // The percentage that you missed is more important than the absolute amount, and if your cumulative
            // absence was too high, then max continuous absence doesn't really matter.
            if(clock.getCumulativePresencePercent(playerId) < Config.Stats.Engagements.minParticipationPercent()) {
                return Engagement.ForfeitReason.PARTICIPATION_PERCENT;
            }

            if(Comparables.greaterThan(clock.getCumulativeAbsence(playerId), Config.Stats.Engagements.maxCumulativeAbsence())) {
                return Engagement.ForfeitReason.CUMULATIVE_ABSENCE;
            }

            if(Comparables.greaterThan(clock.getMaxContinuousAbsence(playerId), Config.Stats.Engagements.maxContinuousAbsence())) {
                return Engagement.ForfeitReason.CONTINUOUS_ABSENCE;
            }
        }

        return null;
    }

    /**
     * Get (or create) the engagement for the given player and competitor
     */
    Engagement getEngagement(PlayerId playerId, Competitor competitor) {
        Engagement engagement = engagements.get(playerId, competitor);
        if(engagement == null) {
            if(competitor instanceof Team) {
                engagement = new TeamEngagement(playerId, (Team) competitor);
            } else {
                engagement = new FreeForAllEngagement(playerId);
            }
            engagements.put(playerId, competitor, engagement);
        }
        return engagement;
    }

    void createAllEngagements() {
        for(PlayerId playerId : getMatch().getPastParticipants()) {
            for(Competitor competitor : getMatch().getPastCompetitors(playerId)) {
                getEngagement(playerId, competitor);
            }
        }
    }

    /**
     * Get an engagement for the given player, using their most recently joined competitor
     */
    @Nullable Engagement getLastEngagement(MatchPlayer player) {
        Competitor competitor = getMatch().getLastCompetitor(player.getPlayerId());
        if(competitor == null) return null;

        return getEngagement(player.getPlayerId(), competitor);
    }

    ListenableFuture<Reply> saveEngagements(final Collection<Engagement> engagements, final boolean feedback) {
        logger.fine("Saving " + engagements.size() + " engagements");

        if(feedback && getMatch().isFinished()) {
            for(Engagement engagement : engagements) {
                MatchPlayer player = getMatch().getPlayer(engagement.getPlayerId());
                if(player != null) sendPostMatchFeedback(player);
            }
        }

        return engagementService.updateMulti(engagements);
    }

    static BaseComponent formatForfeitReason(Engagement.ForfeitReason forfeitReason) {
        switch(forfeitReason) {
            case ABSENCE:
                return new TranslatableComponent("engagement.forfeitReason.missedPart");

            case PARTICIPATION_PERCENT:
                return new TranslatableComponent("engagement.forfeitReason.participationPercent",
                                                 String.valueOf(Numbers.percentage(Config.Stats.Engagements.minParticipationPercent())));

            case CUMULATIVE_ABSENCE:
                return new TranslatableComponent("engagement.forfeitReason.cumulativeAbsence",
                                                 PeriodFormats.briefNaturalPrecise(Config.Stats.Engagements.maxCumulativeAbsence()));

            case CONTINUOUS_ABSENCE:
                return new TranslatableComponent("engagement.forfeitReason.continuousAbsence",
                                                 PeriodFormats.briefNaturalPrecise(Config.Stats.Engagements.maxContinuousAbsence()));
        }
        throw new IllegalStateException();
    }

    static BaseComponent formatResult(Engagement engagement) {
        if(engagement.isForfeit()) {
            return new Component(new TranslatableComponent("engagement.result.forfeit"), ChatColor.DARK_RED);
        } else if(engagement.isWin()) {
            return new Component(new TranslatableComponent("engagement.result.win"), ChatColor.GREEN);
        } else if(engagement.isLoss()) {
            return new Component(new TranslatableComponent("engagement.result.loss"), ChatColor.RED);
        } else {
            return new Component(new TranslatableComponent("engagement.result.tie"), ChatColor.BLUE);
        }
    }

    public void sendPreMatchFeedback(MatchPlayer player) {
        player.sendMessage(new TranslatableComponent("command.match.matchInfo.ranking.message"));
    }

    void sendPostMatchFeedback(MatchPlayer player) {
        Engagement engagement = getLastEngagement(player);
        if(engagement == null) return;

        player.sendMessage(new TranslatableComponent("engagement.matchRecorded", formatResult(engagement)));
        if(engagement.isForfeit()) {
            player.sendMessage(new Component(formatForfeitReason(engagement.forfeit_reason()), ChatColor.YELLOW));
        }

        player.sendMessage(Broadcast.Type.TIP.format(new TranslatableComponent("engagement.statsLink", Links.profileLink(player.getPlayerId()))));
    }

    @EventHandler
    public void onMatchCommit(MatchPostCommitEvent event) {
        // Save all engagements once players have committed
        createAllEngagements();

        final Collection<Player> players = ImmutableSet.copyOf(Collections2.transform(
            getMatch().getParticipatingPlayers(),
            new Function<MatchPlayer, Player>() {
                @Override public Player apply(MatchPlayer player) {
                    return player.getBukkit();
                }
            }
        ));

        syncExecutor.callback(saveEngagements(engagements.values(), false), (result) -> {
            // Refresh all user docs after creating the engagements, to update their match quotas
            userStore.refresh(players);
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinPartyEvent event) {
        // Save engagement when a competitor is joined mid-match
        if(getMatch().isCommitted() && event.getNewParty() instanceof Competitor) {
            Engagement engagement = getEngagement(event.getPlayer().getPlayerId(), (Competitor) event.getNewParty());
            saveEngagements(Collections.singleton(engagement), false);
        }
    }

    @EventHandler
    public void onEnd(MatchEndEvent event) {
        // Save all engagements on match end
        saveEngagements(engagements.values(), true);
    }
}
