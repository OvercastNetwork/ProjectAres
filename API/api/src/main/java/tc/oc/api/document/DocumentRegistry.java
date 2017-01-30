package tc.oc.api.document;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Injector;
import tc.oc.api.docs.BasicDeletableModel;
import tc.oc.api.docs.BasicModel;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.SimplePlayerId;
import tc.oc.api.docs.SimpleUserId;
import tc.oc.api.docs.UserId;
import tc.oc.api.docs.virtual.BasicDocument;
import tc.oc.api.docs.virtual.DeletableModel;
import tc.oc.api.docs.virtual.Document;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.exceptions.SerializationException;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.C3;
import tc.oc.commons.core.util.MapUtils;

/**
 * Cache of {@link DocumentMeta} records, populated on-demand when a {@link Document}
 * is serialized or deserialized.
 */
@Singleton
public class DocumentRegistry {

    protected final Logger logger;
    protected final DocumentGenerator generator;
    protected final Injector injector;

    private final LoadingCache<Class<? extends Document>, DocumentMeta> cache = CacheBuilder.newBuilder().build(
        new CacheLoader<Class<? extends Document>, DocumentMeta>() {
            @Override
            public DocumentMeta load(Class<? extends Document> type) throws Exception {
                return register(type);
            }
        }
    );

    @Inject DocumentRegistry(Loggers loggers, DocumentGenerator generator, Injector injector) {
        this.generator = generator;
        this.injector = injector;
        this.logger = loggers.get(getClass());
    }

    /**
     * Is the given document type directly instantiable? This is true only
     * if the type is a non-abstract class with a default constructor.
     */
    public boolean isInstantiable(Class<? extends Document> type) {
        if(type.isInterface() || Modifier.isAbstract(type.getModifiers())) return false;
        try {
            type.getDeclaredConstructor();
            return true;
        } catch(NoSuchMethodException e) {
            return false;
        }
    }

    public <T extends Document> T instantiate(Class<T> type, Map<String, Object> data) {
        return instantiate(getMeta(type), data);
    }

    public <T extends Document> T instantiate(DocumentMeta<T> meta, Map<String, Object> data) {
        if(meta.type().isInterface()) {
            // To create an interface document, choose the best base type,
            // instantiate that, and then wrap it in a proxy that implements
            // the rest of the properties.
            return generator.instantiate(meta, instantiate(getMeta(meta.baseType()), data), data);

        } else if(isInstantiable(meta.type())) {
            // If document type is directly instantiable, get an instance
            // from the injector and use setters to initialize it.
            final T doc = injector.getInstance(meta.type());
            for(Map.Entry<String, Setter> entry : meta.setters().entrySet()) {
                if(data.containsKey(entry.getKey())) {
                    entry.getValue().setUnchecked(doc, data.get(entry.getKey()));
                }
            }
            return doc;

        } else {
            throw new SerializationException("Document type " + meta.type().getName() + " is not instantiable");
        }
    }

    public <T extends Document> T copy(T original) {
        final DocumentMeta<T> meta = getMeta((Class<T>) original.getClass());
        return instantiate(meta, ImmutableMap.copyOf(Maps.transformValues(meta.getters(), getter -> getter.get(original))));
    }

    /**
     * Get (or create) the metadata for the given {@link Document} type.
     */
    public <T extends Document> DocumentMeta<T> getMeta(Class<T> type) {
        return cache.getUnchecked(type);
    }

