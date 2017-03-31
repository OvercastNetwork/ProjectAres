package tc.oc.pgm.spawns.states;

import javax.annotation.Nullable;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tc.oc.commons.bukkit.freeze.FrozenPlayer;
import tc.oc.pgm.classes.ClassMatchModule;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.itemkeep.ItemKeepPlayerFacet;
import tc.oc.pgm.killreward.KillRewardMatchModule;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.kits.KitPlayerFacet;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.mutation.MutationMatchModule;
import tc.oc.pgm.mutation.types.KitMutation;
import tc.oc.pgm.spawns.Spawn;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;
import tc.oc.pgm.spawns.events.ParticipantReleaseEvent;
import tc.oc.pgm.spawns.events.ParticipantSpawnEvent;

/**
 * Player is alive and participating
 */
public class Alive extends Participating {

    protected final Spawn spawn;
    protected final Location location;
    private @Nullable FrozenPlayer frozenPlayer;

    public Alive(MatchPlayer player, Spawn spawn, Location location) {
        super(player);
        this.spawn = spawn;
        this.location = location;
    }

    @Override
    public void enterState() {
        super.enterState();

        player.reset();
        player.setSpawned(true);
        player.refreshInteraction();

        // Fire Bukkit's event
        PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(player.getBukkit(), location, false);
        player.getMatch().callEvent(respawnEvent);

        // Fire our event
        ParticipantSpawnEvent spawnEvent = new ParticipantSpawnEvent(player, respawnEvent.getRespawnLocation());
        player.getMatch().callEvent(spawnEvent);

        // Teleport the player
        player.getBukkit().teleport(spawnEvent.getLocation());

        // Return kept items
        // TODO: Module should do this itself, maybe from ParticipantSpawnEvent
        player.facetMaybe(ItemKeepPlayerFacet.class)
              .ifPresent(ItemKeepPlayerFacet::restoreKeptInventory);

        player.setVisible(true);
        player.refreshVisibility();
        bukkit.setGameMode(GameMode.SURVIVAL);

        // Apply spawn kit
        for(Kit kit : smm.getPlayerKits()) {
            player.facet(KitPlayerFacet.class).applyKit(kit, false);
        }
        spawn.applyKit(player);

        // Apply class kit(s)
        // TODO: Module should do this itself, maybe from ParticipantSpawnEvent
        match.module(ClassMatchModule.class).ifPresent(cmm -> cmm.giveClassKits(player));

        // Give kill rewards earned while dead
        // TODO: Module should do this itself, maybe from ParticipantSpawnEvent
        match.module(KillRewardMatchModule.class).ifPresent(krmm -> krmm.giveDeadPlayerRewards(player));

        // Apply kit injections from KitMutationModules
        match.module(MutationMatchModule.class)
             .ifPresent(mmm -> mmm.mutationModules().stream()
                                                    .filter(mm -> mm instanceof KitMutation)
                                                    .forEach(mm -> ((KitMutation) mm).apply(player)));

        player.getBukkit().updateInventory();

        if(match.hasStarted()) {
            match.callEvent(new ParticipantReleaseEvent(player, false));
        } else {
            frozenPlayer = freezer.freeze(bukkit);
        }
    }

    private boolean thaw() {
        if(frozenPlayer != null) {
            frozenPlayer.thaw();
            frozenPlayer = null;
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if(match.hasStarted()) {
            if(thaw()) {
                match.callEvent(new ParticipantReleaseEvent(player, true));
            }
        } else if(!canSpawn()) {
            transition(new Observing(player, true, false));
        }
    }

    @Override
    public void leaveState() {
        match.callEvent(new ParticipantDespawnEvent(player, player.getBukkit().getLocation()));
        thaw();
        super.leaveState();
    }

    @Override
    public void onEvent(PlayerChangePartyEvent event) {
        super.onEvent(event);

        if(event.getNewParty() instanceof Competitor) {
            transition(new Joining(player));
        } else {
            transition(new Observing(player, true, true));
        }
    }

    @Override
    public void onEvent(PlayerDeathEvent event) {
        // Prevent default death, but allow item drops
        forceAlive();
    }

    @Override
    public void onEvent(MatchPlayerDeathEvent event) {
        if(!event.isPredicted()) {
            die(event.getKiller());
        }
    }

    public void die(@Nullable ParticipantState killer) {
        player.setDead(true);

        // Setting a player's gamemode resets their fall distance.
        // We need the fall distance for the death message.
        // We set the fall distance back to 0 when we refresh the player.
        float fallDistance = bukkit.getFallDistance();
        bukkit.setGameMode(GameMode.CREATIVE);
        bukkit.setFallDistance(fallDistance);

        playDeathEffect(killer);

        transition(new Dead(player));
    }

    private void playDeathEffect(@Nullable ParticipantState killer) {
        playDeathSound(killer);

        // negative health boost potions sometimes change max health
        for(PotionEffect effect : bukkit.getActivePotionEffects()) {
            // Keep speed and NV for visual continuity
            if(effect.getType() != null &&
               !PotionEffectType.NIGHT_VISION.equals(effect.getType()) &&
               !PotionEffectType.SPEED.equals(effect.getType())) {

                bukkit.removePotionEffect(effect.getType());
            }
        }
    }

    private void playDeathSound(@Nullable ParticipantState killer) {
        Location death = player.getBukkit().getLocation();

        for(MatchPlayer listener : player.getMatch().getPlayers()) {
            if(listener == player) {
                // Own death is normal pitch, full volume
                listener.playSound(Sound.ENTITY_IRONGOLEM_DEATH);
            } else if(killer != null && killer.isPlayer(listener)) {
                // Kill is higher pitch, quieter
                listener.playSound(Sound.ENTITY_IRONGOLEM_DEATH, 0.75f, 4f / 3f);
            } else if(listener.getParty() == player.getParty()) {
                // Ally death is a shorter sound
                listener.playSound(Sound.ENTITY_IRONGOLEM_HURT, death);
            } else {
                // Enemy death is higher pitch
                listener.playSound(Sound.ENTITY_IRONGOLEM_HURT, death, 1, 4f / 3f);
            }
        }
    }
}
