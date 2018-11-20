package tc.oc.commons.bukkit.scheduler;

import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public abstract class RepeatingRunnable implements Runnable {

	private final Predicate<Integer> condition;
	private int taskId = -1;
	private int iteration = 0;

	public RepeatingRunnable() {
		this(dummy -> true);
	}

	public RepeatingRunnable(Predicate<Integer> condition) {
		this.condition = condition;
	}

	public RepeatingRunnable(final int repeat) {
		this(iteration -> iteration < repeat);
	}

	public final void run() {
		try {
			if (condition.test(iteration)) {
				repeat();
			} else {
				cancel();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		iteration++;
	}

	protected abstract void repeat();

	protected abstract Plugin plugin();

	public synchronized void cancel() throws IllegalStateException {
		Bukkit.getScheduler().cancelTask(this.getTaskId());
	}

	public synchronized int getTaskId() throws IllegalStateException {
		int id = this.taskId;
		if (id == -1) {
			throw new IllegalStateException("Not scheduled yet");
		} else {
			return id;
		}
	}

	public synchronized BukkitTask runTask(long delay, long period) throws IllegalArgumentException,
			IllegalStateException {
		this.checkState();
		return this.setupId(Bukkit.getScheduler().runTaskTimer(plugin(), this, delay, period));
	}

	private void checkState() {
		if (this.taskId != -1) {
			throw new IllegalStateException("Already scheduled as " + this.taskId);
		}
	}

	private BukkitTask setupId(BukkitTask task) {
		this.taskId = task.getTaskId();
		return task;
	}

	public synchronized BukkitTask runTaskAsync(long delay, long period) throws IllegalArgumentException,
			IllegalStateException {
		this.checkState();
		return this.setupId(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin(), this, delay,
				period));
	}

	public int getIteration() {
		return iteration;
	}

	@Override
	public int hashCode() {
		return getTaskId();
	}

	@Override
	public String toString() {
		return Integer.toString(getTaskId());
	}
}
