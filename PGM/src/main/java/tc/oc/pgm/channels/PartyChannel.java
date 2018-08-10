package tc.oc.pgm.channels;

import com.google.inject.assistedinject.Assisted;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import tc.oc.api.docs.Chat;
import tc.oc.api.docs.virtual.ChatDoc;
import tc.oc.commons.bukkit.channels.SimpleChannel;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.MultiAudience;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;

import javax.inject.Inject;
import java.util.stream.Stream;

public class PartyChannel extends SimpleChannel implements MultiAudience {

    public static final String RECEIVE_ALL_PERMISSION = "pgm.chat.all.receive";

    public interface Factory {
        PartyChannel create(Party party);
    }

    private final Party party;

    @Inject PartyChannel(@Assisted Party party) {
        this.party = party;
    }

    public Party party() {
        return party;
    }

    @Override
    public BaseComponent prefix() {
        return party.getChatPrefix();
    }

    @Override
    public BaseComponent format(Chat chat, PlayerComponent player, String message) {
        return new Component(player).extra(": ").extra(message);
    }

    @Override
    public ChatDoc.Type type() {
        return ChatDoc.Type.TEAM;
    }

    @Override
    public boolean sendable(CommandSender sender) {
        return party.getPlayers().contains(party.getMatch().getPlayer(sender));
    }

    @Override
    public boolean viewable(CommandSender sender) {
        final MatchPlayer player = party.getMatch().getPlayer(sender);
        return sendable(sender) ||
               player == null ||
               (sender.hasPermission(RECEIVE_ALL_PERMISSION) && player.isObserving());
    }

}
