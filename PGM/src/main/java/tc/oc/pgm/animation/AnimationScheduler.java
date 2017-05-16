package tc.oc.pgm.animation;

import com.google.common.collect.ImmutableList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.pgm.events.MatchBeginEvent;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.match.MatchScheduler;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class AnimationScheduler implements Listener {

    final List<AnimationDefinitionImpl.AnimationImpl> animations;
    private final MatchScheduler scheduler;

    private List<AnimationTask> tasks = ImmutableList.of();

    @Inject AnimationScheduler(MatchScheduler scheduler) {
        this.animations = new ArrayList<>();
        this.scheduler = scheduler;
    }

    @EventHandler
    public void matchBegin(MatchBeginEvent event) {
        tasks = animations.stream()
                .map(AnimationTask::new)
                .collect(Collectors.toImmutableList());
    }

    @EventHandler
    public void matchEnd(MatchEndEvent event) {
        tasks.forEach(AnimationTask::cancel);
    }

    public class AnimationTask {
        final AnimationDefinitionImpl.AnimationImpl animation;
        final Task task;
        int count = 0;
        int currentFrame = 0;

        AnimationTask(AnimationDefinitionImpl.AnimationImpl animation) {
            this.animation = animation;
            this.task = scheduler.createRepeatingTask(animation.getAfter(), animation.getLoop(), this::send);
        }

        void cancel() {
            task.cancel();
        }

        void send() {
            animation.place(animation.getFrames().get(currentFrame));

            this.currentFrame = currentFrame >= animation.getFrames().size() - 1 ? 0 : currentFrame + 1;

            if(++count >= animation.getCount()) {
                cancel();
            }
        }
    }
}
