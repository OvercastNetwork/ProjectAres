package tc.oc.pgm.beacon;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.minecraft.protocol.MinecraftVersion;
import tc.oc.pgm.features.Feature;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;

public class Beacon implements Feature<BeaconDefinition> {

    private final BeaconDefinition data;
    private final Match match;

    public Beacon(Match match, BeaconDefinition data) {
        this.match = match;
        this.data = data;
    }

    @Repeatable(scope = MatchScope.LOADED)
    public void tick() {
        Object packet = NMSHacks.particlesPacket("ITEM_CRACK", true,
                this.data.location().clone().add(0, 56, 0),
                new Vector(0.15, 24, 0.15), // radius on each axis of the particle ball
                0f, // initial horizontal velocity
                data.particleCount(), // number of particles
                Material.WOOL.getId(), this.data.color().getWoolData());

        match.getPlayers().stream().filter(this::canSeeParticles).forEach((matchPlayer -> NMSHacks.sendPacket(matchPlayer.getBukkit(), packet)));

    }

    protected boolean canSeeParticles(MatchPlayer player) {
        return MinecraftVersion.atLeast(MinecraftVersion.MINECRAFT_1_8, player.getBukkit().getProtocolVersion()) &&
                data.visible().query(player).isAllowed();
    }

    @Override
    public BeaconDefinition getDefinition() {
        return data;
    }

}
