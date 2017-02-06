package tc.oc.api.model;

import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;

import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.docs.virtual.PartialModel;
import tc.oc.api.queue.QueueQueryService;
import tc.oc.commons.core.inject.Binders;
import tc.oc.commons.core.inject.KeyedManifest;
import tc.oc.commons.core.inject.SingletonManifest;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.commons.core.reflect.TypeLiterals;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.ImmutableTypeMap;
import tc.oc.commons.core.util.TypeMap;
import tc.oc.inject.ProtectedBinder;
import tc.oc.minecraft.suspend.SuspendableBinder;

public class ModelBinder<M extends Model, P extends PartialModel> implements ModelTypeLiterals, TypeLiterals {

    private final TypeLiteral<M> M;
    private final TypeLiteral<P> P;
    private final Binders binder;
    private final Multibinder<ModelMeta> metas;
    private final OptionalBinder<QueryService<M>> queryServiceBinder;
    private final OptionalBinder<UpdateService<P>> updateServiceBinder;
    private final OptionalBinder<ModelService<M, P>> serviceBinder;
    private final OptionalBinder<ModelStore<M>> storeBinder;

    public static <M extends Model> ModelBinder<M, M> of(ProtectedBinder binder, Class<M> M) {
        return of(binder, M, M);
    }

    public static <M extends Model> ModelBinder<M, M> of(ProtectedBinder binder, TypeLiteral<M> M) {
        return of(binder, M, M);
    }

    public static <M extends Model, P extends PartialModel> ModelBinder<M, P> of(ProtectedBinder binder, Class<M> M, Class<P> P) {
        return of(binder, TypeLiteral.get(M), TypeLiteral.get(P));
    }

    public static <M extends Model, P extends PartialModel> ModelBinder<M, P> of(ProtectedBinder binder, TypeLiteral<M> M, TypeLiteral<P> P) {
        return new ModelBinder<>(binder, M, P);
    }

    private ModelBinder(ProtectedBinder protectedBinder, TypeLiteral<M> M, TypeLiteral<P> P) {
        this.binder = Binders.wrap(protectedBinder.publicBinder());
        this.M = M;
        this.P = P;

        this.metas = Multibinder.newSetBinder(binder, ModelMeta.class);
        this.serviceBinder = OptionalBinder.newOptionalBinder(binder, ModelService(M, P));
        this.queryServiceBinder = OptionalBinder.newOptionalBinder(binder, QueryService(M));
        this.updateServiceBinder = OptionalBinder.newOptionalBinder(binder, UpdateService(P));
        this.storeBinder = OptionalBinder.newOptionalBinder(binder, ModelStore(M));

        binder.install(new OneTime());
        binder.install(new PerModel());
    }

    public LinkedBindingBuilder<ModelStore<M>> bindStore() {
        binder.provisionEagerly(ModelStore(M));
        new SuspendableBinder(binder).addBinding().to(ModelStore(M));
        return storeBinder.setBinding();
    }

    public OptionalBinder<QueryService<M>> queryService() {
        return queryServiceBinder;
    }

    public OptionalBinder<UpdateService<P>> updateService() {
        return updateServiceBinder;
    }

    public LinkedBindingBuilder<ModelService<M, P>> bindDefaultService() {
        queryService().setDefault().to(ModelService(M, P));
        updateService().setDefault().to(ModelService(M, P));
        return serviceBinder.setDefault();
    }

    public LinkedBindingBuilder<ModelService<M, P>> bindService() {
        queryService().setBinding().to(ModelService(M, P));
        updateService().setBinding().to(ModelService(M, P));
        return serviceBinder.setBinding();
    }

    public TypeLiteral<NullModelService<M, P>> nullService() {
        return NullModelService(M, P);
    }

    public TypeLiteral<NullQueryService<M>> nullQueryService() {
        return NullQueryService(M);
    }

    public TypeLiteral<HttpModelService<M, P>> httpService() {
        return HttpModelService(M, P);
    }

    public TypeLiteral<HttpQueryService<M>> httpQueryService() {
        return HttpQueryService(M);
    }

    public TypeLiteral<QueueQueryService<M>> queueQueryService() {
        return QueueQueryService(M);
    }

    private class PerModel extends KeyedManifest {
        @Override
        protected Object manifestKey() {
            return M;
        }

        @Override
        protected void configure() {
            final TypeLiteral<ModelMeta<M, P>> meta = ModelMeta(M, P);
            metas.addBinding().to(meta);
            bind(meta).in(Singleton.class);
            bind(new ResolvableType<ModelMeta<M, ?>>(){}.with(new TypeArgument<M>(M){})).to(meta);
            bind(new ResolvableType<ModelMeta<?, P>>(){}.with(new TypeArgument<P>(P){})).to(meta);
        }
    }

    private class OneTime extends SingletonManifest {
        @Provides @Singleton
        Map<String, ModelMeta> byName(Set<ModelMeta> metas) {
            return metas.stream().collect(Collectors.indexingBy(ModelMeta::name));
        }

        @Provides @Singleton
        TypeMap<Model, ModelMeta> byType(Set<ModelMeta> metas) {
            final ImmutableTypeMap.Builder<Model, ModelMeta> builder = ImmutableTypeMap.builder();
            metas.forEach(meta -> builder.put(meta.completeType(), meta));
            return builder.build();
        }

        @Provides @Singleton
        TypeMap<PartialModel, ModelMeta> byPartialType(Set<ModelMeta> metas) {
            final ImmutableTypeMap.Builder<PartialModel, ModelMeta> builder = ImmutableTypeMap.builder();
            metas.forEach(meta -> builder.put(meta.partialType(), meta));
            return builder.build();
        }
    }
}
