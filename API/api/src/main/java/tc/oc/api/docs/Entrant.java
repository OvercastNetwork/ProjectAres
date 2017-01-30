package tc.oc.api.docs;

import java.util.List;
import javax.annotation.Nonnull;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.team.Team;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.docs.virtual.Model;

@Serialize
public interface Entrant extends Model {
    @Nonnull Team team();
    @Nonnull List<PlayerId> members();
    @Nonnull List<MatchDoc> matches();
}
