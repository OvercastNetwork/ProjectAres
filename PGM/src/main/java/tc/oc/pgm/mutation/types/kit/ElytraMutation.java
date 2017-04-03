package tc.oc.pgm.mutation.types.kit;

import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.commons.bukkit.inventory.ArmorType;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.collection.WeakHashSet;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.doublejump.DoubleJumpKit;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.kits.KitNode;
import tc.oc.pgm.kits.KitPlayerFacet;
import tc.oc.pgm.kits.RemoveKit;
import tc.oc.pgm.kits.SlotItemKit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.mutation.Mutation;
import tc.oc.pgm.mutation.MutationMatchModule;
import tc.oc.pgm.mutation.types.KitMutation;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElytraMutation extends KitMutation {

    final static ItemKit ELYTRA = new SlotItemKit(item(Material.ELYTRA), Slot.Armor.forType(ArmorType.CHESTPLATE));
    final static DoubleJumpKit JUMP = new DoubleJumpKit(true, 6f, Duration.ofSeconds(30), true);

    public ElytraMutation(Match match) {
        super(match, true, ELYTRA, JUMP);
    }

    @Override
    public Stream<? extends Slot> saved() {
        return Stream.of(Slot.Armor.forType(ArmorType.CHESTPLATE));
    }

    @Override
    public void remove(MatchPlayer player) {
        // Anyone left gliding will be taken care of by the ground stop order
        if(!player.getBukkit().isGliding()) {
            super.remove(player);
        }
    }

    @Override
    public void disable() {
        new GroundStop(match()).run();
        super.disable();
    }

    /**
     * A cleanup task to slowly remove elytras from players.
     *
     * This prevents players that are mid-glide from falling
     * out of the sky and gives them time to land.
     */
    private class GroundStop implements Runnable {

        Match match;
        WeakHashSet<MatchPlayer> gliding;
        Instant end;

        GroundStop(Match match) {
            this.match = match;
            this.gliding = new WeakHashSet<>(match.participants().filter(player -> player.getBukkit().isGliding()).collect(Collectors.toSet()));
            this.end = match.getInstantNow().plus(Duration.ofSeconds(10));
        }

        @Override
        public void run() {
            if(match.isRunning() && !gliding.isEmpty() && !match.module(MutationMatchModule.class).get().enabled(Mutation.ELYTRA)) {
                Instant now = match().getInstantNow();
                for(MatchPlayer player : ImmutableSet.copyOf(gliding)) {
                    if(!player.isSpawned() || !player.getBukkit().isGliding() || TimeUtils.isEqualOrBeforeNow(now, end)) {
                        gliding.remove(player);
                        player.facet(KitPlayerFacet.class).applyKit(KitNode.of(new RemoveKit(ELYTRA), new RemoveKit(JUMP)), true);
                        player.sendHotbarMessage(Components.blank());
                    } else {
                        long seconds = Duration.between(now, end).getSeconds();
                        player.sendHotbarMessage(new WarningComponent("mutation.type.elytra.land", new Component(seconds, ChatColor.YELLOW)));
                    }
                }
                match.getScheduler(MatchScope.RUNNING).createDelayedTask(Duration.ofMillis(50), this::run);
            } else {
                gliding.clear();
                match = null;
            }
        }

    }

}
