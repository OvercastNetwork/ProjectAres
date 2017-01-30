package tc.oc.api.tourney;

import java.util.Set;
import javax.annotation.Nonnull;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.Entrant;
import tc.oc.api.docs.virtual.Document;
import tc.oc.api.docs.virtual.MatchDoc;

@Serialize
public interface RecordMatchResponse extends Document {
    @Nonnull MatchDoc match();
    @Nonnull Set<Entrant> entrants();
}
