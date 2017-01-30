package tc.oc.commons.core.commands;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.sk89q.minecraft.util.commands.CommandContext;
import tc.oc.commons.core.util.ArrayUtils;
import tc.oc.minecraft.api.command.CommandSender;

public class CommandInvocationInfo {

    private final Optional<String> sender;
    private final Optional<String> command;
    private final List<String> args;

    private CommandInvocationInfo(Optional<String> sender, Optional<String> command, Iterable<String> args) {
        this.sender = sender;
        this.command = command;
        this.args = ImmutableList.copyOf(args);
    }

    public Optional<String> sender() {
        return sender;
    }

    public Optional<String> command() {
        return command;
    }

    public List<String> args() {
        return args;
    }

    public Optional<String> commandLine() {
        return command().map(cmd -> Stream.concat(Stream.of(cmd), args().stream()).collect(Collectors.joining(" ")));
    }

    private static final CommandInvocationInfo EMPTY = new CommandInvocationInfo(Optional.empty(), Optional.empty(), ImmutableList.of());

    public static CommandInvocationInfo empty() {
        return EMPTY;
    }

    public static CommandInvocationInfo of(@Nullable String sender, @Nullable String command, @Nullable Iterable<String> args) {
        if(sender == null && command == null) return empty();

        return new CommandInvocationInfo(Optional.ofNullable(sender),
                                         Optional.ofNullable(command),
                                         args != null ? args : ImmutableList.of());
    }

    public static CommandInvocationInfo of(@Nullable CommandSender sender, @Nullable String command, @Nullable Iterable<String> args) {
        return of(sender == null ? null : sender.getName(),
                  command,
                  args);
    }

    public static CommandInvocationInfo of(@Nullable CommandSender sender, @Nullable CommandContext context) {
        return of(sender,
                  context == null ? null : context.getCommand(),
                  context == null ? null : ArrayUtils.asSubListFrom(1, context.getOriginalArgs()));
    }

    public static CommandInvocationInfo of(@Nullable CommandSender sender) {
        return of(sender, null);
    }
}
