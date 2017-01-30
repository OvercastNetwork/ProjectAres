package tc.oc.pgm.mutation;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import tc.oc.commons.core.util.MapUtils;
import tc.oc.commons.core.random.RandomUtils;
import tc.oc.pgm.Config;
import tc.oc.pgm.match.*;
import tc.oc.pgm.mutation.command.MutationCommands;
import tc.oc.pgm.mutation.submodule.MutationModule;
import tc.oc.commons.core.random.ImmutableWeightedRandomChooser;

import java.util.*;
import java.util.logging.Level;
import javax.inject.Inject;

public class MutationMatchModule extends MatchModule {
    // TODO: send remote mutation alerts via an AMQP message

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
        this.mutations = getDefaultMutations();
        this.history = new HashSet<>();
        this.modules = new HashMap<>();
    }

    public final ImmutableMap<Mutation, Boolean> getMutations() {
        return ImmutableMap.copyOf(mutations);
    }

    public final ImmutableSet<Mutation> getActiveMutations() {
        return ImmutableSet.copyOf(Collections2.filter(getMutations().keySet(), new Predicate<Mutation>() {
            @Override
            public boolean apply(Mutation mutation) {
                return mutations.get(mutation);
            }
        }));
    }

    public final ImmutableSet<Mutation> getHistoricalMutations() {
        return ImmutableSet.copyOf(history);
    }

    public final ImmutableSet<MutationModule> getMutationModules() {
        return ImmutableSet.copyOf(modules.values());
    }

    @Override
    public boolean shouldLoad() {
        return Config.Mutations.enabled();
    }

    @Override
    public void load() {
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
        for(Mutation mutation : getActiveMutations()) {
            try {
                mutate(mutation);
            } catch (Throwable throwable) {
                logger.log(Level.SEVERE, "Unable to load mutation module (" + mutation.name() + ")", throwable);
            }
        }
    }

    @Override
    public void enable() {
        for(MutationModule module : modules.values()) {
            module.enable(match.isRunning());
        }
    }

    @Override
    public void disable() {
        for(MutationModule module : modules.values()) {
            module.disable(match.isRunning());
        }
    }

    public void mutate(Mutation mutation) throws Throwable {
        Class<? extends MutationModule> clazz = mutation.getModuleClass();
        if(clazz == null || (match.isRunning() && !mutation.isChangeable())) return;
        MutationModule module = modules.containsKey(clazz) ? modules.get(clazz) : mutation.getModuleClass().getDeclaredConstructor(Match.class).newInstance(match);
        if(mutations.get(mutation)) {
            module.enable(match.isRunning());
            modules.put(clazz, module);
            mutations.put(mutation, true);
            history.add(mutation);
        } else {
            module.disable(match.isRunning());
            modules.remove(clazz);
            mutations.put(mutation, false);
        }
    }

    private Map<Mutation, Boolean> getDefaultMutations() {
        Map<Mutation, Boolean> defaults = new HashMap<>();
        MapUtils.putAll(defaults, Sets.newHashSet(Mutation.values()), false);
        return defaults;
    }

    public static boolean check(Match match, Mutation mutation) {
        return Config.Mutations.enabled() &&
                match.hasMatchModule(MutationMatchModule.class) &&
                match.getMatchModule(MutationMatchModule.class).getActiveMutations().contains(mutation);
    }
}
