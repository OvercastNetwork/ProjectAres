package tc.oc.pgm.blitz;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.match.Competitor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public abstract class LivesBase implements Lives {

    private final Map<PlayerId, Integer> deltas;
    private final Competitor competitor;
    private final int original;
    private int current;

    public LivesBase(int lives, Competitor competitor) {
        this.deltas = new HashMap<>();
        this.competitor = competitor;
        this.original = lives;
        this.current = lives;
        update();
    }

    protected void update() {
        competitor().getMatch().callEvent(new LivesEvent(this));
    }

    @Override
    public Competitor competitor() {
        return competitor;
    }

    @Override
    public int original() {
        return original;
    }

    @Override
    public int current() {
        return current;
    }

    @Override
    public boolean empty() {
        return current() <= 0;
    }

    @Override
    public void add(@Nullable PlayerId cause, int delta) {
        current = Math.max(0, current() + delta);
        deltas.put(cause, changesBy(cause) + delta);
        update();
    }

    @Override
    public int changesBy(PlayerId player) {
        return deltas.getOrDefault(player, 0);
    }

    @Override
    public BaseComponent remaining() {
        return new Component(
            new TranslatableComponent(
                "lives.remaining." + type().name().toLowerCase() + "." + (current() == 1 ? "singular"
                                                                                         : "plural"),
                new Component(current(), ChatColor.YELLOW)
            ),
            ChatColor.AQUA
        );
    }

    @Override
    public BaseComponent status() {
        int alive = (int) competitor().players().count();
        return new Component(
            Stream.of(
                new Component("("),
                new TranslatableComponent(
                    empty() ? alive == 0 ? "lives.status.eliminated"
                                         : "lives.status.alive"
                            : "lives.status.lives",
                    new Component(
                        empty() ? alive : current(),
                        ChatColor.WHITE
                    )
                ),
                new Component(")")
            ),
            ChatColor.GRAY,
            ChatColor.ITALIC
        );
    }

    @Override
    public BaseComponent change(int delta) {
        int absDelta = Math.abs(delta);
        return new Component(
            new TranslatableComponent(
                (delta > 0 ? "lives.change.gained."
                           : "lives.change.lost.") + (absDelta == 1 ? "singular"
                                                                    : "plural"),
                new Component(absDelta, ChatColor.AQUA)
            ),
            ChatColor.WHITE
        );
    }

}
