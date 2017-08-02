package tc.oc.pgm.tnt.license;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.event.EventBus;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.User;
import tc.oc.api.docs.UserId;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.docs.virtual.UserDoc.License.Grant;
import tc.oc.api.docs.virtual.UserDoc.License.Request;
import tc.oc.api.docs.virtual.UserDoc.License.Stats;
import tc.oc.api.users.UserService;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.minecraft.scheduler.MainThreadExecutor;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.pgm.match.MatchFinder;

/**
 * Handles granting and revoking TNT licenses.
 */
@Singleton
public class LicenseBroker implements PluginFacet {

    private final Logger logger;
    private final MainThreadExecutor executor;
    private final UserService userService;
    private final OnlinePlayers onlinePlayers;
    private final Audiences audiences;
    private final EventBus eventBus;
    private final MatchFinder matchPlayers;

    @Inject LicenseBroker(Loggers loggers, MainThreadExecutor executor, UserService userService, OnlinePlayers onlinePlayers, Audiences audiences, EventBus eventBus, MatchFinder matchPlayers) {
        this.logger = loggers.get(getClass());
        this.executor = executor;
        this.userService = userService;
        this.onlinePlayers = onlinePlayers;
        this.audiences = audiences;
        this.eventBus = eventBus;
        this.matchPlayers = matchPlayers;
    }

    /**
     * Display information about a player's TNT license.
     */
    public void information(User user, Audience audience) {
        if(user.hasTntLicense()) {
            audience.sendMessage(new Component(
                new TranslatableComponent("tnt.license.info.alreadyHas", "/tnt revoke"),
                ChatColor.YELLOW
            ));
        } else if(user.requested_tnt_license_at() != null) {
            audience.sendMessage(new Component(
                new TranslatableComponent("tnt.license.request.alreadyHas"),
                ChatColor.YELLOW
            ));
        } else {
            audience.sendMessage(new Component(
                new TranslatableComponent("tnt.license.info.doesNotHave", "/tnt request"),
                ChatColor.YELLOW
            ));
        }
    }

    /**
     * Request for a player to receive a TNT license.
     */
    public void request(User user, Audience audience) {
        if(user.requestedTntLicense()) {
            audience.sendMessage(new WarningComponent("tnt.license.request.alreadyHas"));
        } else {
            executor.callback(
                userService.update(user, new RequestLicense()),
                result -> {
                    logger.info(result.username() + " requested a TNT license");

                    audience.sendMessage(
                        new Component(new TranslatableComponent("tnt.license.request.success"),
                                      ChatColor.YELLOW)
                    );
                }
            );
        }
    }

    public enum GrantReason {
        ENEMY_KILLS, OBJECTIVES;
    }

    /**
     * Grants a player a TNT license.
     */
    public void grant(User user, GrantReason reason) {
        if(!user.hasTntLicense()) {
            executor.callback(userService.update(user, new GrantLicense()), user1 -> {
                logger.info(user.username() + " was granted a TNT license for " + reason);

                onlinePlayers.byUserId(user).ifPresent(player -> audiences.get(player).sendMessage(
                    new Component(
                        new TranslatableComponent(
                            "tnt.license.grant.success",
                            new TranslatableComponent("tnt.license.grant.reason." + reason.name().toLowerCase())
                        ), ChatColor.YELLOW
                    )
                ));
            });
        }
    }

    public void grant(UserId userId, GrantReason reason) {
        executor.callback(userService.find(userId), user -> grant(user, reason));

    }

    public enum RevokeReason {
        TEAM_KILLS, COMMAND;
    }

    /**
     * Revoke a TNT license from a player.
     */
    public void revoke(User user, RevokeReason reason, boolean auto) {
        final Audience audience = audiences.get(onlinePlayers.find(user));
        final boolean hadLicense = user.hasTntLicense();
        if(!(hadLicense || user.requestedTntLicense())) {
            if(!auto) {
                audience.sendMessage(new WarningComponent("tnt.license.revoke.hasNotRequested"));
            }
            return;
        }

        executor.callback(
            userService.update(
                user,
                auto ? new RevokeLicense() : new CancelLicense() // Manual revoke cancels the request as well, auto-revoke does not
            ),
            user1 -> {
                if(hadLicense) {
                    logger.info(user.username() + " lost a TNT license for " + reason + (auto ? " automatically" : " manually"));

                    audience.sendMessage(new Component(
                        new TranslatableComponent(
                            "tnt.license.revoke.success",
                            new TranslatableComponent("tnt.license.revoke.reason." + reason.name().toLowerCase())
                        ), ChatColor.YELLOW
                    ));
                } else if(!auto) {
                    audience.sendMessage(new WarningComponent("tnt.license.revoke.cancelled"));
                }

                matchPlayers.player(user).ifPresent(
                    player -> eventBus.callEvent(new LicenseRevokeEvent(player, hadLicense))
                );
            }
        );
    }

    private static class ResetStats implements Stats {
        @Override
        public List<UserDoc.License.Kill> tnt_license_kills() {
            return Collections.emptyList();
        }
    }

    private static class RequestLicense extends ResetStats implements Request {
        @Override
        public Instant requested_tnt_license_at() {
            return Instant.now();
        }
    }

    private static class GrantLicense extends ResetStats implements Grant {
        @Override
        public Instant granted_tnt_license_at() {
            return Instant.now();
        }
    }

    private static class RevokeLicense extends ResetStats implements Grant {
        @Override
        public @Nullable Instant granted_tnt_license_at() {
            return null;
        }
    }

    private static class CancelLicense extends RevokeLicense implements Request {
        @Override
        public @Nullable Instant requested_tnt_license_at() {
            return null;
        }
    }
}
