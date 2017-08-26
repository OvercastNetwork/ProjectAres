package tc.oc.pgm.spawns.states;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.MatchPlayer;

/**
 * Player is waiting to spawn after joining a team
 */
public class Joining extends Spawning {

    public Joining(MatchPlayer player) {
        super(player);
        this.spawnRequested = true;
    }

    @Override
    public void enterState() {
        player.setVisible(false);
        bukkit.setGravity(true);

        super.enterState();
        trySpawn();
    }

    @Override
    protected BaseComponent getTitle() {
        return new Component();
    }

    @Override
    public void onEvent(PlayerChangePartyEvent event) {
        super.onEvent(event);
        if(!(event.getNewParty() instanceof Competitor)) {
            transition(new Observing(player, false, false));
        }
    }
}
