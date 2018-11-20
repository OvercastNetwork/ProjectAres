package tc.oc.lobby.bukkit.gizmos.rocket;

import com.google.common.collect.Lists;
import java.util.List;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Rocket {
    public Rocket(Player observer, Player victim, List<Firework> fireworks) {
        this.observer = observer;
        this.victim = victim;
        this.fireworks = Lists.newArrayList(fireworks);

        this.previousCenter = this.getCenter();
    }

    public Player getObserver() {
        return this.observer;
    }

    public Player getVictim() {
        return this.victim;
    }

    public List<Firework> getFireworks() {
        return this.fireworks;
    }

    public boolean allFireworksAlive() {
        for(Firework firework : this.fireworks) {
            if(firework.isDead()) return false;
        }

        return true;
    }

    public Vector getCenter() {
        int num = this.fireworks.size();
        double totalX = 0;
        double totalY = 0;
        double totalZ = 0;

        for(Firework firework : this.fireworks) {
            totalX += firework.getLocation().getX();
            totalY += firework.getLocation().getY();
            totalZ += firework.getLocation().getZ();
        }

        return new Vector(totalX / num, totalY / num, totalZ / num);
    }

    public Vector getPreviousCenter() {
        return this.previousCenter.clone();
    }

    public void setPreviousCenter(Vector previousCenter) {
        this.previousCenter = previousCenter;
    }

    final Player observer;
    final Player victim;
    final List<Firework> fireworks;
    Vector previousCenter;
}
