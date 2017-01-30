package tc.oc.pgm.tnt.license;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.events.SingleMatchPlayerEvent;
import tc.oc.pgm.match.MatchPlayer;

public class LicenseRevokeEvent extends SingleMatchPlayerEvent {

    private final boolean hadLicense;

    public LicenseRevokeEvent(MatchPlayer player, boolean hadLicense) {
        super(player);
        this.hadLicense = hadLicense;
    }

    public boolean hadLicense() {
        return hadLicense;
    }

    private static final HandlerList handlers = new HandlerList();
}
