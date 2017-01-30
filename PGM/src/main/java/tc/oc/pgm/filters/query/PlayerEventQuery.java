package tc.oc.pgm.filters.query;

import org.bukkit.event.Event;

import static com.google.common.base.Preconditions.checkNotNull;

public class PlayerEventQuery extends TransientPlayerQuery implements IPlayerEventQuery {

    private final Event event;

    public PlayerEventQuery(IPlayerQuery player, Event event) {
        super(player);
        this.event = checkNotNull(event);
    }

    @Override
    public Event getEvent() {
        return event;
    }

    @Override
    public int randomSeed() {
        return IPlayerEventQuery.super.randomSeed();
    }
}
