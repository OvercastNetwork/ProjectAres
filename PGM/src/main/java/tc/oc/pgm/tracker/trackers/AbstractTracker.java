package tc.oc.pgm.tracker.trackers;

import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.reflect.TypeToken;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.tracker.damage.TrackerInfo;

/**
 * Base class with a few convenience methods that are useful to trackers.
 *
 * Subclasses specify the type of {@link TrackerInfo} they use and the
 * resolve methods provided by the base class will filter out results of
 * the wrong type. If subclasses don't want results to be filtered, they
 * should call the resolve methods directly on the block/entity tracker.
 */
@ListenerScope(MatchScope.RUNNING)
public abstract class AbstractTracker<Info extends TrackerInfo> implements Listener {

    @Inject protected Match match;
    @Inject protected EntityTracker entities;
    @Inject protected BlockTracker blocks;

    protected Logger logger;
    @Inject void initLogger(Loggers loggers) {
        this.logger = loggers.get(getClass());
    }

    private final Class<Info> infoClass;

    protected AbstractTracker() {
        this.infoClass = (Class<Info>) new TypeToken<Info>(getClass()){}.getRawType();
    }

    protected EntityTracker entities() {
        return entities;
    }

    protected BlockTracker blocks() {
        return blocks;
    }

    protected @Nullable Info resolveBlock(Block block) {
        return blocks.resolveInfo(block, infoClass);
    }

    protected @Nullable Info resolveEntity(Entity entity) {
        return entities.resolveInfo(entity, infoClass);
    }
}
