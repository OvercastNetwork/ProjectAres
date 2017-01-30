package tc.oc.api.model;

import java.util.function.Consumer;

import com.google.inject.TypeLiteral;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.docs.virtual.PartialModel;
import tc.oc.commons.core.inject.ProtectedBinders;

public interface ModelBinders extends ProtectedBinders, ModelTypeLiterals {

    default <M extends Model> ModelBinder<M, M> bindModel(Class<M> M) {
        return ModelBinder.of(this, M);
    }

    default <M extends Model> ModelBinder<M, M> bindModel(TypeLiteral<M> M) {
        return ModelBinder.of(this, M);
    }

    default <M extends Model, P extends PartialModel> ModelBinder<M, P> bindModel(Class<M> M, Class<P> P) {
        return ModelBinder.of(this, M, P);
    }

    default <M extends Model, P extends PartialModel> ModelBinder<M, P> bindModel(TypeLiteral<M> M, TypeLiteral<P> P) {
        return ModelBinder.of(this, M, P);
    }

    default <M extends Model> void bindModel(Class<M> M, Consumer<ModelBinder<M, M>> block) {
        block.accept(ModelBinder.of(this, M));
    }

    default <M extends Model> void bindModel(TypeLiteral<M> M, Consumer<ModelBinder<M, M>> block) {
        block.accept(ModelBinder.of(this, M));
    }

    default <M extends Model, P extends PartialModel> void bindModel(Class<M> M, Class<P> P, Consumer<ModelBinder<M, P>> block) {
        block.accept(ModelBinder.of(this, M, P));
    }

    default <M extends Model, P extends PartialModel> void bindModel(TypeLiteral<M> M, TypeLiteral<P> P, Consumer<ModelBinder<M, P>> block) {
        block.accept(ModelBinder.of(this, M, P));
    }
}
