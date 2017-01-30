package net.anxuiz.tourney.task;

import javax.annotation.Nullable;

import net.anxuiz.tourney.Tourney;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import java.time.Instant;

public abstract class Task implements Runnable {
    protected Instant start;
    protected @Nullable BukkitTask task;

    public void start() {
        this.start = Instant.now();
        this.task = Bukkit.getScheduler().runTaskTimer(Tourney.get(), this, 0, 20);
    }

    public void cancel() {
        if (this.task != null) {
            Bukkit.getScheduler().cancelTask(this.task.getTaskId());
        }
    }

    public @Nullable Instant getStart() {
        return this.start;
    }

    @Override
    public abstract void run();

}
