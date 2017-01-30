package tc.oc.pgm.mutation;

import java.util.Map;

/**
 * Configuration options for {@link MutationMapModule}.
 */
public class MutationOptions {

    protected final Map<Mutation, Double> weights;
    protected final double chance;
    protected final int amount;

    public MutationOptions(Map<Mutation, Double> weights, double chance, int amount) {
        this.weights = weights;
        this.chance = chance;
        // Reduce to the theoretical maximum amount
        this.amount = amount == Integer.MAX_VALUE ? Mutation.values().length : amount;
    }

}
