package tc.oc.pgm.mutation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.minecraft.MinecraftService;

/**
 * The set of mutations queued to load for the next match, as stored in the local server document.
 */
public class MutationQueue {

    private final MinecraftService minecraftService;

    @Inject MutationQueue(MinecraftService minecraftService) {
        this.minecraftService = minecraftService;
    }

    public Collection<Mutation> mutations() {
        return minecraftService
            .getLocalServer()
            .queued_mutations()
            .stream()
            .flatMap(Mutation::fromString)
            .collect(Collectors.toList());
    }

    public ListenableFuture<Server> clear() {
        return force(Collections.emptyList());
    }

    public ListenableFuture<Server> removeAll(final Collection<Mutation> mutations) {
        Collection<Mutation> removed = mutations();
        removed.removeAll(mutations);
        return force(removed);
    }

    public ListenableFuture<Server> mergeAll(final Collection<Mutation> mutations) {
        Collection<Mutation> merged = new HashSet<>();
        for(Mutation mutation : Mutation.values()) {
            if(mutations.contains(mutation) || mutations().contains(mutation)) {
                merged.add(mutation);
            }
        }
        return force(merged);
    }

    private ListenableFuture<Server> force(final Collection<Mutation> mutations) {
        return minecraftService.updateLocalServer((ServerDoc.Mutation) () -> mutations.stream().map(Mutation::name).collect(Collectors.toSet()));
    }

}
