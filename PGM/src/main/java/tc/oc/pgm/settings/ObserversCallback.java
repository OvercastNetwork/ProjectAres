package tc.oc.pgm.settings;

import javax.inject.Inject;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.bukkit.PlayerSettingCallback;
import org.bukkit.entity.Player;
import tc.oc.pgm.match.MatchFinder;
import tc.oc.pgm.match.MatchPlayer;

public class ObserversCallback extends PlayerSettingCallback {

    private final MatchFinder finder;

    @Inject private ObserversCallback(MatchFinder finder) {
        this.finder = finder;
    }

    @Override
    public void notifyChange(Player bukkit, Setting setting, Object oldValue, Object newValue) {
        yield();
        finder.player(bukkit).ifPresent(MatchPlayer::refreshVisibility);
    }
}
