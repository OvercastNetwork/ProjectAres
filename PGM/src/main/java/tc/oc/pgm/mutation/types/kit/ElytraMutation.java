package tc.oc.pgm.mutation.types.kit;

import org.bukkit.Material;
import tc.oc.commons.bukkit.inventory.ArmorType;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.pgm.doublejump.DoubleJumpKit;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.kits.ItemKitApplicator;
import tc.oc.pgm.kits.SlotItemKit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.mutation.types.KitMutation;

import java.time.Duration;
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
        // If the player is mid-air, give them a totem so they don't fall and die
        if(player.getBukkit().isGliding()) {
            ItemKitApplicator applicator = new ItemKitApplicator();
            applicator.put(Slot.OffHand.offHand(), item(Material.TOTEM), false);
            applicator.apply(player);
        }
        super.remove(player);
    }

}
