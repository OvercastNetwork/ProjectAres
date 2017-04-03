package tc.oc.pgm.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.query.EntitySpawnQuery;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;

@ListenerScope(MatchScope.LOADED)
public class MobsMatchModule extends MatchModule implements Listener {

    private final Filter mobsFilter;

    public MobsMatchModule(Match match, Filter mobsFilter) {
        super(match);
        this.mobsFilter = mobsFilter;
    }

    @Override
    public void load() {
        super.load();
        getMatch().getWorld().setSpawnFlags(false, false);
    }

    @Override
    public void enable() {
        getMatch().getWorld().setSpawnFlags(true, true);
    }

    @Override
    public void disable() {
        getMatch().getWorld().setSpawnFlags(false, false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void checkSpawn(final CreatureSpawnEvent event) {
        if(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(mobsFilter.query(new EntitySpawnQuery(event, event.getEntity(), event.getSpawnReason())).isDenied());
        }
    }

}
