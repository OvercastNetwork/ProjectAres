package tc.oc.pgm.modules;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.utils.MaterialPattern;

@ListenerScope(MatchScope.RUNNING)
public class ItemDestroyMatchModule extends MatchModule implements Listener {
    protected final ImmutableSet<MaterialPattern> patterns;

    public ItemDestroyMatchModule(Match match, Set<MaterialPattern> patterns) {
        super(match);
        this.patterns = ImmutableSet.copyOf(patterns);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void processItemRemoval(ItemSpawnEvent event) {
        final ItemStack item = event.getEntity().getItemStack();
        if(patterns.stream().anyMatch(pattern -> pattern.matches(item))) {
            event.setCancelled(true);
        }
    }
}
