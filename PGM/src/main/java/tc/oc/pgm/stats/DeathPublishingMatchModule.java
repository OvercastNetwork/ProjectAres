package tc.oc.pgm.stats;

import java.util.Optional;
import javax.inject.Inject;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import java.time.Duration;
import java.time.Instant;

import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.DeathDoc;
import tc.oc.api.model.BatchUpdater;
import tc.oc.api.model.BatchUpdaterFactory;
import tc.oc.api.model.IdFactory;
import tc.oc.pgm.classes.ClassMatchModule;
import tc.oc.pgm.classes.PlayerClass;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.tracker.damage.BlockInfo;
import tc.oc.pgm.tracker.damage.DamageInfo;
import tc.oc.pgm.tracker.damage.EntityInfo;
import tc.oc.pgm.tracker.damage.FallInfo;
import tc.oc.pgm.tracker.damage.ItemInfo;
import tc.oc.pgm.tracker.damage.MeleeInfo;
import tc.oc.pgm.tracker.damage.ProjectileInfo;
import tc.oc.pgm.tracker.damage.RangedInfo;
import tc.oc.pgm.tracker.damage.SpleefInfo;
import tc.oc.pgm.tracker.damage.TNTInfo;
import tc.oc.pgm.tracker.damage.TrackerInfo;

@ListenerScope(MatchScope.LOADED)
public class DeathPublishingMatchModule extends MatchModule implements Listener {

    private static final Duration UPDATE_DELAY = Duration.ofMinutes(1);

    private final Server server;
    private final StatisticsConfiguration config;
    private final IdFactory idFactory;
    private final BatchUpdater<DeathDoc.Partial> batchUpdater;

    @Inject DeathPublishingMatchModule(Match match, StatisticsConfiguration config, Server server, BatchUpdaterFactory<DeathDoc.Partial> updaterFactory, IdFactory idFactory) {
        super(match);
        this.server = server;
        this.config = config;
        this.idFactory = idFactory;
        this.batchUpdater = updaterFactory.createBatchUpdater(UPDATE_DELAY);
    }

    @Override
    public void unload() {
        batchUpdater.flush();
        super.unload();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(MatchPlayerDeathEvent event) {
        final MatchPlayer victim = event.getVictim();
        final ParticipantState killer = event.getKiller();

        if(!victim.isParticipating() || !config.deaths()) return;

        final ClassMatchModule classes = match.getMatchModule(ClassMatchModule.class);
        final DamageInfo damage = event.getDamageInfo();
        final Location location = event.getVictim().getBukkit().getLocation();
        final String _id = idFactory.newId();

        batchUpdater.update(new DeathDoc.Creation() {
            @Override
            public String _id() {
                return _id;
            }

            @Override
            public String server_id() {
                return server._id();
            }

            @Override
            public String match_id() {
                return match.getId();
            }

            @Override
            public String family() {
                return server.family();
            }

            @Override
            public Instant date() {
                return match.getInstantNow();
            }

            @Override
            public String victim() {
                return victim.getPlayerId().player_id();
            }

            @Override
            public String killer() {
                return killer != null && !event.isSelfKill() ? killer.getPlayerId().player_id() : null;
            }

            @Override
            public String entity_killer() {
                return killer() == null && damage instanceof EntityInfo ? ((EntityInfo) damage).getIdentifier() : null;
            }

            @Override
            public String block_killer() {
                return killer() == null && damage instanceof BlockInfo ? ((BlockInfo) damage).getIdentifier() : null;
            }

            @Override
            public Boolean player_killer() {
                return killer() != null;
            }

            @Override
            public Boolean teamkill() {
                return killer() != null && !event.isSelfKill() && event.isTeamKill();
            }

            @Override
            public String victim_class() {
                return Optional.ofNullable(classes)
                               .flatMap(cmm -> cmm.lastPlayedClass(victim.getPlayerId()))
                               .map(PlayerClass::getName)
                               .orElse(null);
            }

            @Override
            public String killer_class() {
                return Optional.ofNullable(classes)
                               .filter(cmm -> killer != null)
                               .flatMap(cmm -> cmm.lastPlayedClass(killer.getPlayerId()))
                               .map(PlayerClass::getName)
                               .orElse(null);
            }

            @Override
            public int raindrops() {
                return event.getRaindrops();
            }

            @Override
            public double x() {
                return location.getX();
            }

            @Override
            public double y() {
                return location.getY();
            }

            @Override
            public double z() {
                return location.getZ();
            }

            @Override
            public Double distance() {
                if(damage instanceof FallInfo) {
                    TrackerInfo cause = ((FallInfo) damage).getCause();
                    Location location = event.getVictim().getBukkit().getLocation();
                    RangedInfo rangedInfo = cause instanceof RangedInfo ? (RangedInfo) damage : (FallInfo) damage;
                    return rangedInfo.distanceFrom(location);
                }
                return null;
            }

            @Override
            public Boolean enchanted() {
                return weapon() != null && damage instanceof MeleeInfo ? ((MeleeInfo) damage).getWeapon().isEnchanted() : null;
            }

            @Override
            public String weapon() {
                final ItemInfo info = damage instanceof MeleeInfo ? ((MeleeInfo) damage).getWeapon() : null;
                if(info != null && info.getItem().getType() != Material.AIR) {
                    return info.getIdentifier();
                }
                return null;
            }

            @Override
            public String from() {
                return damage instanceof FallInfo ? ((FallInfo) damage).getFrom().toString() : null;
            }

            @Override
            public String action() {
                if(damage instanceof FallInfo) {
                    TrackerInfo cause = ((FallInfo) damage).getCause();
                    if(cause instanceof ProjectileInfo) {
                        return "SHOOT";
                    } else if(cause instanceof SpleefInfo) {
                        return "SPLEEF";
                    } else if(cause instanceof TNTInfo) {
                        return "EXPLODE";
                    } else {
                        return "HIT";
                    }
                }
                return null;
            }

            @Override
            public String cause() {
                if(damage instanceof MeleeInfo) {
                    return "MELEE";
                } else if(damage instanceof ProjectileInfo) {
                    if(((ProjectileInfo) damage).getProjectile() instanceof EntityInfo) {
                        switch(((EntityInfo)((ProjectileInfo) damage).getProjectile()).getEntityType()) {
                            case ARROW:
                            case SPECTRAL_ARROW:
                            case TIPPED_ARROW:
                                return "ARROW";
                            case SPLASH_POTION:
                            case LINGERING_POTION:
                                return "POTION";
                        }
                    }
                }
                return null;
            }
        });
    }

}
