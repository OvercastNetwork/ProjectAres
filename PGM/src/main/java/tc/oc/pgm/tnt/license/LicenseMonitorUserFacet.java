package tc.oc.pgm.tnt.license;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.api.docs.User;
import tc.oc.api.docs.virtual.UserDoc.License.Kill;
import tc.oc.api.docs.virtual.UserDoc.License.Stats;
import tc.oc.api.users.UserService;
import tc.oc.commons.bukkit.event.targeted.TargetedEventHandler;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.MatchUserFacet;
import tc.oc.pgm.match.ParticipantState;

/**
 * Monitors player actions to grant or revoke TNT licenses.
 */
@ListenerScope(MatchScope.LOADED)
public class LicenseMonitorUserFacet implements MatchUserFacet, Listener {

    private final static int ENEMY_KILLS_FOR_GRANT = 5;                 // Enemy kills required to automatically grant a license
    private final static int UNIQUE_ENEMY_KILLS_FOR_GRANT = 3;          // How many unique enemies must be within ENEMY_KILLS_FOR_GRANT
    private final static double TEAM_KILLS_PERCENT_FOR_REVOKE = 0.5;    // Percent of total kills that are team kills required to automatically revoke a license (must all be unique)
    private final static int KILL_WINDOW_MIN = ENEMY_KILLS_FOR_GRANT;   // Minimum window size
    private final static int KILL_WINDOW_MAX = 25;                      // Maximum window size
    private final static int KILL_WINDOW_INCREMENT_RATE = 50;           // How many enemy kills for the window size to increase by one

    private final User user;
    private final UserService userService;
    private final LicenseBroker licenseBroker;
    private final LicenseConfiguration licenseConfiguration;

    @Inject LicenseMonitorUserFacet(User user, UserService userService, LicenseBroker licenseBroker, LicenseConfiguration licenseConfiguration) {
        this.user = user;
        this.userService = userService;
        this.licenseBroker = licenseBroker;
        this.licenseConfiguration = licenseConfiguration;
    }

    private boolean canGrant() {
        return licenseConfiguration.autoGrant() && !user.hasTntLicense() && user.requestedTntLicense();
    }

    private boolean canRevoke() {
        return licenseConfiguration.autoRevoke() && user.hasTntLicense();
    }

    private boolean isActive() {
        return canGrant() || canRevoke();
    }

    private int windowSize() {
        return Math.max(KILL_WINDOW_MIN + (user.enemy_kills() / KILL_WINDOW_INCREMENT_RATE), KILL_WINDOW_MAX);
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onKill(MatchPlayerDeathEvent event) {
        if(!isActive()) return;
        if(event.isSelfKill()) return;

        final ParticipantState killer = event.getKiller();
        if(killer == null) return;
        if(!killer.getPlayerId().equals(user)) return;

        final List<Kill> kills = ImmutableList.<Kill>builder()
            .addAll(user.tnt_license_kills()
                        .subList(Math.max(0, user.tnt_license_kills().size() - windowSize()),
                                 user.tnt_license_kills().size()))
            .add(new Kill() {
                @Override public String victim_id() {
                    return event.getVictim().getPlayerId()._id();
                }

                @Override public boolean friendly() {
                    return event.isTeamKill();
                }
            })
            .build();

        final Set<String> uniqueEnemies = new HashSet<>();
        int enemies = 0;
        int teammates = 0;

        for(Kill kill : kills) {
            if(kill.friendly()) {
                teammates++;
            } else {
                uniqueEnemies.add(kill.victim_id());
                enemies++;
            }
        }

        if(canRevoke() && ((double) teammates) / windowSize() >= TEAM_KILLS_PERCENT_FOR_REVOKE) {
            licenseBroker.revoke(user, LicenseBroker.RevokeReason.TEAM_KILLS, true);
        } else if(canGrant() && teammates == 0 && enemies >= ENEMY_KILLS_FOR_GRANT && uniqueEnemies.size() >= UNIQUE_ENEMY_KILLS_FOR_GRANT) {
            licenseBroker.grant(user, LicenseBroker.GrantReason.ENEMY_KILLS);
        } else {
            userService.update(user, (Stats) () -> kills);
        }
    }
}
