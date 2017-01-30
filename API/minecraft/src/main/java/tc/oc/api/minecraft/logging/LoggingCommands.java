package tc.oc.api.minecraft.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.google.common.base.Strings;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.minecraft.util.commands.SuggestException;
import net.md_5.bungee.api.ChatColor;
import org.apache.logging.log4j.spi.LoggerContext;
import tc.oc.api.util.Permissions;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;
import tc.oc.commons.core.logging.Logging;
import tc.oc.commons.core.logging.LoggingConfig;
import tc.oc.minecraft.api.command.CommandSender;

public class LoggingCommands implements NestedCommands {

    private final LoggingConfig loggingConfig;

    @Inject LoggingCommands(LoggingConfig loggingConfig) {
        this.loggingConfig = loggingConfig;
    }

    @Override
    public void enable() {
        // Verify this reflection magic works
        for(LoggerContext context : Logging.L4J.getContexts()) {
            Logging.L4J.getLoggers(context);
        }
    }

    private static String levelName(Level level) {
        if(level == Level.WARNING) {
            return "WARN";
        } else if(level == null) {
            return "parent";
        } else {
            return level.getName();
        }
    }

    private static String colorLevelName(Level level) {
        return Logging.levelColor(level) + levelName(level) + ChatColor.RESET;
    }

    private static String colorLevelName(org.apache.logging.log4j.Level level) {
        return Logging.levelColor(level) + level.name() + ChatColor.RESET;
    }

    private static String paddedLevelName(Level level) {
        return Logging.levelColor(level) + Strings.padEnd(levelName(level), 6, ' ') + ChatColor.RESET;
    }

    private static String paddedLevelName(org.apache.logging.log4j.Level level) {
        return Logging.levelColor(level) + Strings.padEnd(level.name(), 6, ' ') + ChatColor.RESET;
    }

    private static String loggerName(String literal) {
        if(literal == null || literal.length() == 0) {
            return "<root>";
        } else {
            return literal;
        }
    }

    private static String loggerName(Logger logger) {
        return loggerName(logger.getName());
    }

    private String loggerNameArg(CommandContext args, int index) throws CommandException, SuggestException {
        return args.string(index, Stream.concat(Logging.loggerNames(),
                                                Logging.L4J.loggerNames())
                                        .sorted()
                                        .collect(Collectors.toList()));
    }

    public static class Parent implements Commands {
        @Command(
            aliases = "log",
            desc = "Commands related to logging",
            min = 1,
            max = -1
        )
        @NestedCommand(LoggingCommands.class)
        @CommandPermissions(Permissions.DEVELOPER)
        public void log(CommandContext args, CommandSender sender) throws CommandException {}
    }

    @Command(
        aliases = "list",
        desc = "List all registered loggers",
        usage = "[prefix]",
        min = 0,
        max = 1
    )
    public void list(CommandContext args, CommandSender sender) throws CommandException {
        String prefix = args.getString(0, "");

        for(LoggerContext context : Logging.L4J.getContexts()) {
            Map<String, org.apache.logging.log4j.Logger> loggers = Logging.L4J.getLoggers(context);
            if(!loggers.isEmpty()) {
                List<String> names = new ArrayList<>(loggers.keySet());
                Collections.sort(names);
                boolean first = true;

                for(String name : names) {
                    if(name.startsWith(prefix)) {
                        if(first) {
                            first = false;
                            sender.sendMessage(ChatColor.YELLOW + "log4j Loggers (" + Logging.L4J.getContextName(context) + "):");
                        }
                        org.apache.logging.log4j.Logger logger = loggers.get(name);
                        sender.sendMessage("[" + paddedLevelName(Logging.L4J.getLevel(logger)) +
                                           "] [" + paddedLevelName(Logging.L4J.getEffectiveLevel(logger)) +
                                           "] " + loggerName(logger.getName()));
                    }
                }
            }
        }

        LogManager lm = LogManager.getLogManager();
        List<String> names = Collections.list(lm.getLoggerNames());
        if(!names.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "java.util.logging Loggers:");
            Collections.sort(names);
            for(String name : names) {
                if(name.startsWith(prefix)) {
                    Logger logger = lm.getLogger(name);
                    if(logger != null) {
                        sender.sendMessage("[" + paddedLevelName(logger.getLevel()) +
                                           "] [" + paddedLevelName(Logging.getEffectiveLevel(logger)) +
                                           "] " + loggerName(name));
                    }
                }
            }
        }
    }

    @Command(
        aliases = "level",
        desc = "Set or reset the level of a logger, or all loggers",
        usage = "<reset|off|severe|warning|info|config|fine|finer|finest|all> [jul|l4j] [root | <logger name>]",
        min = 1,
        max = 3
    )
    public void level(CommandContext args, CommandSender sender) throws CommandException, SuggestException {
        String levelName = args.getString(0).toUpperCase();

        boolean jul = true, l4j = true;
        if(args.argsLength() >= 2) {
            String subsystem = args.getString(1).toLowerCase();
            if("jul".equals(subsystem)) {
                l4j = false;
            } else if("l4j".equals(subsystem)) {
                jul = false;
            }
        }

        String loggerName = loggerNameArg(args, jul && l4j ? 1 : 2);

        if(jul) {
            Logger julLogger = Logging.findLogger(loggerName);
            if(julLogger != null) {
                Level level;
                if("RESET".equals(levelName)) {
                    level = null;
                } else {
                    level = Level.parse(levelName);
                }
                julLogger.setLevel(level);
                sender.sendMessage(ChatColor.WHITE + "Logger " + loggerName(julLogger) +
                                   " level " + (level == null ? "reset" : "set to " + colorLevelName(level)));
                return;
            }
        }

        if(l4j) {
            org.apache.logging.log4j.Logger l4jLogger = Logging.L4J.findLogger(loggerName);
            if(l4jLogger != null) {
                org.apache.logging.log4j.Level level = org.apache.logging.log4j.Level.valueOf(levelName);
                Logging.L4J.setLevel(l4jLogger, level);
                sender.sendMessage(ChatColor.WHITE + "Logger " + l4jLogger.getName() +
                                   " level " + (level == null ? "reset" : "set to " + colorLevelName(level)));
                return;
            }
        }

        throw new CommandException("No logger named '" + loggerName + "'");
    }

    @Command(
        aliases = "props",
        desc = "Dump the current JUL logging properties",
        min = 0,
        max = 0
    )
    public void props(CommandContext args, CommandSender sender) throws CommandException {
        try {
            for(Map.Entry<Object, Object> entry : Logging.getLoggingProperties().entrySet()) {
                sender.sendMessage(entry.getKey() + "=" + entry.getValue());
            }
        } catch(IllegalAccessException | NoSuchFieldException e) {
            throw new CommandException("Failed to get JUL logging properties", e);
        }
    }

    @Command(
        aliases = "load",
        desc = "Reload the logging configuration",
        min = 0,
        max = 0
    )
    public void load(CommandContext args, CommandSender sender) throws CommandException {
        loggingConfig.load();
        sender.sendMessage("Logging configuration reloaded");
    }
}
