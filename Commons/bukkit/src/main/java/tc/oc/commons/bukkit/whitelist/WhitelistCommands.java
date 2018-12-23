package tc.oc.commons.bukkit.whitelist;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import java.util.Iterator;
import javax.inject.Inject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.ListComponent;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.commands.UserFinder;
import tc.oc.commons.bukkit.format.MiscFormatter;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.commands.CommandFutureCallback;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;
import tc.oc.commons.core.commands.TranslatableCommandException;
import tc.oc.minecraft.scheduler.SyncExecutor;

public class WhitelistCommands implements NestedCommands {

    public static class Parent implements Commands {
        @Command(
            aliases = {"whitelist", "wl"},
            desc = "Commands to manipulate the player whitelist",
            min = 1,
            max = -1
        )
        @NestedCommand(WhitelistCommands.class)
        @CommandPermissions(Whitelist.EDIT_PERM)
        public void whitelist() {}
    }

    private final Whitelist whitelist;
    private final SyncExecutor syncExecutor;
    private final Audiences audiences;
    private final MiscFormatter misc;
    private final IdentityProvider identities;
    private final UserFinder userFinder;

    @Inject WhitelistCommands(Whitelist whitelist, SyncExecutor syncExecutor, Audiences audiences, MiscFormatter misc, IdentityProvider identities, UserFinder userFinder) {
        this.whitelist = whitelist;
        this.syncExecutor = syncExecutor;
        this.audiences = audiences;
        this.misc = misc;
        this.identities = identities;
        this.userFinder = userFinder;
    }

    @Command(
        aliases = {"status", "state"},
        desc = "Check if the whitelist is on or off",
        min = 0,
        max = 0
    )
    public void status(CommandContext args, CommandSender sender) throws CommandException {
        audiences.get(sender).sendMessage(
            new TranslatableComponent(
                "whitelist.status",
                misc.abled(whitelist.isEnabled())
            )
        );
    }

    @Command(
        aliases = {"on", "enable"},
        desc = "Enable the whitelist",
        min = 0,
        max = 0
    )
    public void enable(CommandContext args, CommandSender sender) throws CommandException {
        whitelist.setEnabled(true);
        status(args, sender);
    }

    @Command(
        aliases = {"off", "disable"},
        desc = "Disable the whitelist",
        min = 0,
        max = 0
    )
    public void disable(CommandContext args, CommandSender sender) throws CommandException {
        whitelist.setEnabled(false);
        status(args, sender);
    }

    @Command(
        aliases = {"list", "show"},
        desc = "Show the complete whitelist",
        min = 0,
        max = 0
    )
    public void list(CommandContext args, CommandSender sender) throws CommandException {
        final Audience audience = audiences.get(sender);
        if(whitelist.isEmpty()) {
            audience.sendMessage(new TranslatableComponent("whitelist.empty"));
            return;
        }

        final ListComponent list = new ListComponent(
            whitelist.stream()
                .map(identities::currentIdentity)
                .map(identity -> new PlayerComponent(identity, NameStyle.FANCY))
        );
        audience.sendMessage(
            new Component()
                .extra(new TranslatableComponent("whitelist.size", new Component(whitelist.size(), ChatColor.AQUA)))
                .extra(": ")
                .extra(list)
        );
    }

    @Command(
        aliases = {"reset", "clear"},
        desc = "Reset whitelist members to default",
        min = 0,
        max = 0
    )
    public void clear(CommandContext args, final CommandSender sender) throws CommandException {
        whitelist.reset();
        audiences.get(sender).sendMessage(new TranslatableComponent(whitelist.isEmpty() ? "whitelist.empty" : "whitelist.default"));
    }

    @Command(
        aliases = {"add"},
        desc = "Add a player to the whitelist",
        usage = "<username>",
        min = 1,
        max = 1
    )
    public void add(CommandContext args, final CommandSender sender) throws CommandException {
        syncExecutor.callback(
            userFinder.findUser(sender, args, 0),
            CommandFutureCallback.onSuccess(sender, args, result -> {
                whitelist.add(result.user);
                audiences.get(sender).sendMessage(
                    new TranslatableComponent(
                        "whitelist.add",
                        new PlayerComponent(identities.currentIdentity(result.user), NameStyle.FANCY)
                    )
                );
            })
        );
    }

    @Command(
        aliases = {"remove"},
        desc = "Remove a player from the whitelist",
        usage = "<username>",
        min = 1,
        max = 1
    )
    public void remove(CommandContext args, final CommandSender sender) throws CommandException {
        final String username = args.getString(0);
        for(Iterator<PlayerId> iter = whitelist.iterator(); iter.hasNext();) {
            PlayerId playerId = iter.next();
            if(username.equalsIgnoreCase(playerId.username())) {
                iter.remove();
                audiences.get(sender).sendMessage(
                    new TranslatableComponent(
                        "whitelist.remove",
                        new PlayerComponent(identities.currentIdentity(playerId), NameStyle.FANCY)
                    )
                );
                return;
            }
        }
        throw new TranslatableCommandException("whitelist.notFound", username);
    }

    @Command(
        aliases = {"all"},
        desc = "Whitelist all players currently on the server",
        min = 0,
        max = 0
    )
    public void all(CommandContext args, final CommandSender sender) throws CommandException {
        audiences.get(sender).sendMessage(new TranslatableComponent(
            "whitelist.addMulti",
            new Component(whitelist.addAllOnline(), ChatColor.AQUA)
        ));
    }


    @Command(
        aliases = {"kick"},
        desc = "Kick any players not currently on the whitelist",
        min = 0,
        max = 0
    )
    public void kick(CommandContext args, final CommandSender sender) throws CommandException {
        audiences.get(sender).sendMessage(new TranslatableComponent(
            "whitelist.kickMulti",
            new Component(whitelist.kickAll(), ChatColor.AQUA)
        ));
    }
}
