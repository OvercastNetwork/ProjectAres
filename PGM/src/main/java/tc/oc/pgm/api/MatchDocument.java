package tc.oc.pgm.api;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import tc.oc.api.docs.AbstractModel;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.virtual.CompetitorDoc;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Streams;
import tc.oc.pgm.goals.GoalMatchModule;
import tc.oc.pgm.join.JoinMatchModule;
import tc.oc.pgm.blitz.BlitzMatchModule;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchState;
import tc.oc.pgm.mutation.MutationMatchModule;
import tc.oc.pgm.victory.VictoryMatchModule;

public class MatchDocument extends AbstractModel implements MatchDoc {

    private final ServerDoc.Identity localServer;
    private final MapDoc map;
    private final Match match;
    private final VictoryMatchModule victory;
    private final Optional<MutationMatchModule> mutations;
    private final Optional<GoalMatchModule> goals;
    private final BlitzMatchModule blitz;
    private final Optional<JoinMatchModule> join;

    @Inject MatchDocument(ServerDoc.Identity localServer, MapDoc map, Match match, VictoryMatchModule victory, Optional<MutationMatchModule> mutations, Optional<GoalMatchModule> goals, BlitzMatchModule blitz, Optional<JoinMatchModule> join) {
        this.match = match;
        this.localServer = localServer;
        this.map = map;
        this.victory = victory;
        this.mutations = mutations;
        this.goals = goals;
        this.blitz = blitz;
        this.join = join;
    }

    @Override
    public String _id() {
        return match.getId();
    }

    @Override
    public String server_id() {
        return localServer._id();
    }

    @Override
    public String family_id() {
        return localServer.family();
    }

    @Override
    public Instant load() {
        return match.getLoadTime();
    }

    @Override
    public @Nullable Instant start() {
        return match.getStateChangeTime(MatchState.Running);
    }

    @Override
    public @Nullable Instant end() {
        return match.getStateChangeTime(MatchState.Finished);
    }

    @Override
    public @Nullable Instant unload() {
        return match.getUnloadTime();
    }

    @Override
    public boolean join_mid_match() {
        return !blitz.activated() && join.isPresent() && join.get().canJoinMid();
    }

    @Override public MapDoc map() {
        return map;
    }

    @Override
    public int player_count() {
        return match.getParticipationClock().getAllWithNonZeroPresence().size();
    }

    private Collection<String> winningIds(Class<? extends CompetitorDoc> type) {
        return match.hasStarted() ? victory.leaders()
                                           .stream()
                                           .map(Competitor::getDocument)
                                           .filter(type::isInstance)
                                           .map(Model::_id)
                                           .collect(Collectors.toImmutableList())
                                  : ImmutableList.of();
    }

    @Override
    public Collection<String> winning_team_ids() {
        return winningIds(Team.class);
    }

    @Override
    public Collection<String> winning_user_ids() {
        return winningIds(PlayerId.class);
    }

    @Override
    public Set<String> mutations() {
        return mutations.map(mmm -> mmm.mutationsHistorical()
                                       .stream()
                                       .map(tc.oc.pgm.mutation.Mutation::name)
                                       .collect(Collectors.toImmutableSet()))
                        .orElse(ImmutableSet.of());
    }

    @Override
    public Collection<? extends Team> competitors() {
        return Streams.instancesOf(match.competitors()
                                        .map(Competitor::getDocument),
                                   Team.class)
                      .collect(Collectors.toImmutableList());
    }

    @Override
    public Collection<? extends Goal> objectives() {
        return goals.map(gmm -> Collections2.transform(gmm.getGoals(),
                                                       tc.oc.pgm.goals.Goal::getDocument))
                    .orElse(ImmutableSet.of());
    }
}
