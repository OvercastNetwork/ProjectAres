package tc.oc.commons.bukkit.punishment;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.Punishment;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.PunishmentDoc;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.message.MessageListener;
import tc.oc.api.message.MessageQueue;
import tc.oc.api.message.types.ModelUpdate;
import tc.oc.api.model.UpdateService;
import tc.oc.api.util.Permissions;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.bukkit.settings.SettingManagerProvider;
import tc.oc.commons.bukkit.teleport.PlayerServerChanger;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.concurrent.Flexecutor;
import tc.oc.minecraft.api.event.Enableable;
import tc.oc.minecraft.scheduler.Sync;

import static tc.oc.commons.bukkit.punishment.PunishmentMessageSetting.Options;
import static tc.oc.commons.bukkit.punishment.PunishmentPermissions.LOOK_UP;
import static tc.oc.commons.bukkit.punishment.PunishmentPermissions.LOOK_UP_STALE;

@Singleton
public class PunishmentEnforcer implements Enableable, MessageListener {

    private final PunishmentFormatter punishmentFormatter;
    private final UpdateService<PunishmentDoc.Partial> punishmentService;
    private final MessageQueue queue;
    private final Flexecutor executor;
    private final Server localServer;
    private final OnlinePlayers players;
    private final IdentityProvider identities;
    private final Audiences audiences;
    private final PlayerServerChanger playerServerChanger;
    private final SettingManagerProvider settings;

    @Inject PunishmentEnforcer(PunishmentFormatter punishmentFormatter,
                               UpdateService<PunishmentDoc.Partial> punishmentService,
                               MessageQueue queue,
                               @Sync Flexecutor executor,
                               Server localServer,
                               OnlinePlayers players,
                               IdentityProvider identities,
                               Audiences audiences,
                               PlayerServerChanger playerServerChanger,
                               SettingManagerProvider settings) {

        this.punishmentFormatter = punishmentFormatter;
        this.punishmentService = punishmentService;
        this.queue = queue;
        this.executor = executor;
        this.localServer = localServer;
        this.players = players;
        this.identities = identities;
        this.audiences = audiences;
        this.playerServerChanger = playerServerChanger;
        this.settings = settings;
    }

    @Override
    public void enable() {
        queue.bind(ModelUpdate.class);
        queue.subscribe(this, executor);
    }

    @Override
    public void disable() {
        queue.unsubscribe(this);
    }

    @HandleMessage
    private void onUpdate(ModelUpdate<Punishment> message) {
        final Punishment punishment = message.document();
        if(!punishment.enforced()) {
            announce(punishment);
            enforce(punishment);
        }
    }

    private void enforce(Punishment punishment) {
        players.byUserId(punishment.punished()).ifPresent(punished -> enforce(punishment, punished));
    }

    private void enforce(Punishment punishment, Player punished) {
        final Audience audience = audiences.get(punished);
        switch(punishment.type()) {
            case WARN:
                audience.playSound(new BukkitSound(Sound.ENTITY_ENDERDRAGON_GROWL, 1f, 1f));
                punishmentFormatter.warning(punishment)
                                   .feed((title, subtitle) -> audience.showTitle(title, subtitle, 5, 200, 10));
                break;
            case KICK:
            case BAN:
                playerServerChanger.kickPlayer(punished, punishmentFormatter.screen(punishment, punished));
                break;
        }

        if(!punishment.off_record()) {
            punishmentService.update(punishment._id(), (PunishmentDoc.Enforce) () -> true);
        }
    }

    private void announce(Punishment punishment) {
        players.all()
               .stream()
               .filter(player -> viewable(player, punishment, true))
               .forEach(player -> audiences.get(player).sendMessages(
                   punishmentFormatter.format(punishment, true,
                                              !punishment.server_id().equals(localServer._id()))
               ));
    }

    public boolean viewable(CommandSender sender, Punishment punishment, boolean announced) {
        if(viewByIdentity(sender, punishment)) {
            if(announced) {
                return viewByType(sender, punishment) && viewBySetting(sender, punishment) && viewByIdentity(sender, punishment) && viewByRecord(sender, punishment);
            } else {
                return viewByLookup(sender, punishment);
            }
        }
        return false;
    }

    private boolean viewByIdentity(CommandSender sender, Punishment punishment) {
        return identities.currentOrConsoleIdentity(punishment.punisher()).isRevealed(sender);
    }

    private boolean viewBySetting(CommandSender sender, Punishment punishment) {
        switch(settings.tryManager(sender).map(m -> m.getValue(PunishmentMessageSetting.get(), Options.class, Options.SERVER)).orElse(Options.NONE)) {
            case GLOBAL:
                return true;
            case SERVER:
                return localServer._id().equals(punishment.server_id());
            case NONE:
            default:
                return false;
        }
    }
    
    private boolean viewByRecord(CommandSender sender, Punishment punishment) {
        if(punishment.off_record()) {
            return localServer._id().equals(punishment.server_id());   
        }
        return true;
    }

    private boolean viewByType(CommandSender sender, Punishment punishment) {
        switch(punishment.type()) {
            case WARN:
            case FORUM_BAN:
            case FORUM_WARN:
                return sender.hasPermission(Permissions.STAFF);
            case TOURNEY_BAN:
                return localServer.network().equals(ServerDoc.Network.TOURNAMENT);
            default:
                return true;
        }
    }

    private boolean viewByLookup(CommandSender sender, Punishment punishment) {
        return sender.hasPermission(punishment.stale() ? LOOK_UP_STALE : LOOK_UP);
    }

}
