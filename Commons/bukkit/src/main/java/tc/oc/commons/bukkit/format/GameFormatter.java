package tc.oc.commons.bukkit.format;

import java.util.Collection;
import java.util.Optional;
import javax.inject.Inject;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.api.docs.Arena;
import tc.oc.api.docs.Game;
import tc.oc.api.docs.Server;
import tc.oc.api.servers.ServerStore;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.commons.bukkit.localization.Translations;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;

public class GameFormatter {

    public static final ChatColor MESSAGE_COLOR = ChatColor.YELLOW;
    public static final ChatColor NAME_COLOR = ChatColor.AQUA;

    private @Inject Translations translations;
    private @Inject ServerStore servers;
    private @Inject Server localServer;
    private @Inject MiscFormatter miscFormatter;

    public static class Dark extends GameFormatter {
        @Override
        protected ChatColor countColor() {
            return ChatColor.DARK_AQUA;
        }

        @Override
        protected ChatColor slashColor() {
            return ChatColor.GRAY;
        }

        @Override
        protected ChatColor limitColor() {
            return ChatColor.DARK_GRAY;
        }
    }

    protected ChatColor countColor() {
        return ChatColor.WHITE;
    }

    protected ChatColor slashColor() {
        return ChatColor.DARK_GRAY;
    }

    protected ChatColor limitColor() {
        return ChatColor.GRAY;
    }

    public BaseComponent play(Game game) {
        return new TranslatableComponent("game.play", name(game, false));
    }

    public BaseComponent replay(Game game) {
        return new TranslatableComponent("game.replay", name(game, false));
    }

    public BaseComponent replayMaybe(Game game) {
        return new Component(
            new TranslatableComponent(
                "game.replayMaybe",
                new Component("/replay", NAME_COLOR)
                    .extra(" ")
                    .extra(name(game, false))
            ), ChatColor.GREEN
        );
    }

    public BaseComponent leave(Game game) {
        return new TranslatableComponent("game.leave", name(game, false));
    }

    public BaseComponent name(Game game) {
        return name(game, true);
    }

    public BaseComponent name(Game game, boolean clickable) {
        final Component c = new Component(game.name(), NAME_COLOR);
        if(!clickable) return c;

        return new Component(ChatColor.WHITE)
            .extra("[")
            .extra(c.clickEvent(ClickEvent.Action.RUN_COMMAND, "/play " + game.name())
                    .hoverEvent(HoverEvent.Action.SHOW_TEXT, play(game)))
            .extra("]");
    }

    public static String descriptionKey(Game game) {
        return "game.description." + game._id();
    }

    public BaseComponent description(Game game) {
        final String key = descriptionKey(game);
        return translations.hasKey(key) ? new Component(new TranslatableComponent(key), ChatColor.DARK_AQUA)
                                        : Components.blank();
    }

    public BaseComponent joining(Game game) {
        return new Component(
            new TranslatableComponent("game.joining", name(game)),
            MESSAGE_COLOR
        );
    }

    public BaseComponent rejoining(Game game) {
        return new Component(
            new TranslatableComponent("game.rejoining", name(game)),
            MESSAGE_COLOR
        );
    }

    public BaseComponent cannotJoin(Game game) {
        return new WarningComponent("game.cannotJoin", name(game));
    }

    public BaseComponent queued(Game game, int playersNeeded) {
        return new Component(
            new TranslatableComponent("game.waitingForPlayers", new Component(playersNeeded, ChatColor.AQUA), name(game, false)),
            MESSAGE_COLOR
        );
    }

    public BaseComponent left(Game game) {
        return new Component(
            new TranslatableComponent("game.left", name(game)),
            MESSAGE_COLOR
        );
    }

    public BaseComponent notPlaying() {
        return new WarningComponent("game.notPlaying");
    }

    public BaseComponent alreadyPlaying(Game game) {
        return new WarningComponent("game.alreadyPlaying", name(game));
    }

    public Optional<Integer> minimumPlayers(Arena arena) {
        return arena.next_server_id() == null ? Optional.empty()
                                              : Optional.of(servers.byId(arena.next_server_id()).min_players());
    }

    public int countObservers(Arena arena) {
        return servers.byArena(arena)
                      .stream()
                      .mapToInt(Server::num_observing)
                      .sum();
    }

    public BaseComponent countAndMax(int count, int max) {
        return max < 0 ? new Component(count, countColor())
                       : new Component(new Component(count, countColor()),
                                       new Component("/", slashColor()),
                                       new Component(max, limitColor()));
    }

    public BaseComponent onlineCount(int count) {
        return new Component(new TranslatableComponent("game.numOnline", new Component(count, ChatColor.WHITE)),
                             ChatColor.BLUE);
    }

    public BaseComponent onlineCount(Server server) {
        return onlineCount(server.num_online());
    }

    public BaseComponent onlineCount(Arena arena) {
        return onlineCount(arena.num_playing() + arena.num_queued());
    }

    public BaseComponent playingCount(int count, int max) {
        return new Component(new TranslatableComponent("game.numPlaying", countAndMax(count, max)),
                             ChatColor.DARK_GREEN);
    }

    public BaseComponent playingCount(Arena arena) {
        return playingCount(arena.num_playing(), -1);
    }

    public BaseComponent playingCount(Server server) {
        return playingCount(server.num_participating(), server.max_players());
    }

    public BaseComponent watchingCount(int count) {
        return new Component(new TranslatableComponent("game.numWatching", new Component(count, ChatColor.WHITE)),
                             ChatColor.BLUE);
    }

    public BaseComponent watchingCount(Server server) {
        return watchingCount(server.num_observing());
    }

    public BaseComponent watchingCount(Arena arena) {
        return watchingCount(countObservers(arena));
    }

    public BaseComponent waitingCount(Arena arena) {
        return new Component(new TranslatableComponent("game.numQueued", countAndMax(arena.num_queued(), minimumPlayers(arena).orElse(-1))),
                             ChatColor.DARK_PURPLE);
    }

    public void sendList(Audience audience, Collection<Game> games) {
        if(games.isEmpty()) {
            audience.sendMessage(new WarningComponent("game.none"));
            return;
        }

        audience.sendMessage(
            new Component(
                new TranslatableComponent(
                    "game.choose",
                    new Component("/play <game>", ChatColor.GOLD),
                    new Component("/watch <game>", ChatColor.GOLD)
                ),
                MESSAGE_COLOR
            )
        );

        for(Game game : games) {
            audience.sendMessage(
                new Component(" ")
                    .extra(name(game))
                    .extra(" ")
                    .extra(description(game))
            );
        }
    }
}
