package tc.oc.pgm.broadcast;

import java.util.List;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.pgm.events.MatchBeginEvent;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.match.MatchAudiences;
import tc.oc.pgm.match.MatchScheduler;

public class BroadcastScheduler implements Listener {

    private final List<Broadcast> broadcasts;
    private final MatchAudiences audiences;
    private final MatchScheduler scheduler;

    private List<BroadcastTask> tasks = ImmutableList.of();

    @Inject BroadcastScheduler(List<Broadcast> broadcasts, MatchAudiences audiences, MatchScheduler scheduler) {
        this.broadcasts = broadcasts;
        this.audiences = audiences;
        this.scheduler = scheduler;
    }

    @EventHandler
    public void matchBegin(MatchBeginEvent event) {
        tasks = broadcasts.stream()
                          .map(BroadcastTask::new)
                          .collect(Collectors.toImmutableList());
    }

    @EventHandler
    public void matchEnd(MatchEndEvent event) {
        tasks.forEach(BroadcastTask::cancel);
    }

    private class BroadcastTask {
        final Broadcast broadcast;
        final Task task;
        int count = 0;

        BroadcastTask(Broadcast broadcast) {
            this.broadcast = broadcast;
            this.task = scheduler.createRepeatingTask(broadcast.after, broadcast.every, this::send);
        }

        void cancel() {
            task.cancel();
        }

        void send() {
            final Audience audience = audiences.filter(broadcast.filter);
            audience.sendMessage(broadcast.getFormattedMessage());
            audience.playSound(broadcast.getSound());

            if(++count >= broadcast.count) {
                cancel();
            }
        }
    }
}
