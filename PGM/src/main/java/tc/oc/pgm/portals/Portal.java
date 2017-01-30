package tc.oc.pgm.portals;

import org.bukkit.EntityLocation;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.FilterMatchModule;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;

@FeatureInfo(name = "portal")
public interface Portal extends FeatureDefinition {}

class PortalImpl extends FeatureDefinition.Impl implements Portal {
    private static final BukkitSound USE_SOUND = new BukkitSound(Sound.ENTITY_ENDERMEN_TELEPORT, 1f, 1f);

    private final @Inspect Filter trigger;
    private final @Inspect Filter participantFilter, observerFilter;
    private final @Inspect PortalTransform transform;
    private final @Inspect boolean sound;
    private final @Inspect boolean smooth;

    public PortalImpl(Filter trigger,
                      PortalTransform transform,
                      Filter participantFilter,
                      Filter observerFilter,
                      boolean sound,
                      boolean smooth) {

        this.transform = transform;
        this.trigger = trigger;
        this.participantFilter = participantFilter;
        this.observerFilter = observerFilter;
        this.sound = sound;
        this.smooth = smooth;
    }

    private boolean canUse(MatchPlayer player) {
        return (player.isParticipating() ? participantFilter : observerFilter).query(player).isAllowed();
    }

    @Override
    public void load(Match match) {
        final FilterMatchModule fmm = match.needMatchModule(FilterMatchModule.class);
        fmm.onRise(MatchPlayer.class, trigger, player -> {
            if(canUse(player) && !player.facet(PortalPlayerFacet.class).teleport()) {
                teleportPlayer(player, player.getBukkit().getEntityLocation());
            }
        });
    }

    private void teleportPlayer(final MatchPlayer player, final EntityLocation from) {
        final EntityLocation to = transform.apply(from);
        final Player bukkit = player.getBukkit();
        final Match match = player.getMatch();

        if(sound) {
            // Don't play the sound for the teleporting player at the entering portal,
            // because they will instantly teleport away and hear the one at the exit.
            for(MatchPlayer listener : match.getPlayers()) {
                if(!player.equals(listener) && listener.getBukkit().canSee(player.getBukkit())) {
                    listener.playSound(USE_SOUND, from);
                }
            }
        }

        // Use ENDER_PEARL as the cause so that this teleport is treated
        // as an "in-game" movement
        if(smooth) {
            bukkit.teleportRelative(to.toVector().subtract(from.toVector()),
                                    to.getYaw() - from.getYaw(),
                                    to.getPitch() - from.getPitch(),
                                    PlayerTeleportEvent.TeleportCause.ENDER_PEARL);
        } else {
            bukkit.teleport(to, PlayerTeleportEvent.TeleportCause.ENDER_PEARL);
        }
        // Reset fall distance
        bukkit.setFallDistance(0);

        if(sound) {
            for(MatchPlayer listener : match.getPlayers()) {
                if(listener.getBukkit().canSee(player.getBukkit())) {
                    listener.playSound(USE_SOUND, to);
                }
            }
        }
    }
}
