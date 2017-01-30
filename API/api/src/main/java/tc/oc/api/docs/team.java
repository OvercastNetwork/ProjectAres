package tc.oc.api.docs;

import java.util.List;
import javax.annotation.Nonnull;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.docs.virtual.PartialModel;

public interface team {

    interface Partial extends PartialModel {}

    @Serialize
    interface Id extends Partial, Model {
        @Nonnull String name();
        @Nonnull String name_normalized();

    }

    @Serialize
    interface Members extends Partial {
        @Nonnull PlayerId leader();
        @Nonnull List<PlayerId> members();
    }

    interface Team extends Id, Members {}
}
