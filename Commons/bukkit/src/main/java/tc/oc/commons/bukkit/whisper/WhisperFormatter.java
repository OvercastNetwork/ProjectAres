package tc.oc.commons.bukkit.whisper;

import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.Whisper;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.chat.UserTextComponent;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.commons.bukkit.format.MiscFormatter;
import tc.oc.commons.bukkit.format.ServerFormatter;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.bukkit.settings.SettingManagerProvider;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.formatting.PeriodFormats;
import tc.oc.commons.core.util.Comparables;

public class WhisperFormatter {

    private static final Duration OLD_MESSAGE = Duration.ofSeconds(10);
    private static final BukkitSound MESSAGE_SOUND = new BukkitSound(Sound.ENTITY_PLAYER_LEVELUP, 1, 2);

    private final IdentityProvider identities;
    private final MiscFormatter miscFormatter;
    private final ServerFormatter serverFormatter;
    private final ServerStore serverStore;
    private final Server localServer;
    private final SettingManagerProvider playerSettings;
    private final Audiences audiences;

    @Inject WhisperFormatter(IdentityProvider identities, MiscFormatter miscFormatter, ServerStore serverStore, Server localServer, SettingManagerProvider playerSettings, Audiences audiences) {
        this.identities = identities;
        this.miscFormatter = miscFormatter;
        this.serverStore = serverStore;
        this.localServer = localServer;
        this.playerSettings = playerSettings;
        this.audiences = audiences;
        this.serverFormatter = ServerFormatter.dark;
    }

    public Identity senderIdentity(Whisper whisper) {
        return identities.createIdentity(whisper.sender_uid(), whisper.sender_nickname());
    }

    public Identity recipientIdentity(Whisper whisper) {
        return identities.createIdentity(whisper.recipient_uid(), whisper.recipient_specified());
    }

    private Component prefix() {
        return new Component(miscFormatter.typePrefix("PM"), ChatColor.GRAY);
    }

    public void send(CommandSender viewer, Whisper whisper) {
        final Identity sender = senderIdentity(whisper);
        final Identity recipient = recipientIdentity(whisper);
        final Audience audience = audiences.get(viewer);

        final Component display = prefix()
            .extra(new TranslatableComponent(sender.getNickname() == null ? "privateMessage.to"
                                                                          : "privateMessage.from.to",
                                             new PlayerComponent(sender, NameStyle.VERBOSE),
                                             new PlayerComponent(recipient, NameStyle.VERBOSE)))
            .extra(": ")
            .extra(new Component(new UserTextComponent(sender, whisper.content()), ChatColor.WHITE));

        audience.sendMessage(display);
        audiences.console().sendMessage(display);
    }

    public void receive(Player viewer, Whisper whisper) {
        final Identity sender = senderIdentity(whisper);
        final Identity recipient = recipientIdentity(whisper);
        final Audience audience = audiences.get(viewer);
        final boolean local = whisper.server_id().equals(localServer._id());

        final Component from = new Component();
        if(!local) {
            // Show sender server if not local. There is an edge case where this reveals nicked players:
            // if a player uses /reply to send a message from their real identity while they are nicked,
            // the recipient will know they are nicked because they are not reported as online, and they
            // will also know what server they are on. The former is unavoidable. The latter could be
            // avoided by saving a flag in the message indicating that the sender was disguised, but
            // that probably isn't worth the trouble. We might do it when we move PMs to the API.
            try {
                from.extra(serverFormatter.nameWithDatacenter(serverStore.byId(whisper._id()))).extra(" ");
            } catch(IllegalStateException e) {
                // Send the message without the server of origin if the document cannot be found
            }
        }
        from.extra(new PlayerComponent(sender, NameStyle.VERBOSE));

        // Show recipient identity if it is nicked OR if it is not the recipient's current identity
        String key = "privateMessage.from";
        if(recipient.getNickname() != null || !recipient.isCurrent()) {
            key += ".to";
        }

        final Duration age = Duration.between(whisper.sent(), Instant.now());
        if(Comparables.greaterThan(age, OLD_MESSAGE)) {
            // If message is old, show the time
            key += ".time";
        } else {
            // If message is new, play a sound
            if(playerSettings.getManager(viewer)
                             .getValue(WhisperSettings.sound(), WhisperSettings.Options.class)
                             .isAllowed(sender.familiarity(viewer))) {
                audience.playSound(MESSAGE_SOUND);
            }
        }

        final Component display = prefix()
            .extra(new TranslatableComponent(key,
                                             from,
                                             new PlayerComponent(recipient, NameStyle.VERBOSE),
                                             new Component(new TranslatableComponent("time.ago", PeriodFormats.briefNaturalApproximate(age)), ChatColor.GOLD)))
            .extra(": ")
            .extra(new Component(new UserTextComponent(whisper.content()), ChatColor.WHITE));

        audience.sendMessage(display);
        if(!local) {
            audiences.console().sendMessage(display);
        }
    }

    public void blocked(CommandSender viewer, Identity recipient) {
        audiences.get(viewer).sendMessage(new WarningComponent("command.message.blockedNoPermissions",
                                                               new PlayerComponent(recipient, NameStyle.VERBOSE)));
    }

    public void noReply(CommandSender viewer) {
        audiences.get(viewer).sendMessage(new WarningComponent("command.reply.noMessages"));
    }
}
