package tc.oc.pgm.blitz;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import com.google.common.collect.Maps;
import tc.oc.api.docs.PlayerId;

public class LifeManager {

    final int lives;
    final Map<PlayerId, Integer> livesLeft = Maps.newHashMap();

    public LifeManager(int lives) {
        checkArgument(lives > 0, "lives must be greater than zero");

        this.lives = lives;
    }

    public int getLives() {
        return this.lives;
    }

    public int getLives(PlayerId player) {
        checkNotNull(player, "player id");

        Integer livesLeft = this.livesLeft.get(player);
        if(livesLeft != null) {
            return livesLeft;
        } else {
            return this.lives;
        }
    }

    public int addLives(PlayerId player, int dlives) {
        checkNotNull(player, "player id");

        int lives = Math.max(0, this.getLives(player) + dlives);
        this.livesLeft.put(player, lives);

        return lives;
    }
}
