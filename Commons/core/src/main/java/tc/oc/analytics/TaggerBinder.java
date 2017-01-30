package tc.oc.analytics;

import com.google.inject.Binder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

public class TaggerBinder {

    private final Multibinder<Tagger> taggers;

    public TaggerBinder(Binder binder) {
        this.taggers = Multibinder.newSetBinder(binder, Tagger.class);
    }

    public LinkedBindingBuilder<Tagger> addBinding() {
        return taggers.addBinding();
    }
}
