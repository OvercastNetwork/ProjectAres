package tc.oc.commons.bukkit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.NestedCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.User;
import tc.oc.api.users.ChangeGroupRequest;
import tc.oc.api.users.UserService;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;
import tc.oc.commons.core.concurrent.Flexecutor;
import tc.oc.commons.core.util.ThrowingBiConsumer;
import tc.oc.minecraft.scheduler.Sync;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;

public class GroupCommands implements NestedCommands {

    public static class Parent implements Commands {
        @Command(
            aliases = { "group" },
            desc = "Commands to edit group membership",
            min = 1,
            max = -1
        )
        @NestedCommand(value = {GroupCommands.class})
        public void commands() throws CommandPermissionsException {}
    }

    private final Flexecutor flexecutor;
    private final BukkitUserStore userStore;
    private final UserService userService;
    private final UserFinder userFinder;
    private final Audiences audiences;

    @Inject GroupCommands(@Sync Flexecutor flexecutor, BukkitUserStore userStore, UserService userService, UserFinder userFinder, Audiences audiences) {
        this.flexecutor = flexecutor;
        this.userStore = userStore;
        this.userService = userService;
        this.userFinder = userFinder;
        this.audiences = audiences;
    }

    public void edit(final CommandContext args, final CommandSender sender, boolean add, boolean expire, ThrowingBiConsumer<User, String, Exception> consumer) throws CommandException {
        if(!(sender instanceof ConsoleCommandSender)) throw new CommandPermissionsException();
        flexecutor.callback(
            userFinder.findUser(sender, args, 0),
            response -> {
                String group = args.getString(1);
                flexecutor.callback(
                    userService.changeGroup(response.user, new ChangeGroupRequest() {
                        public String group() {
                            return group;
                        }
                        public String type() {
                            return add ? "join" : (expire ? "expire" : "leave");
                        }
                        public Instant end() {
                            try {
                                Duration duration = CommandUtils.getDuration(args, 2, null);
                                return add && duration != null ? Instant.now().plus(duration) : null;
                            } catch(CommandException e) {
                                return null;
                            }
                        }
                    }),
                    user -> consumer.acceptThrows(user, group)
                );
            }
        );
    }

    @Command(
        aliases = { "join" },
        desc = "Add a player to a group",
        usage = "<player> <group> [duration]",
        min = 2,
        max = 3
    )
    public void join(final CommandContext args, final CommandSender sender) throws CommandException {
        edit(args, sender, true, false, (User user, String group) -> {
            sender.sendMessage("Added " + user.username() + " to the " + group + " group");
        });
    }

    @Command(
        aliases = { "leave" },
        desc = "Remove a player to a group",
        usage = "<player> <group>",
        min = 2,
        max = 2
    )
    public void leave(final CommandContext args, final CommandSender sender) throws CommandException {
        edit(args, sender, false, false, (User user, String group) -> {
            sender.sendMessage("Removed " + user.username() + " from the " + group + " group");
        });
    }

    @Command(
        aliases = { "expire" },
        desc = "Expire a player's membership to a group",
        usage = "<player> <group>",
        min = 2,
        max = 2
    )
    public void expire(final CommandContext args, final CommandSender sender) throws CommandException {
        edit(args, sender, false, true, (User user, String group) -> {
            sender.sendMessage("Expired " + user.username() + "'s membership from the " + group + " group");
        });
    }

}
