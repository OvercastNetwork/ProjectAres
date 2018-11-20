package tc.oc.lobby.bukkit.gizmos.rocket;

import java.util.Iterator;
import org.bukkit.util.Vector;
import tc.oc.lobby.bukkit.gizmos.Gizmos;

public class RocketTask implements Runnable{
    @Override
    public void run() {
        for(Iterator<Rocket> it = Gizmos.rocketGizmo.rockets.iterator(); it.hasNext();) {
            Rocket rocket = it.next();

            if(rocket.allFireworksAlive()) {
                Vector center = rocket.getCenter();
                Vector delta = center.clone().subtract(rocket.getPreviousCenter());

                RocketUtils.fakeDelta(rocket.getObserver(), rocket.getVictim(), delta);
                rocket.setPreviousCenter(center);
            } else {
                rocket.getObserver().hidePlayer(rocket.getVictim());
                it.remove();
            }
        }
    }
}

