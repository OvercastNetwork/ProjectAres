package tc.oc.pgm.map;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.inject.Injector;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.pgm.map.inject.MapInjectionScope;
import tc.oc.pgm.module.ModuleLoadException;
import tc.oc.pgm.xml.UnsupportedMapProtocolException;

import static tc.oc.commons.core.inject.Injection.unwrappingExceptions;

/**
 * Base class for an XML configured map.
 *
 * This class is responsible for parsing an XML file into a
 * {@link MapModuleContext}, detecting changes that require a reload,
 * and creation of a child {@link Injector} with bindings specific to
 * the map.
 *
 * This class is relatively decoupled from the rest of PGM, so that it can
 * potentially be used to integrate PGM features into other applications,
 * such as the lobby, or a non-match based game, e.g. an MMO.
 *
 * Anything related specifically to matches should be in {@link PGMMap}.
 */
public class MapDefinition {

    @Inject private MapConfiguration configuration;
    @Inject private Provider<MapModuleContext> contextProvider;
    @Inject private Provider<MapPersistentContext> persistentContextProvider;
    @Inject private ExceptionHandler exceptionHandler;
    @Inject private MapInjectionScope mapInjectionScope;

    private MapLogger logger;
    @Inject void init(MapLogger.Factory loggerFactory) {
        this.logger = loggerFactory.create(this);
    }

    private final MapFolder folder;

    protected @Nullable SoftReference<MapModuleContext> context;
    private @Nullable MapPersistentContext persistentContext;

    protected MapDefinition(MapFolder folder) {
        this.folder = folder;
    }


    public MapLogger getLogger() {
        return logger;
    }

    public MapFolder getFolder() {
        return folder;
    }

    public Optional<String> getThumbnailUri() {
        return getFolder().getThumbnailUri();
    }

    public String getDottedPath() {
        return Joiner.on(".").join(getFolder().getRelativePath());
    }

    public String getName() {
        return getFolder().getRelativePath().toString();
    }

    public boolean isLoaded() {
        return context != null;
    }

    public Optional<MapModuleContext> getContext() {
        if(context == null) {
            throw new IllegalStateException("Map is not loaded: " + this);
        }
        return Optional.ofNullable(context.get());
    }

    public MapPersistentContext getPersistentContext() {
        if(persistentContext == null) {
            throw new IllegalStateException("Map is not loaded: " + this);
        }
        return persistentContext;
    }

    public boolean shouldReload() {
        if(context == null) return true;
        if(!configuration.autoReload()) return false;
        MapModuleContext mapContext = context.get();
        if(mapContext == null) return true;
        if(mapContext.loadedFiles().isEmpty()) return configuration.reloadWhenError();

        try {
            for(Map.Entry<Path, HashCode> loaded : mapContext.loadedFiles().entrySet()) {
                HashCode latest = Files.hash(loaded.getKey().toFile(), Hashing.sha256());
                if(!latest.equals(loaded.getValue())) return true;
            }

            return false;
        } catch (IOException e) {
            return true;
        }
    }

    public boolean reload() throws MapNotFoundException {
        List<? extends ModuleLoadException> errors;

        try {
            final MapModuleContext newContext = mapInjectionScope.withNewStore(this, () -> {
                final MapModuleContext context = unwrappingExceptions(ModuleLoadException.class, contextProvider);
                context.load();
                return context;
            });

            if(!newContext.hasErrors()) {
                this.context = new SoftReference<>(newContext);
                this.persistentContext = newContext.asCurrentScope(persistentContextProvider::get);
                return true;
            }

            errors = newContext.getErrors();
        } catch(MapNotFoundException e) {
            throw e;
        } catch(UnsupportedMapProtocolException e) {
            logger.warning("Skipping map with unsupported proto " + e.getProto());
            errors = ImmutableList.of();
        } catch(ModuleLoadException e) {
            errors = ImmutableList.of(e);
        } catch(Throwable e) {
            exceptionHandler.handleException(e);
            errors = ImmutableList.of(new ModuleLoadException("Internal error", e));
        }

        for(ModuleLoadException error : errors) {
            logger.log(new MapLogRecord(this, error));
        }

        return false;
    }
}