    private <T extends Document> DocumentMeta<T> register(final Class<T> type) {
        logger.fine("Registering serializable type " + type);

        // Find property accessors declared directly on the given document
        final Map<String, Getter> getters = new HashMap<>();
        final Map<String, Setter> setters = new HashMap<>();
        for(Method method : DocumentMeta.serializedMethods(type)) {
            registerMethod(getters, setters, method);
        }
        for(Field field : DocumentMeta.serializedFields(type)) {
            registerField(getters, setters, field);
        }

        // Find the immediate supertypes of the document
        final List<DocumentMeta<? super T>> parents = new ArrayList<>();
        for(Class<? super T> parent : Types.parents(type)) {
            if(Document.class.isAssignableFrom(parent)) {
                parents.add((DocumentMeta<? super T>) getMeta(parent.asSubclass(Document.class)));
            }
        }

        // Merge all ancestors into a single list
        final List<DocumentMeta<? super T>> ancestors = ImmutableList.copyOf(
            C3.merge(
                Lists.transform(
                    parents,
                    (Function<DocumentMeta<? super T>, Collection<? extends DocumentMeta<? super T>>>) DocumentMeta::ancestors
                )
            )
        );

        if(logger.isLoggable(Level.FINE)) {
            logger.fine("Linearized ancestors for " + type + ": " + Joiner.on(", ").join(ancestors));
        }

        // Copy inherited accessors from ancestor documents
        for(DocumentMeta<? super T> ancestor : ancestors) {
            MapUtils.putAbsent(getters, ancestor.getters());
            MapUtils.putAbsent(setters, ancestor.setters());
        }

        return new DocumentMeta<>(type, ancestors, bestBaseClass(type), getters, setters);
    }

    private static String serializedName(Member member) {
        if(member instanceof AnnotatedElement) {
            SerializedName nameAnnot = ((AnnotatedElement) member).getAnnotation(SerializedName.class);
            if(nameAnnot != null) return nameAnnot.value();
        }
        return member.getName();
    }

    private static @Nullable Type getterType(Method method) {
        if(method.getGenericParameterTypes().length == 0 && method.getGenericReturnType() != Void.TYPE) {
            return method.getGenericReturnType();
        }
        return null;
    }

    private static @Nullable Type setterType(Method method) {
        if(method.getGenericParameterTypes().length == 1) {
            return method.getGenericParameterTypes()[0];
        }
        return null;
    }

    private void registerMethod(Map<String, Getter> getters, Map<String, Setter> setters, Method method) {
        if(Modifier.isStatic(method.getModifiers())) return;

        final String name = serializedName(method);
        boolean accessor = false;

        if(getterType(method) != null) {
            accessor = true;
            if(!getters.containsKey(name)) {
                if(logger.isLoggable(Level.FINE)) {
                    logger.fine("  " + name + " -- get --> " + method);
                }
                getters.put(name, new GetterMethod(this, method));
            }
        }

        if(setterType(method) != null) {
            accessor = true;
            if(!setters.containsKey(name)) {
                if(logger.isLoggable(Level.FINE)) {
                    logger.fine("  " + name + " -- set --> " + method);
                }
                setters.put(name, new SetterMethod(this, method));
            }
        }

        if(!accessor) {
            throw new SerializationException("Serialized method " + method + " is not a valid getter or setter");
        }
    }

    private void registerField(Map<String, Getter> getters, Map<String, Setter> setters, Field field) {
        if(Modifier.isTransient(field.getModifiers()) ||
           Modifier.isStatic(field.getModifiers()) ||
           field.isSynthetic() ||
           field.isEnumConstant()) return;

        final String name = serializedName(field);
        final boolean gettable = !getters.containsKey(name);
        final boolean settable = !setters.containsKey(name);

        if(gettable || settable) {
            if(logger.isLoggable(Level.FINE)) {
                String access;
                if(gettable && settable) {
                    access = "get/set";
                } else if(gettable) {
                    access = "get";
                } else {
                    access = "set";
                }
                logger.fine("  " + name + " -- " + access + " --> " + field);
            }

            if(gettable) {
                getters.put(name, new FieldGetter(this, field));
            }

            if(settable) {
                setters.put(name, new FieldSetter(this, field));
            }
        }
    }

    // TODO: This could could be done in a more general way i.e. search
    // the registry for the best existing implementation to inherit from.
    public Class<? extends Document> bestBaseClass(Class<? extends Document> type) {
        if(PlayerId.class.isAssignableFrom(type)) {
            return SimplePlayerId.class;
        } else if(UserId.class.isAssignableFrom(type)) {
            return SimpleUserId.class;
        } else if(DeletableModel.class.isAssignableFrom(type)) {
            return BasicDeletableModel.class;
        } else if(Model.class.isAssignableFrom(type)) {
            return BasicModel.class;
        } else {
            return BasicDocument.class;
        }
    }
}
