package tc.oc.api.reports;

import javax.annotation.Nonnull;
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
    @Serialize private final @Nullable String user_id;
    @Serialize private final boolean cross_server;

    private final int page, perPage;

    private ReportSearchRequest(String server_id, String user_id, boolean cross_server, int page, int perPage) {
        checkArgument(page > 0);
        checkArgument(perPage > 0);

        this.server_id = server_id;
        this.user_id = user_id;
        this.cross_server = cross_server;
        this.page = page;
        this.perPage = perPage;
    }

    public static ReportSearchRequest create(int page, int perPage) {
        return new ReportSearchRequest(null, null, false, page, perPage);
    }

    public ReportSearchRequest forServer(ServerDoc.Identity server, boolean cross_server) {
        return new ReportSearchRequest(server._id(), null, cross_server, page, perPage);
    }

    public ReportSearchRequest forPlayer(PlayerId playerId) {
        checkState(user_id == null);
        return new ReportSearchRequest(server_id, playerId._id(), true, page, perPage);
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
