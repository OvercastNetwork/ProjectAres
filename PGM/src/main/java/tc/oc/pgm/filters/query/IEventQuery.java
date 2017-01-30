package tc.oc.pgm.filters.query;

import org.bukkit.event.Event;

public interface IEventQuery extends ITransientQuery {

    Event getEvent();

    @Override
    default int randomSeed() {
        return getEvent().hashCode();
    }
}
