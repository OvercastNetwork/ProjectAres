package tc.oc.pgm.mutation;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import tc.oc.commons.core.util.MapUtils;
import tc.oc.commons.core.random.RandomUtils;
import tc.oc.pgm.Config;
import tc.oc.pgm.match.*;
import tc.oc.pgm.mutation.command.MutationCommands;
import tc.oc.pgm.mutation.types.MutationModule;
import tc.oc.commons.core.random.ImmutableWeightedRandomChooser;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.inject.Inject;

public class MutationMatchModule extends MatchModule {

    /**
     * Chance that a mutation event will occur.
     */
    private final double chance;

    /**
     * Maximum amount of plugin generated mutations.
     * Users can override this limit by using {@link MutationCommands}.
     */
    private final int amount;

    /**
     * Weighed item selector to select {@link Mutation}s.
     */
    private final ImmutableWeightedRandomChooser<Mutation, Double> weightedSelector;

    /**
     * All the enabled mutations and their values for the current match.
     * By default, all mutations are set to false, but
     * various methods during {@link #load()} change them.
     */
    private final Map<Mutation, Boolean> mutations;

    /**
     * A collection of historical mutations that have been enabled
     * at least once during this match.
     */
    private final Set<Mutation> history;

    /**
     * The mutations modules that handle the logic for
     * each {@link Mutation}. These are like a {@link MatchModule}
     * due to their same enable and disable loading structure.
     */
    private final Map<Class<? extends MutationModule>, MutationModule> modules;

    @Inject private MutationQueue mutationQueue;

    public MutationMatchModule(Match match, MutationOptions options) {
        super(match);
        this.chance = options.chance;
        this.amount = options.amount;
        this.weightedSelector = new ImmutableWeightedRandomChooser<>(options.weights);
        this.mutations = mutationsDefault();
        this.history = new HashSet<>();
        this.modules = new HashMap<>();
    }

    public final ImmutableMap<Mutation, Boolean> mutations() {
        return ImmutableMap.copyOf(mutations);
    }

    public final ImmutableSet<Mutation> mutationsActive() {
        return ImmutableSet.copyOf(Collections2.filter(mutations().keySet(), mutations::get));
    }

    public final ImmutableSet<Mutation> mutationsHistorical() {
        return ImmutableSet.copyOf(history);
    }

    private Map<Mutation, Boolean> mutationsDefault() {
        Map<Mutation, Boolean> defaults = new HashMap<>();
        MapUtils.putAll(defaults, Sets.newHashSet(Mutation.values()), false);
        return defaults;
    }

    public final ImmutableSet<MutationModule> mutationModules() {
        return ImmutableSet.copyOf(modules.values());
    }

    @Override
    public void load() {
        if(!Config.Mutations.enabled()) return;
        Random random = match.getRandom();
        // Check if the api has any queued mutations
        Collection<Mutation> queuedMutations = mutationQueue.mutations();
        MapUtils.putAll(mutations, queuedMutations, true);
        if(queuedMutations.isEmpty()) {
            // Randomly add mutations to the match if there wasn't anything queued
            if(chance > random.nextDouble()) {
                int max = RandomUtils.safeNextInt(random, amount + 1);
                for(int i = 0; i < max; i++) {
                    mutations.put(weightedSelector.choose(random), true);
                }
            }
        } else {
            // Clear the mutation queue from the api
            mutationQueue.clear();
        }
        // Load the mutation modules for this match
        for(Mutation mutation : mutationsActive()) {
            try {
                mutate(mutation);
            } catch (Throwable throwable) {
                logger.log(Level.SEVERE, "Unable to load mutation module (" + mutation.name() + ")", throwable);
            }
        }
    }

    @Override
    public void enable() {
        modules.values().forEach(MutationModule::enable);
    }

    @Override
    public void disable() {
        modules.values().forEach(MutationModule::disable);
    }

    public void register(Mutation mutation, boolean load) {
        mutations.put(mutation, load);
    }

    public void mutate(Mutation mutation) throws Throwable {
        Class<? extends MutationModule> loader = mutation.loader();
        if(loader == null) return;
        MutationModule module = modules.containsKey(loader) ? modules.get(loader) : loader.getDeclaredConstructor(Match.class).newInstance(match);
        if(mutations.get(mutation)) {
            module.enable();
            modules.put(loader, module);
            mutations.put(mutation, true);
            history.add(mutation);
        } else {
            module.disable();
            modules.remove(loader);
            mutations.put(mutation, false);
        }
    }

    public boolean enabled() {
        return !mutationsActive().isEmpty();
    }

    public boolean enabled(Mutation... mutations) {
        return mutationsActive().stream().anyMatch(m1 -> Stream.of(mutations).anyMatch(m2 -> m2.equals(m1)));
    }

}
