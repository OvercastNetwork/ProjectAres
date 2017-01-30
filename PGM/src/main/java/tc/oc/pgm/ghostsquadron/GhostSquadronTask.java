package tc.oc.pgm.ghostsquadron;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.UserId;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.classes.ClassMatchModule;

public class GhostSquadronTask implements Runnable {
    public GhostSquadronTask(Match match, GhostSquadronMatchModule matchModule, ClassMatchModule classMatchModule) {
        this.match = checkNotNull(match, "match");
        this.matchModule = checkNotNull(matchModule, "ghost squadron match module");
        this.classMatchModule = checkNotNull(classMatchModule, "class match module");
    }

    @Override
    public void run() {
        for(UserId userId : this.classMatchModule.getClassMembers(this.matchModule.trackerClass)) {
            MatchPlayer player = this.match.getPlayer(userId);
            if(player == null) continue;

            MatchPlayer closestEnemy = player;
            double closestRadiusSq = Double.MAX_VALUE;

            for(MatchPlayer enemy : this.matchModule.getMatch().getParticipatingPlayers()) {
                if(enemy.getParty() == player.getParty()) continue;

                double radiusSq = enemy.getBukkit().getLocation().distanceSquared(player.getBukkit().getLocation());
                if(radiusSq < closestRadiusSq) {
                    closestEnemy = enemy;
                    closestRadiusSq = radiusSq;
                }
            }

            player.getBukkit().setCompassTarget(closestEnemy.getBukkit().getLocation());
        }

        for(Map.Entry<Location, PlayerId> entry : this.matchModule.landmines.entrySet()) {
            MatchPlayer player = this.match.getPlayer(entry.getValue());
            Location loc = entry.getKey();
            if(player == null) continue;

            for(MatchPlayer enemy : this.match.getPlayers()) {
                enemy.getBukkit().playEffect(loc.clone().add(0, .7, 0), Effect.VILLAGER_THUNDERCLOUD, 0, 0, 0f, 0f, 0f, 0f, 1, 3);
            }
        }

        for(UserId userId : this.classMatchModule.getClassMembers(this.matchModule.spiderClass)) {
            MatchPlayer player = this.match.getPlayer(userId);
            if(player == null) continue;

            for(MatchPlayer enemy : this.matchModule.getMatch().getParticipatingPlayers()) {
                if(enemy.getParty() == player.getParty()) continue;
                if(enemy.getBukkit().getLocation().distanceSquared(player.getBukkit().getLocation()) < GhostSquadron.SPIDER_SENSE_RADIUS_SQ) {
                    this.matchModule.spideySense(player);
                    break;
                }
            }
        }
    }

    final Match match;
    final GhostSquadronMatchModule matchModule;
    final ClassMatchModule classMatchModule;
}
