package tc.oc.pgm.tablist;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.api.docs.Server;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.commons.bukkit.tablist.DynamicTabEntry;
import tc.oc.commons.bukkit.tablist.TabView;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.formatting.PeriodFormats;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.PGMTranslations;
import tc.oc.commons.core.util.DefaultProvider;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class MatchFooterTabEntry extends DynamicTabEntry {

    public static class Factory implements DefaultProvider<Match, MatchFooterTabEntry> {
        @Override
        public MatchFooterTabEntry get(Match key) {
            return new MatchFooterTabEntry(key);
        }
    }

    @Inject private static MinecraftService minecraftService;

    private final Match match;
    private @Nullable Task tickTask;

    public MatchFooterTabEntry(Match match) {
        this.match = match;
    }

    @Override
    public void addToView(TabView view) {
        super.addToView(view);
        if(this.tickTask == null) {
            Runnable tick = new Runnable() {
                @Override public void run() {
                    MatchFooterTabEntry.this.invalidate();
                }
            };
            this.tickTask = match.getScheduler(MatchScope.LOADED).createRepeatingTask(5, 20, tick);
        }
    }

    @Override
    public void removeFromView(TabView view) {
        super.removeFromView(view);
        if(!this.hasViews() && this.tickTask != null) {
            this.tickTask.cancel();
            this.tickTask = null;
        }
    }

    @Override
    public BaseComponent getContent(TabView view) {
        Component content = new Component(ChatColor.DARK_GRAY);

        Server server = minecraftService.getLocalServer();
        String datacenter = server.datacenter();
        String name = server.name();

        if(datacenter != null) {
            content.extra(new Component(datacenter, ChatColor.WHITE, ChatColor.BOLD),
                          new Component(" - "));
        }

        content.extra(new Component(PGMTranslations.get().t("command.match.matchInfo.time", view.getViewer()) + ": ", ChatColor.GRAY),
                      new Component(PeriodFormats.formatColons(this.match.runningTime()), this.match.isRunning() ? ChatColor.GREEN : ChatColor.GOLD));

        if(name != null) {
            content.extra(new Component(" - "),
                          new Component(name, ChatColor.WHITE, ChatColor.BOLD));
        }

        return content;
    }
}
