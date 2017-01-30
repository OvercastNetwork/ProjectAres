package tc.oc.api.model;

import java.util.Collection;

import tc.oc.api.docs.virtual.PartialModel;

public interface BatchUpdater<T extends PartialModel> {

    void flush();

    void schedule();

    void update(T doc);

    default void updateMulti(Collection<T> doc) {
        doc.forEach(this::update);
    }
}

