package tc.oc.pgm.gamerules;

import java.util.Map;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.Repeatable;
import tc.oc.time.Time;

public class GameRulesMatchModule extends MatchModule {

    private final Map<String, String> gameRules;

    public GameRulesMatchModule(Match match, Map<String, String> gameRules) {
        super(match);
        this.gameRules = Preconditions.checkNotNull(gameRules, "gamerules");
    }

    @Override
    public void load() {
        update();
    }

    @Repeatable(interval = @Time(seconds = 1))
    public void tick() {
        update();
    }

    public void update() {
        gameRulesImmutable().forEach((String rule, String val) -> match.getWorld().setGameRuleValue(rule, val));
    }

    public Map<String, String> gameRules() {
        return gameRules;
    }

    public ImmutableMap<String, String> gameRulesImmutable() {
        return ImmutableMap.copyOf(gameRules());
    }

}
