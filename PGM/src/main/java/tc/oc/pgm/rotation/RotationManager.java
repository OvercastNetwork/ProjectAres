package tc.oc.pgm.rotation;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.configuration.Configuration;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.commons.core.logging.ClassLogger;
import tc.oc.pgm.map.PGMMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class RotationManager {

    private final Logger logger;
    private final MinecraftService minecraftService;
    private final Configuration config;
    private final SortedSet<RotationProviderInfo> providers;
    private String currentRotationName;
    private RotationState defaultRotation;

    public RotationManager(Logger logger, MinecraftService minecraftService, Configuration config, PGMMap defaultMap, Collection<RotationProviderInfo> providers) {
        this.logger = ClassLogger.get(checkNotNull(logger, "logger"), getClass());
        this.minecraftService = minecraftService;
        this.config = config;
        this.providers = Collections.synchronizedSortedSet(Sets.newTreeSet(providers));

        load(defaultMap);
    }

    public @Nonnull RotationState getRotation() {
        RotationState rotation = this.getRotation(this.currentRotationName);
        if(rotation == null) {
            rotation = this.defaultRotation;
        }
        return rotation;
    }

    public @Nullable RotationState getRotation(@Nonnull String name) {
        for(RotationProviderInfo info : this.providers) {
            RotationState rotation = info.provider.getRotation(name);
            if(rotation != null) {
                return rotation;
            }
        }

        return null;
    }

    public @Nonnull Map<String, RotationState> getRotations() {
        Map<String, RotationState> rotations = Maps.newTreeMap();
        for(RotationProviderInfo info : this.providers) {
            for(Map.Entry<String, RotationState> rotation : info.provider.getRotations().entrySet()) {
                if(!rotations.containsKey(rotation.getKey())) {
                    rotations.put(rotation.getKey(), rotation.getValue());
                }
            }
        }
        return ImmutableMap.copyOf(rotations);
    }

    public void setRotation(@Nonnull RotationState rotation) {
        this.setRotation(this.currentRotationName, rotation);
    }

    public void setRotation(@Nonnull String name, @Nonnull RotationState rotation) {
        Preconditions.checkNotNull(name, "rotation name");
        Preconditions.checkNotNull(rotation, "rotation");

        for(RotationProviderInfo info : this.providers) {
            info.provider.saveRotation(name, rotation);
        }

        minecraftService.updateLocalServer((ServerDoc.Rotations) () ->
            getRotations().entrySet()
                          .stream()
                          .map(entry -> new ServerDoc.Rotation() {
                              public String name() { return entry.getKey(); }
                              public String next_map_id() { return entry.getValue().getNext().getId().slug(); }
                          })
                          .sorted(Comparator.comparing(ServerDoc.Rotation::name, (r1, r2) -> r1.equals(name) ? -1 : 1))
                          .collect(Collectors.toList()));
    }

    public @Nonnull String getCurrentRotationName() {
        return this.currentRotationName;
    }

    public void setCurrentRotationName(@Nonnull String name) {
        Preconditions.checkNotNull(name, "rotation name");

        this.currentRotationName = name;
    }

    public @Nonnull List<RotationProvider> getProviders(@Nonnull String rotationName) {
        ImmutableList.Builder<RotationProvider> providers = ImmutableList.builder();
        for(RotationProviderInfo info : this.providers) {
            if(info.provider.getRotation(rotationName) != null) {
                providers.add(info.provider);
            }
        }
        return providers.build();
    }

    public @Nullable RotationProvider getProviderByName(@Nonnull String name) {
        for(RotationProviderInfo info : this.providers) {
            if(info.name.equalsIgnoreCase(name)) {
                return info.provider;
            }
        }
        return null;
    }
    
    public @Nonnull List<RotationProviderInfo> getProviders() {
        return ImmutableList.copyOf(this.providers);
    }

    public void addProvider(@Nonnull RotationProvider provider, @Nonnull String name, int priority, int count) {
        Preconditions.checkNotNull(provider, "rotation provider");
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkArgument(this.getProviderByName(name) == null, "provider is already registered to name");

        RotationProviderInfo state = new RotationProviderInfo(provider, name, priority, count);
        this.providers.add(state);
    }

    /**
     * Reload all rotations and re-acquire all {@link PGMMap} objects from the map library.
     * After calling this method, the rotation system should not have any references to
     * {@link PGMMap}s that are not currently in the library.
     */
    public boolean load(PGMMap defaultMap) {
        this.defaultRotation = new RotationState(Collections.singletonList(defaultMap), 0);

        List<ServerDoc.Rotation> rotations = minecraftService.getLocalServer().rotations();
        this.currentRotationName = rotations.isEmpty() ? config.getString("rotation.default-name", "default") : rotations.get(0).name();


        logger.info("Loading rotations from " + providers.size() +
                    " providers. Fallback map is '" + defaultRotation.getMaps().get(0).getName() +
                    "'. Initial rotation name is '" + currentRotationName + "'");

        Instant timeoutTime = Instant.now().plusMillis(config.getInt("rotation.initial-wait", 10*1000));
        Map<RotationProviderInfo, Future<?>> loadingFutures = Maps.newHashMapWithExpectedSize(this.providers.size());

        for(RotationProviderInfo info : this.providers) {
            loadingFutures.put(info, info.provider.loadRotations());
        }

        boolean ranOutOfTime = false;
        for(Map.Entry<RotationProviderInfo, Future<?>> futureEntry : loadingFutures.entrySet()) {
            RotationProviderInfo info = futureEntry.getKey();
            String name = info.name;
            Future<?> future = futureEntry.getValue();
            long wait = Duration.between(Instant.now(), timeoutTime).toMillis();

            try {
                future.get(wait, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                ranOutOfTime = true;
                this.logger.severe(String.format("Rotation provider '%s' failed to load before timeout.", name));
            } catch (ExecutionException e) {
                this.logger.log(Level.SEVERE, String.format("Rotation provider '%s' threw an exception while loading.", name), e.getCause());
            } catch (InterruptedException e) {
                this.logger.warning(String.format("Rotation provider '%s' was interrupted while trying to load.", name));
            }
        }

        return !ranOutOfTime;
    }
}
