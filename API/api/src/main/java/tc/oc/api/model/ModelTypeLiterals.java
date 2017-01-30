package tc.oc.api.model;

import com.google.inject.TypeLiteral;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.docs.virtual.PartialModel;
import tc.oc.api.queue.QueueQueryService;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;

public interface ModelTypeLiterals {

    default <P extends PartialModel, M extends Model> TypeLiteral<ModelMeta<M, P>> ModelMeta(TypeLiteral<M> M, TypeLiteral<P> P) {
        return new ResolvableType<ModelMeta<M, P>>(){}.with(new TypeArgument<M>(M){},
                                                            new TypeArgument<P>(P){});
    }
    default <P extends PartialModel, M extends Model> TypeLiteral<ModelMeta<M, P>> ModelMeta(Class<M> M, Class<P> P) {
        return new ResolvableType<ModelMeta<M, P>>(){}.with(new TypeArgument<M>(M){},
                                                            new TypeArgument<P>(P){});
    }

    default <P extends PartialModel, M extends Model> TypeLiteral<ModelService<M, P>> ModelService(TypeLiteral<M> M, TypeLiteral<P> P) {
        return new ResolvableType<ModelService<M, P>>(){}.with(new TypeArgument<M>(M){},
                                                               new TypeArgument<P>(P){});
    }
    default <P extends PartialModel, M extends Model> TypeLiteral<ModelService<M, P>> ModelService(Class<M> M, Class<P> P) {
        return new ResolvableType<ModelService<M, P>>(){}.with(new TypeArgument<M>(M){},
                                                               new TypeArgument<P>(P){});
    }

    default <P extends PartialModel, M extends Model> TypeLiteral<NullModelService<M, P>> NullModelService(TypeLiteral<M> M, TypeLiteral<P> P) {
        return new ResolvableType<NullModelService<M, P>>(){}.with(new TypeArgument<M>(M){},
                                                                   new TypeArgument<P>(P){});
    }
    default <P extends PartialModel, M extends Model> TypeLiteral<NullModelService<M, P>> NullModelService(Class<M> M, Class<P> P) {
        return new ResolvableType<NullModelService<M, P>>(){}.with(new TypeArgument<M>(M){},
                                                                   new TypeArgument<P>(P){});
    }

    default <P extends PartialModel, M extends Model> TypeLiteral<HttpModelService<M, P>> HttpModelService(TypeLiteral<M> M, TypeLiteral<P> P) {
        return new ResolvableType<HttpModelService<M, P>>(){}.with(new TypeArgument<M>(M){},
                                                                   new TypeArgument<P>(P){});
    }
    default <P extends PartialModel, M extends Model> TypeLiteral<HttpModelService<M, P>> HttpModelService(Class<M> M, Class<P> P) {
        return new ResolvableType<HttpModelService<M, P>>(){}.with(new TypeArgument<M>(M){},
                                                                   new TypeArgument<P>(P){});
    }

    default <M extends Model> TypeLiteral<QueryService<M>> QueryService(TypeLiteral<M> M) {
        return new ResolvableType<QueryService<M>>(){}.with(new TypeArgument<M>(M){});
    }
    default <M extends Model> TypeLiteral<QueryService<M>> QueryService(Class<M> M) {
        return new ResolvableType<QueryService<M>>(){}.with(new TypeArgument<M>(M){});
    }

    default <M extends Model> TypeLiteral<NullQueryService<M>> NullQueryService(TypeLiteral<M> M) {
        return new ResolvableType<NullQueryService<M>>(){}.with(new TypeArgument<M>(M){});
    }
    default <M extends Model> TypeLiteral<NullQueryService<M>> NullQueryService(Class<M> M) {
        return new ResolvableType<NullQueryService<M>>(){}.with(new TypeArgument<M>(M){});
    }

    default <M extends Model> TypeLiteral<HttpQueryService<M>> HttpQueryService(TypeLiteral<M> M) {
        return new ResolvableType<HttpQueryService<M>>(){}.with(new TypeArgument<M>(M){});
    }
    default <M extends Model> TypeLiteral<HttpQueryService<M>> HttpQueryService(Class<M> M) {
        return new ResolvableType<HttpQueryService<M>>(){}.with(new TypeArgument<M>(M){});
    }

    default <M extends Model> TypeLiteral<QueueQueryService<M>> QueueQueryService(TypeLiteral<M> M) {
        return new ResolvableType<QueueQueryService<M>>(){}.with(new TypeArgument<M>(M){});
    }
    default <M extends Model> TypeLiteral<QueueQueryService<M>> QueueQueryService(Class<M> M) {
        return new ResolvableType<QueueQueryService<M>>(){}.with(new TypeArgument<M>(M){});
    }

    default <P extends PartialModel> TypeLiteral<UpdateService<P>> UpdateService(TypeLiteral<P> P) {
        return new ResolvableType<UpdateService<P>>(){}.with(new TypeArgument<P>(P){});
    }
    default <P extends PartialModel> TypeLiteral<UpdateService<P>> UpdateService(Class<P> P) {
        return new ResolvableType<UpdateService<P>>(){}.with(new TypeArgument<P>(P){});
    }

    default <P extends PartialModel> TypeLiteral<BatchUpdater<P>> BatchUpdater(TypeLiteral<P> P) {
        return new ResolvableType<BatchUpdater<P>>(){}.with(new TypeArgument<P>(P){});
    }
    default <P extends PartialModel> TypeLiteral<BatchUpdater<P>> BatchUpdater(Class<P> P) {
        return new ResolvableType<BatchUpdater<P>>(){}.with(new TypeArgument<P>(P){});
    }

    default <M extends Model> TypeLiteral<ModelStore<M>> ModelStore(TypeLiteral<M> M) {
        return new ResolvableType<ModelStore<M>>(){}.with(new TypeArgument<M>(M){});
    }
    default <M extends Model> TypeLiteral<ModelStore<M>> ModelStore(Class<M> M) {
        return new ResolvableType<ModelStore<M>>(){}.with(new TypeArgument<M>(M){});
    }
}
