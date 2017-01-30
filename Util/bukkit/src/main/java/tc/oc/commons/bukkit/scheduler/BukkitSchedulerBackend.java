package tc.oc.commons.bukkit.scheduler;

import javax.inject.Inject;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import java.time.Duration;
import tc.oc.commons.core.plugin.PluginScoped;
import tc.oc.commons.core.scheduler.SchedulerBackend;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.commons.core.util.TimeUtils;

@PluginScoped
public class BukkitSchedulerBackend implements SchedulerBackend<BukkitTask> {

    private final Plugin plugin;
    private final BukkitScheduler bukkit;

    @Inject BukkitSchedulerBackend(Plugin plugin, BukkitScheduler bukkit) {
        this.plugin = plugin;
        this.bukkit = bukkit;
    }

    @Override
    public int taskId(BukkitTask bukkitTask) {
        return bukkitTask.getTaskId();
    }

    @Override
    public boolean isTaskQueued(BukkitTask bukkitTask) {
        return bukkit.isQueued(bukkitTask.getTaskId());
    }

    @Override
    public boolean isTaskRunning(BukkitTask bukkitTask) {
        return bukkit.isCurrentlyRunning(bukkitTask.getTaskId());
    }

    @Override
    public BukkitTask startTask(Task.Parameters schedule, Runnable runnable) {
        final Duration delay = schedule.delay();
        final Duration interval = schedule.interval();
        return bukkit.runTaskTimer(plugin,
                                   runnable,
                                   delay == null ? 0 : TimeUtils.toTicks(delay),
                                   interval == null ? -1 : TimeUtils.toTicks(interval));
    }

    @Override
    public void cancelTask(BukkitTask bukkitTask) {
        bukkitTask.cancel();
    }
}
