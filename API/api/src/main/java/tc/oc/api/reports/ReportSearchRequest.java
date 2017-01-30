package tc.oc.api.reports;

import java.util.Collection;
import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Report;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.message.types.FindRequest;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class ReportSearchRequest extends FindRequest<Report> {

    @Serialize private final @Nullable String server_id;
    @Serialize private final @Nullable Collection<String> family_ids;
    @Serialize private final @Nullable String user_id;

    private final int page, perPage;

    private ReportSearchRequest(String server_id, Collection<String> family_ids, String user_id, int page, int perPage) {
        checkArgument(page > 0);
        checkArgument(perPage > 0);

        this.server_id = server_id;
        this.family_ids = family_ids;
        this.user_id = user_id;
        this.page = page;
        this.perPage = perPage;
    }

    public static ReportSearchRequest create(int page, int perPage) {
        return new ReportSearchRequest(null, null, null, page, perPage);
    }

    public ReportSearchRequest forServer(ServerDoc.Identity server) {
        checkState(server_id == null);
        return new ReportSearchRequest(server._id(), null, null, page, perPage);
    }

    public ReportSearchRequest forFamilies(Collection<String> familyIds) {
        checkState(family_ids == null);
        return new ReportSearchRequest(null, familyIds, null, page, perPage);
    }

    public ReportSearchRequest forPlayer(PlayerId playerId) {
        checkState(user_id == null);
        return new ReportSearchRequest(server_id, family_ids, playerId._id(), page, perPage);
    }

    @Override
    public Integer skip() {
        return (page - 1) * perPage;
    }

    @Override
    public Integer limit() {
        return perPage;
    }
}
