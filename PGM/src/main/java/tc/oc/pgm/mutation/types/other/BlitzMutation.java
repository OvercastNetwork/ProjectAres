package tc.oc.pgm.mutation.types.other;

import com.google.common.collect.Range;
import org.apache.commons.lang.math.Fraction;
import tc.oc.commons.core.random.RandomUtils;
import tc.oc.pgm.blitz.BlitzMatchModuleImpl;
import tc.oc.pgm.blitz.BlitzProperties;
import tc.oc.pgm.blitz.Lives;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.mutation.types.MutationModule;
import tc.oc.pgm.teams.TeamMatchModule;

public class BlitzMutation extends MutationModule.Impl {

    final static Range<Integer> LIVES = Range.closed(1, 3);
    final static Fraction TEAM_CHANCE = Fraction.ONE_QUARTER;

    public BlitzMutation(Match match) {
        super(match);
    }

    @Override
    public void enable() {
        super.enable();
        int lives = entropy().randomInt(LIVES);
        Lives.Type type;
        if(match().module(TeamMatchModule.class).isPresent() && RandomUtils.nextBoolean(random(), TEAM_CHANCE)) {
            type = Lives.Type.TEAM;
            lives *= match().module(TeamMatchModule.class).get().getFullestTeam().getSize();
        } else {
            type = Lives.Type.INDIVIDUAL;
        }
        match().module(BlitzMatchModuleImpl.class).get().activate(BlitzProperties.create(match(), lives, type));
    }

    @Override
    public void disable() {
        match().getScheduler(MatchScope.LOADED).createTask(() -> {
            if(!match().isFinished()) {
                match().module(BlitzMatchModuleImpl.class).get().deactivate();
            }
        });
        super.disable();
    }

}
