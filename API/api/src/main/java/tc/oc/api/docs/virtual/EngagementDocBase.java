package tc.oc.api.docs.virtual;

import javax.annotation.Nullable;

import java.time.Duration;
import java.time.Instant;
import tc.oc.api.docs.BasicModel;
import tc.oc.api.docs.SemanticVersion;

public abstract class EngagementDocBase extends BasicModel implements EngagementDoc {

    protected final MatchDoc matchDocument;

    protected EngagementDocBase(String _id, MatchDoc matchDocument) {
        super(_id);
        this.matchDocument = matchDocument;
    }

    @Override
    public String server_id() {
        return matchDocument.server_id();
    }

    @Override
    public String family_id() {
        return matchDocument.family_id();
    }

    @Override
    public MapDoc.Genre genre() {
        return matchDocument.map().genre();
    }

    @Override
    public String match_id() {
        return matchDocument._id();
    }

    @Override
    public Instant match_started_at() {
        return matchDocument.start();
    }

    @Override
    public @Nullable Instant match_finished_at() {
        return matchDocument.end();
    }

    @Override
    public @Nullable Duration match_length() {
        Instant start = matchDocument.start();
        Instant end = matchDocument.end();
        if(start != null && end != null) {
            return Duration.between(start, end);
        } else {
            return null;
        }
    }

    @Override
    public String map_id() {
        return matchDocument.map()._id();
    }

    @Override
    public SemanticVersion map_version() {
        return matchDocument.map().version();
    }

    @Override
    public int player_count() {
        return matchDocument.player_count();
    }

    @Override
    public int competitor_count() {
        return matchDocument.competitors().size();
    }
}
