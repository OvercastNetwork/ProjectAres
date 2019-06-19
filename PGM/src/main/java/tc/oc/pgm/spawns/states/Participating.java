package tc.oc.pgm.spawns.states;

import org.bukkit.permissions.PermissionAttachment;
import tc.oc.api.util.Permissions;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.match.MatchPlayer;

public class Participating extends State {

    private PermissionAttachment permissionAttachment;

    public Participating(MatchPlayer player) {
        super(player);
    }

    @Override
    public void enterState() {
        super.enterState();
        bukkit.setGravity(true);
        permissionAttachment = bukkit.addAttachment(smm.getMatch().getPlugin(), Permissions.PARTICIPANT, true);
    }

    @Override
    public void leaveState() {
        if(permissionAttachment != null) bukkit.removeAttachment(permissionAttachment);
        super.leaveState();
    }

    @Override
    public void onEvent(MatchEndEvent event) {
        super.onEvent(event);
        transition(new Observing(player, true, false));
    }
}
