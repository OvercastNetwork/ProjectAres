package tc.oc.commons.bukkit.nick;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.UserId;

@Singleton
public class ConsoleIdentity implements Identity {

    private static final String NAME = "Console";

    @Override
    public PlayerId getPlayerId() {
        return new PlayerId() {
            @Override
            public String username() {
                return NAME;
            }

            @Override
            public String _id() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String player_id() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public @Nullable String getNickname() {
        return null;
    }

    @Override
    public String getRealName() {
        return NAME;
    }

    @Override
    public String getPublicName() {
        return NAME;
    }

    @Override
    public @Nullable Player getPlayer() {
        return null;
    }

    @Override
    public boolean belongsTo(CommandSender sender) {
        return sender == Bukkit.getConsoleSender();
    }

    @Override
    public boolean isCurrent() {
        return true;
    }

    @Override
    public boolean isConsole() {
        return true;
    }

    @Override
    public String getName(CommandSender viewer) {
        return NAME;
    }

    @Override
    public boolean isOnline(CommandSender viewer) {
        return true;
    }


    @Override
    public @Nullable Player getPlayer(CommandSender viewer) {
        return null;
    }

    @Override
    public boolean isDead(CommandSender viewer) {
        return false;
    }

    @Override
    public boolean isFriend(CommandSender viewer) {
        return false;
    }

    @Override
    public Familiarity familiarity(CommandSender viewer) {
        return Familiarity.ANONYMOUS;
    }

    @Override
    public boolean isDisguised(CommandSender viewer) {
        return false;
    }

    @Override
    public boolean isRevealed(CommandSender viewer) {
        return true;
    }

    @Override
    public boolean belongsTo(UserId userId, CommandSender viewer) {
        return false;
    }

    @Override
    public boolean isSamePerson(Identity identity, CommandSender viewer) {
        return identity instanceof ConsoleIdentity;
    }
}
