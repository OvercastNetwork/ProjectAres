package tc.oc.pgm.filters.matcher.player;

import com.google.common.base.Preconditions;
import tc.oc.commons.core.util.Optionals;
import tc.oc.pgm.classes.ClassMatchModule;
import tc.oc.pgm.classes.PlayerClass;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IPlayerQuery;

public class PlayerClassFilter extends TypedFilter.Impl<IPlayerQuery> {
    private final @Inspect PlayerClass playerClass;

    public PlayerClassFilter(PlayerClass playerClass) {
        this.playerClass = Preconditions.checkNotNull(playerClass, "player class");
    }

    @Override
    public boolean matches(IPlayerQuery query) {
        return Optionals.flatMapBoth(query.module(ClassMatchModule.class),
                                     query.onlinePlayer(),
                                     (cmm, player) -> cmm.lastPlayedClass(player.getPlayerId()))
                        .filter(playerClass::equals)
                        .isPresent();
    }
}
