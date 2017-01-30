package tc.oc.pgm.bossbar;

import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Combined text and meter progress for display in the boss bar
 */
public interface BossBarContent {
    BaseComponent text();
    float progress();

    static BossBarContent of(BaseComponent text, float progress) {
        return new BossBarContent() {
            @Override public BaseComponent text() { return text; }
            @Override public float progress() { return progress; }
        };
    }
}
