package tc.oc.pgm.control;

import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.Repeatable;
import tc.oc.time.Time;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;

import static tc.oc.commons.core.stream.Collectors.toImmutableList;

public class ControllableGoalMatchModule extends MatchModule {

    private static final long MILLIS = 100;
    private static final Duration INTERVAL = Duration.ofMillis(100);

    private final List<ControllableGoal> controllables;
    private final ControllableGoalAnnouncer announcer;

    @Inject private ControllableGoalMatchModule(Match match) {
        this.announcer = new ControllableGoalAnnouncer(match);
        this.controllables = match.featureDefinitions()
                                  .all(ControllableGoalDefinition.class)
                                  .map(cp -> (ControllableGoal) cp.getGoal(match))
                                  .collect(toImmutableList());
    }

    @Override
    public void load() {
        match.registerEvents(announcer);
        for(ControllableGoal controllable : controllables) {
            match.registerEventsAndRepeatables(controllable);
            controllable.display();
        }
    }

    @Override
    public void unload() {
        for(ControllableGoal controllable : controllables) {
            match.unregisterEventsAndRepeatables(controllable);
        }
        match.unregisterEvents(announcer);
    }

    @Repeatable(interval = @Time(milliseconds = MILLIS))
    public void tick() {
        for(ControllableGoal controllable : controllables) {
            controllable.tick(INTERVAL);
        }
    }

}
