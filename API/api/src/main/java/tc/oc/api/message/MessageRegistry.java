package tc.oc.api.message;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import org.apache.commons.collections.map.HashedMap;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.document.DocumentRegistry;
import tc.oc.api.exceptions.SerializationException;
import tc.oc.api.message.types.ModelMessage;
import tc.oc.api.model.ModelRegistry;
import tc.oc.api.model.NoSuchModelException;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.reflect.InheritablePropertyVisitor;
import tc.oc.commons.core.reflect.Types;

@Singleton
public class MessageRegistry {

    protected final Logger logger;
    protected final DocumentRegistry documentRegistry;
    protected final ModelRegistry modelRegistry;
    private final Map<String, MessageMeta<?>> byName = new HashedMap();

    private final LoadingCache<Class<? extends Message>, MessageMeta> byType = CacheBuilder.newBuilder().build(
        new CacheLoader<Class<? extends Message>, MessageMeta>() {
            @Override
            public MessageMeta load(Class<? extends Message> type) throws Exception {
                // Registered types are explicitly inserted into the cache,
                // so a cache miss means the type itself is not a registered
                // message, and we need to look for an ancestor that is.
                return findAncestorMeta(type);
            }
        }
    );

    @Inject MessageRegistry(Loggers loggers, Set<MessageMeta<?>> messages, DocumentRegistry documentRegistry, ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
        this.logger = loggers.get(getClass());
        this.documentRegistry = documentRegistry;

        messages.forEach(meta -> {
            if(ModelMessage.class.isAssignableFrom(meta.type())) {
                if(meta.type().getTypeParameters().length > 1) {
                    throw new SerializationException(ModelMessage.class.getSimpleName() + " subtype must have no more than one type parameter");
                }
            }

            if(byName.containsKey(meta.name())) {
                throw new SerializationException("Tried to register multiple message types for name " + meta.name());
            }

            byName.put(meta.name(), meta);
            byType.put(meta.type(), meta);
        });
    }

    public TypeToken<? extends Message> resolve(String name) {
        return resolve(name, Optional.empty());
    }

    public TypeToken<? extends Message> resolve(String name, Optional<String> modelName) {
        final MessageMeta<?> meta = byName.get(name);
        if(meta == null) {
            throw new NoSuchMessageException("No registered message type named " + name);
        }

        TypeToken token = TypeToken.of(meta.type());
        if(ModelMessage.class.isAssignableFrom(meta.type()) && modelName.isPresent()) {
            try {
                token = modelMessageType(token, modelRegistry.resolve(modelName.get()).completeType());
            } catch(NoSuchModelException e) {
                throw new NoSuchMessageException(e.getMessage());
            }
        }

        return token;
    }

    private static <M extends Model, N extends ModelMessage<M>> TypeToken<N> modelMessageType(TypeToken<N> messageType, TypeToken<M> modelType) {
        if(messageType.getRawType().getTypeParameters().length == 0) {
            return messageType;
        } else {
            return (TypeToken<N>) TypeToken.of(new ParameterizedType() {
                @Override
                public Type[] getActualTypeArguments() {
                    return new Type[]{ modelType.getType() };
                }

                @Override
                public Type getRawType() {
                    return messageType.getRawType();
                }

                @Override
                public Type getOwnerType() {
                    return messageType.getRawType().getEnclosingClass();
                }
            });
        }
    }

    public String typeName(Class<? extends Message> type) {
        return getMeta(type).name();
    }

    private <T extends Message> MessageMeta<? super T> getMeta(Class<T> type) {
        return byType.getUnchecked(type);
    }

    private <T extends Message> MessageMeta<? super T> findAncestorMeta(final Class<T> type) {
        // We don't want to trigger any cache loads, we just want to search what is
        // already in the cache, which is exactly what this map view does.
        final Map<Class<? extends Message>, MessageMeta> byTypeMap = byType.asMap();

        Map<Class<?>, MessageMeta> metas = Types.walkAncestors(
            type,
            Types.assignableTo(Message.class),
            new InheritablePropertyVisitor<>(new Function<Class<?>, MessageMeta>() {
                @Override
                public @Nullable MessageMeta apply(Class<?> cls) {
                    if(type == cls) return null;
                    return byTypeMap.get(cls);
                }
            })
        ).values();

        switch(metas.size()) {
            case 0: throw new SerializationException("No name found for message type " + type.getName());
            case 1: return metas.values().iterator().next();
            default: throw new SerializationException("Ambiguous name for message type " + type.getName() + ": could be any of " + Joiner.on(", ").join(metas.values()));
        }
    }

    private boolean isInstantiable(Class<? extends Message> type) {
        if(
            type.isAnonymousClass() ||
            type.isLocalClass() ||
            type.isMemberClass() ||
            type.isSynthetic() ||
            Modifier.isAbstract(type.getModifiers())
        ) return false;

        try {
            type.getDeclaredConstructor().setAccessible(true);
        } catch(NoSuchMethodException e) {
            return false;
        }

        return true;
    }
}
