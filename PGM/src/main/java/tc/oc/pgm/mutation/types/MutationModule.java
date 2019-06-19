package tc.oc.pgm.mutation.types;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.item.ItemBuilder;
import tc.oc.commons.core.random.AdvancingEntropy;
import tc.oc.commons.core.random.Entropy;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.mutation.Mutation;

import java.util.Random;

/**
 * Bits of immutable code that manage a {@link Mutation}.
 *
 * This should be able to load at any time during the match
 * and not cause any problems. This will allow mutations
 * to be forcefully loaded on the fly without any worries
 * of breaking the match state.
 */
@ListenerScope(MatchScope.RUNNING)
public interface MutationModule extends Listener {

    Match match();

    default void enable() {
        match().registerEventsAndRepeatables(this);
    }

    default void disable() {
        match().unregisterEvents(this);
        match().unregisterRepeatable(this);
    }

    default World world() {
        return match().getWorld();
    }

    default Entropy entropy() {
        return match().entropyForTick();
    }

    default Random random() {
        return match().getRandom();
    }

    abstract class Impl implements MutationModule {

        private final Match match;
        private final Entropy entropy;

        public Impl(final Match match) {
            this.match = match;
            this.entropy = new AdvancingEntropy(match.entropyForTick().randomLong());
        }

        @Override
        public Match match() {
            return match;
        }

        @Override
        public Entropy entropy() {
            return entropy;
        }

        protected static ItemStack item(Material material, int amount) {
            return new ItemBuilder().material(material).amount(amount).unbreakable(true).shareable(false).get();
        }

        protected static ItemStack item(Material material) {
            return item(material, 1);
        }

    }

}
