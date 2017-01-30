package tc.oc.commons.bukkit.report;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.util.concurrent.ListenableFuture;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Report;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.docs.virtual.ReportDoc;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.model.IdFactory;
import tc.oc.api.model.ModelService;
import tc.oc.api.util.Permissions;

@Singleton
public class ReportCreator {

    private static final EnumSet<GameMode> MODERATING_GAMEMODES = EnumSet.of(GameMode.CREATIVE, GameMode.SPECTATOR);

    private final ModelService<Report, ReportDoc.Partial> reportService;
    private final OnlinePlayers onlinePlayers;
    private final BukkitUserStore userStore;
    private final IdFactory idFactory;
    private final Server localServer;

    @Inject ReportCreator(ModelService<Report, ReportDoc.Partial> reportService, OnlinePlayers onlinePlayers, BukkitUserStore userStore, IdFactory idFactory, Server localServer) {
        this.reportService = reportService;
        this.onlinePlayers = onlinePlayers;
        this.userStore = userStore;
        this.idFactory = idFactory;
        this.localServer = localServer;
    }

    private boolean isModerating(Player player) {
        // HACK - we should have a better way to detect this
        return player.hasPermission(Permissions.STAFF) &&
               (MODERATING_GAMEMODES.contains(player.getGameMode()) ||
                localServer.role() != ServerDoc.Role.PGM);
    }

    public ListenableFuture<Report> createReport(PlayerId reportedId, @Nullable PlayerId reporterId, String reason, boolean automatic) {
        final String _id = idFactory.newId();

        return reportService.update(
            new ReportDoc.Creation() {
                @Override public String _id() {
                    return _id;
                }

                @Override public String scope() {
                    return "game";
                }

                @Override public boolean automatic() {
                    return automatic;
                }

                @Override public String family() {
                    return localServer.family();
                }

                @Override public String server_id() {
                    return localServer._id();
                }

                @Override public String match_id() {
                    final MatchDoc match = localServer.current_match();
                    return match == null ? null : match._id();
                }

                @Override public String reporter_id() {
                    return reporterId == null ? null : reporterId._id();
                }

                @Override public String reported_id() {
                    return reportedId._id();
                }

                @Override public String reason() {
                    return reason;
                }

                @Override public List<String> staff_online() {
                    return onlinePlayers.all()
                                        .stream()
                                        .filter(ReportCreator.this::isModerating)
                                        .map(player -> userStore.getUser(player).player_id())
                                        .collect(Collectors.toList());
                }
            }
        );
    }
}
