package tc.oc.pgm.controlpoint;

import java.time.Duration;
import java.util.List;
import javax.inject.Inject;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.Repeatable;
import tc.oc.time.Time;

import static tc.oc.commons.core.stream.Collectors.toImmutableList;

public class ControlPointMatchModule extends MatchModule {

    private static final long MILLIS = 100;
    private static final Duration INTERVAL = Duration.ofMillis(MILLIS); // milliseconds, two ticks

    private final List<ControlPoint> controlPoints;
    private final ControlPointAnnouncer announcer;

    @Inject private ControlPointMatchModule(Match match) {
        this.announcer = new ControlPointAnnouncer(match);
        this.controlPoints = match.featureDefinitions()
                                  .all(ControlPointDefinition.class)
                                  .map(cp -> cp.getGoal(match))
                                  .collect(toImmutableList());
    }

    @Override
    public void load() {
        super.load();
        getMatch().registerEvents(this.announcer);
    }

    @Override
    public void unload() {
        for(ControlPoint controlPoint : this.controlPoints) {
            controlPoint.unregisterEvents();
        }
        HandlerList.unregisterAll(this.announcer);
        super.unload();
    }

    @Repeatable(interval = @Time(milliseconds = MILLIS))
    public void tick() {
        for(ControlPoint controlPoint : controlPoints) {
            controlPoint.tick(INTERVAL);
        }
    }

}
