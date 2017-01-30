package tc.oc.commons.core.inject;

import com.google.inject.Binding;
import com.google.inject.PrivateBinder;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.PrivateElements;

/**
 * Expose the key to each visited {@link Binding} from the given {@link PrivateBinder}
 */
public class ElementExposer extends DefaultElementVisitor<Void> {

    private final PrivateBinder binder;

    public ElementExposer(PrivateBinder binder) {
        this.binder = binder;
    }

    @Override
    public <V> Void visit(Binding<V> binding) {
        binder.expose(binding.getKey());
        return super.visit(binding);
    }

    @Override
    public Void visit(PrivateElements privateElements) {
        privateElements.getExposedKeys().forEach(binder::expose);
        return super.visit(privateElements);
    }
}
