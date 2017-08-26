package tc.oc.pgm.listeners;

import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.commons.bukkit.chat.HeaderComponent;
import tc.oc.commons.bukkit.chat.ListComponent;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.pgm.Config;
import tc.oc.pgm.events.MatchBeginEvent;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.PlayerJoinMatchEvent;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.map.Contributor;
import tc.oc.pgm.map.MapInfo;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchFormatter;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.mutation.Mutation;
import tc.oc.pgm.mutation.MutationMatchModule;
import tc.oc.pgm.quota.QuotaMatchModule;
import tc.oc.pgm.skillreq.SkillRequirementMatchModule;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.victory.VictoryMatchModule;

@Singleton
public class MatchAnnouncer implements PluginFacet, Listener {

    public static final Component GO = new Component(new TranslatableComponent("broadcast.go"), ChatColor.GREEN);

    private static final BukkitSound SOUND_MATCH_START = new BukkitSound(Sound.BLOCK_NOTE_PLING, 1f, 1.59f);
    private static final BukkitSound SOUND_MATCH_WIN = new BukkitSound(Sound.ENTITY_WITHER_DEATH, 1f, 1f);
    private static final BukkitSound SOUND_MATCH_LOSE = new BukkitSound(Sound.ENTITY_WITHER_SPAWN, 1f, 1f);

    private static final int CHAT_WIDTH = 200;
    private static final int TITLE_FADE = 5;
    private static final int TITLE_STAY = 100;
    private static final int MAX_TITLE_WINNERS = 3;

    private final MatchFormatter formatter;

    @Inject MatchAnnouncer(MatchFormatter formatter) {
        this.formatter = formatter;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchBegin(final MatchBeginEvent event) {
        Match match = event.getMatch();
        match.sendMessage(new Component(new TranslatableComponent("broadcast.matchStart"), ChatColor.GREEN));

        for(MatchPlayer player : match.getParticipatingPlayers()) {
            player.showTitle(GO, null, 0, 5, 15);
        }

        match.playSound(SOUND_MATCH_START);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMatchEnd(final MatchEndEvent event) {
        final VictoryMatchModule vmm = event.getMatch().needMatchModule(VictoryMatchModule.class);
        final Set<Competitor> winners = vmm.winners();
        final BaseComponent chat, title;

        if(!winners.isEmpty()) {
            final boolean plural = winners.size() > 1 || Iterables.getOnlyElement(winners).isNamePlural();
            chat = new TranslatableComponent(plural ? "broadcast.gameOver.teamWinText.plural"
                                                    : "broadcast.gameOver.teamWinText",
                                             new ListComponent(winners, party -> party.getStyledName(NameStyle.FANCY)));
            title = winners.size() <= MAX_TITLE_WINNERS ? chat
                                                              : new TranslatableComponent("broadcast.gameOver.multipleWinners",
                                                                                            new Component(winners.size(), ChatColor.AQUA));
        } else {
            chat = title = new TranslatableComponent("broadcast.gameOver.gameOverText");
        }

        event.getMatch().sendMessage(chat);

        for(MatchPlayer viewer : event.getMatch().getPlayers()) {
            BaseComponent subtitle = null;
            if(!winners.isEmpty()) {
                if(!viewer.isParticipatingType()) {
                    // Observer
                    viewer.playSound(SOUND_MATCH_WIN);
                } else if(winners.contains(viewer.getCompetitor())) {
                    // Winner
                    viewer.playSound(SOUND_MATCH_WIN);
                    if(viewer.getParty() instanceof Team) {
                        subtitle = new Component(new TranslatableComponent("broadcast.gameOver.teamWon"), ChatColor.GREEN);
                    }
                } else {
                    // Loser
                    viewer.playSound(SOUND_MATCH_LOSE);
                    if(viewer.getParty() instanceof Team) {
                        subtitle = new Component(new TranslatableComponent("broadcast.gameOver.teamLost"), ChatColor.RED);
                    }
                }
            }

            viewer.showTitle(title, subtitle, 0, 40, 40);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void join(PlayerJoinMatchEvent event) {
        event.getPlayer().getBukkit().hideTitle();

        final Match match = event.getMatch();
        final PlayerId viewerId = event.getPlayer().getPlayerId();

        match.getScheduler(MatchScope.LOADED).createDelayedTask(5L, () -> {
            final MatchPlayer viewer = match.getPlayer(viewerId);
            if(viewer == null) return;

            sendWelcomeMessage(viewer);
            match.module(SkillRequirementMatchModule.class).ifPresent(srmm -> srmm.sendFeedback(viewer));
            match.module(QuotaMatchModule.class).ifPresent(qmm -> qmm.sendQuotaInfo(viewer));
        });
    }

    public void sendWelcomeMessage(MatchPlayer viewer) {
        MapInfo mapInfo = viewer.getMatch().getMapInfo();
        final Component name = new Component(mapInfo.name, ChatColor.BOLD, ChatColor.AQUA);
        final Component objective = new Component(mapInfo.objective, ChatColor.BLUE, ChatColor.ITALIC);

        if(Config.Broadcast.title()) {
            viewer.getBukkit().showTitle(name, objective, TITLE_FADE, TITLE_STAY, TITLE_FADE);
        }

        viewer.sendMessage(new HeaderComponent(ChatColor.WHITE, CHAT_WIDTH, name));

        for(BaseComponent line : Components.wordWrap(objective, CHAT_WIDTH)) {
            viewer.sendMessage(line);
        }

        final List<Contributor> authors = mapInfo.getNamedAuthors();
        if(!authors.isEmpty()) {
            viewer.sendMessage(
                new Component(" ", ChatColor.DARK_GRAY).extra(
                    new TranslatableComponent(
                        "broadcast.welcomeMessage.createdBy",
                        new ListComponent(Lists.transform(authors, author -> author.getStyledName(NameStyle.MAPMAKER)))
                    )
                )
            );
        }

        final MutationMatchModule mmm = viewer.getMatch().getMatchModule(MutationMatchModule.class);
        if(mmm != null && mmm.mutationsActive().size() > 0) {
            viewer.sendMessage(
                new Component(" ", ChatColor.DARK_GRAY).extra(
                    new TranslatableComponent("broadcast.welcomeMessage.mutations",
                                              new ListComponent(Collections2.transform(mmm.mutationsActive(), Mutation.toComponent(ChatColor.GREEN)))
                    )
                )
            );
        }

        viewer.sendMessage(new HeaderComponent(ChatColor.WHITE, CHAT_WIDTH));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void matchInfoOnParticipate(final PlayerPartyChangeEvent event) {
        if(event.getNewParty() instanceof Competitor) {
            formatter.sendMatchInfo(event.getPlayer().getBukkit(), event.getMatch());
        }
    }
}
