package tc.oc.api.docs.virtual;

import javax.annotation.Nullable;

import java.time.Duration;
import java.time.Instant;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.api.model.ModelName;

@Serialize
@ModelName("Engagement")
public interface EngagementDoc extends Model {
    String family_id();
    String server_id();
    String user_id();
    MapDoc.Genre genre();

    String match_id();
    Instant match_started_at();
    Instant match_joined_at();
    @Nullable Instant match_finished_at();
    @Nullable Duration match_length();
    @Nullable Duration match_participation();

    boolean committed();

    String map_id();
    SemanticVersion map_version();

    int player_count();
    int competitor_count();

    @Nullable String team_pgm_id();
    @Nullable Integer team_size();
    @Nullable Duration team_participation();

    @Nullable Integer rank();
    @Nullable Integer tied_count();

    enum ForfeitReason {
        ABSENCE,
        PARTICIPATION_PERCENT,
        CUMULATIVE_ABSENCE,
        CONTINUOUS_ABSENCE
    }
    @Nullable ForfeitReason forfeit_reason();
}
