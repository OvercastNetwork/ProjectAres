package tc.oc.pgm.payload;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.Repeatable;
import tc.oc.time.Time;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;

import static tc.oc.commons.core.stream.Collectors.toImmutableList;

public class PayloadMatchModule extends MatchModule {

    private static final long MILLIS = 100;
    private static final Duration INTERVAL = Duration.ofMillis(MILLIS); // milliseconds, two ticks

    private final List<Payload> payloads;
    private final PayloadAnnouncer announcer;

    @Inject
    private PayloadMatchModule(Match match) {
        this.announcer = new PayloadAnnouncer(match);
        this.payloads = match.featureDefinitions()
                                  .all(PayloadDefinition.class)
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
        for(Payload payload : this.payloads) {
            payload.unregisterEvents();
        }
        HandlerList.unregisterAll(this.announcer);
        super.unload();
    }

    @Repeatable(interval = @Time(milliseconds = MILLIS))
    public void tick() {
        for(Payload payload : payloads) {
            payload.tick(INTERVAL);
        }
    }

}
