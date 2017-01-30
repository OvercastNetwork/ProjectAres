package tc.oc.pgm.structure;

import java.util.Comparator;
import java.util.PriorityQueue;
import javax.inject.Inject;

import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchScope;

/**
 * We need this to make sure that dynamics are placed and cleared in definition order,
 * and that clears happen before placements.
 */
public class DynamicScheduler {

    private final Match match;
    private final PriorityQueue<Dynamic> clearQueue;
    private final PriorityQueue<Dynamic> placeQueue;

    @Inject DynamicScheduler(Match match, FeatureDefinitionContext fdc) {
        this.match = match;

        // Process dynamics in lexical order
        final Comparator<Dynamic> order = Comparator.comparing(Dynamic::getDefinition, fdc);
        this.clearQueue = new PriorityQueue<>(order);
        this.placeQueue = new PriorityQueue<>(order);
    }

    void queuePlace(Dynamic dynamic) {
        clearQueue.remove(dynamic);
        placeQueue.add(dynamic);
        schedule();
    }

    void queueClear(Dynamic dynamic) {
        placeQueue.remove(dynamic);
        clearQueue.add(dynamic);
        schedule();
    }

    private void schedule() {
        match.getScheduler(MatchScope.LOADED)
             .debounceTask(this::process);
    }

    public void process(){
        for(;;) {
            final Dynamic dynamic = clearQueue.poll();
            if(dynamic == null) break;
            dynamic.clear();
        }

        for(;;) {
            Dynamic dynamic = placeQueue.poll();
            if(dynamic == null) break;
            dynamic.place();
        }
    }
}
