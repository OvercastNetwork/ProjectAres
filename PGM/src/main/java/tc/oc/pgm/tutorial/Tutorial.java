package tc.oc.pgm.tutorial;

import java.util.Collection;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.commons.core.chat.Component;

public class Tutorial {

    private final ImmutableList<TutorialStage> stages;

    Tutorial(Collection<TutorialStage> stages) {
        this.stages = ImmutableList.copyOf(stages);
    }

    boolean hasStages() {
        return !stages.isEmpty();
    }

    @Nullable TutorialStage getNextStage(TutorialStage stage) {
        return this.getStage(stage, 1);
    }

    @Nullable TutorialStage getPreviousStage(TutorialStage stage) {
        return this.getStage(stage, -1);
    }

    @Nullable TutorialStage getStage(TutorialStage start, int offset) {
        int curIndex = start != null ? this.stages.indexOf(start) : -1;
        int nextIndex = curIndex + offset;

        if(nextIndex >= 0 && nextIndex < this.stages.size()) {
            return this.stages.get(nextIndex);
        } else {
            return null;
        }
    }

    BaseComponent renderNavigation(TutorialStage stage) {
        final TutorialStage next = getNextStage(stage);
        final TutorialStage prev = getPreviousStage(stage);
        final Component c = new Component(ChatColor.BOLD);

        if(prev != null) {
            c.extra(new Component(new TranslatableComponent("misc.leftClick"), ChatColor.DARK_GRAY));
            c.extra(new Component(" \u00AB ", ChatColor.AQUA));
            c.extra(new Component(prev.getTitle(), ChatColor.RED));
        }

        if(prev != null && next != null) {
            c.extra(new Component(" \u23A5 ", ChatColor.AQUA));
        }

        if(next != null) {
            c.extra(new Component(next.getTitle(), ChatColor.GREEN));
            c.extra(new Component(" \u00BB ", ChatColor.AQUA));
            c.extra(new Component(new TranslatableComponent("misc.rightClick"), ChatColor.DARK_GRAY));
        }

        return c;
    }
}
