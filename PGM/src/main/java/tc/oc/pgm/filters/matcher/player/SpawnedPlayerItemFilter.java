package tc.oc.pgm.filters.matcher.player;

import java.util.function.Predicate;

import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.filters.ItemMatcher;
import tc.oc.pgm.filters.query.IPlayerQuery;
import tc.oc.pgm.match.MatchPlayer;

public abstract class SpawnedPlayerItemFilter extends SpawnedPlayerFilter {

    @Inspect(inline = true)
    private final Predicate<? super ItemStack> matcher;

    public SpawnedPlayerItemFilter(ItemStack base) {
        this(new ItemMatcher(base));
    }

    public SpawnedPlayerItemFilter(Predicate<? super ItemStack> matcher) {
        this.matcher = matcher;
    }

    protected abstract Iterable<ItemStack> getItems(MatchPlayer player);

    @Override
    protected boolean matches(IPlayerQuery query, MatchPlayer player) {
        for(ItemStack item : getItems(player)) {
            if(matcher.test(item)) return true;
        }
        return false;
    }
}
