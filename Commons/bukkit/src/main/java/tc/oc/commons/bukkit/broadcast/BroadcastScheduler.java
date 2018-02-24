package tc.oc.commons.bukkit.broadcast;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.ConsoleCommandSender;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.Server;
import tc.oc.commons.bukkit.broadcast.model.BroadcastSchedule;
import tc.oc.commons.bukkit.broadcast.model.BroadcastSet;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.localization.LocalizedMessageMap;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.scheduler.Scheduler;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Pair;
import tc.oc.parse.DocumentWatcher;

/**
 * Manages all broadcast messages
 */
@Singleton
public class BroadcastScheduler implements PluginFacet {

    // Configuration file, relative to config root
    private static final Path CONFIG_FILE = Paths.get("broadcasts.xml");

    private static final Path SOURCES_PATH = Paths.get("localized/broadcasts");

    // Base path for (untranslated) message sources, relative to config root
    private static final Path TRANSLATIONS_PATH = Paths.get("broadcasts");

    private final Logger logger;
    private final Path configPath;
    private final Path configFile;
    private final Scheduler scheduler;
    private final ConsoleCommandSender console;
    private final OnlinePlayers onlinePlayers;
    private final LocalizedMessageMap.Factory messageMapFactory;
    private final DocumentWatcher.Factory<List<BroadcastSchedule>> documentWatcherFactory;
    private final Server localServer;
    private final BroadcastFormatter formatter;
    private final ComponentRenderContext renderer;
    private final BroadcastSettings settings;

    private final Random random = new Random();

    private DocumentWatcher<List<BroadcastSchedule>> scheduleWatcher;
    private List<ScheduledTask> tasks = ImmutableList.of();

    @Inject BroadcastScheduler(Loggers loggers,
                               @Named("configuration") Path configPath,
                               Scheduler scheduler,
                               ConsoleCommandSender console,
                               OnlinePlayers onlinePlayers,
                               LocalizedMessageMap.Factory messageMapFactory,
                               DocumentWatcher.Factory<List<BroadcastSchedule>> documentWatcherFactory,
                               Server localServer,
                               BroadcastFormatter formatter,
                               ComponentRenderContext renderer,
                               BroadcastSettings settings) {

        this.logger = loggers.get(getClass());
        this.configPath = configPath;
        this.configFile = configPath.resolve(CONFIG_FILE);
        this.scheduler = scheduler;
        this.console = console;
        this.onlinePlayers = onlinePlayers;
        this.messageMapFactory = messageMapFactory;
        this.documentWatcherFactory = documentWatcherFactory;
        this.localServer = localServer;
        this.formatter = formatter;
        this.renderer = renderer;
        this.settings = settings;
    }

    @Override
    public void enable() {
        scheduleWatcher = documentWatcherFactory.create(configFile, schedule -> {
            tasks.forEach(ScheduledTask::cancel);
            tasks = schedule.map(doc -> ImmutableList.copyOf(Lists.transform(doc, ScheduledTask::new)))
                            .orElse(ImmutableList.of());
        });
    }

    @Override
    public void disable() {
        tasks.forEach(ScheduledTask::cancel);

        if(scheduleWatcher != null) {
            scheduleWatcher.cancel();
            scheduleWatcher = null;
        }
    }

    /**
     * Handles a single {@link BroadcastSchedule}
     */
    private class ScheduledTask {

        final BroadcastSchedule schedule;
        final Map<BroadcastSet, LocalizedMessageMap> messages;
        final Task task;

        ScheduledTask(BroadcastSchedule schedule) {
            logger.fine(() -> "Starting broadcast schedule " + schedule);

            this.schedule = schedule;
            this.messages = schedule.messages().stream().collect(Collectors.mappingTo(
                set -> messageMapFactory.create(configPath.resolve(SOURCES_PATH).resolve(set.path()),
                                                TRANSLATIONS_PATH.resolve(set.path())))
            );
            this.task = scheduler.createRepeatingTask(schedule.delay(), schedule.interval(), this::dispatch);
        }

        void dispatch() {
            if(!schedule.serverFilter().test(localServer)) return;

            final List<Pair<BroadcastSet, String>> choices = messages.entrySet()
                                                                     .stream()
                                                                     .flatMap(entry -> entry.getValue()
                                                                                            .keySet()
                                                                                            .stream()
                                                                                            .map(key -> Pair.of(entry.getKey(), key)))
                                                                     .collect(Collectors.toImmutableList());
            if(choices.isEmpty()) return;

            final Pair<BroadcastSet, String> choice = choices.get(random.nextInt(choices.size()));
            final BaseComponent message = formatter.broadcast(choice.first.prefix(),
                                                              messages.get(choice.first).get(choice.second));
            Stream.concat(Stream.of(console), onlinePlayers.all().stream()).forEach(viewer -> {
                if(settings.isVisible(choice.first.prefix(), viewer)) {
                    renderer.send(message, viewer);
                }
            });
        }

        void cancel() {
            task.cancel();
            messages.values().forEach(LocalizedMessageMap::disable);
        }
    }
}
