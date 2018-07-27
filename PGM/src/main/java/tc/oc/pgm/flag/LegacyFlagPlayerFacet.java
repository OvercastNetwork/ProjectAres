package tc.oc.pgm.flag;

import com.google.common.collect.ImmutableList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.item.ItemBuilder;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.flag.event.FlagStateChangeEvent;
import tc.oc.pgm.flag.state.Carried;
import tc.oc.pgm.flag.state.Spawned;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayerFacet;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Repeatable;
import tc.oc.time.Time;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.IntStream.range;
import static tc.oc.minecraft.protocol.MinecraftVersion.*;

@ListenerScope(MatchScope.LOADED)
public class LegacyFlagPlayerFacet implements MatchPlayerFacet, Listener {

    private final Match match;
    private final Player bukkit;
    private final Map<Flag, Beam> beams;

    @Inject LegacyFlagPlayerFacet(Match match, Player bukkit) {
        this.match = match;
        this.bukkit = bukkit;
        this.beams = new HashMap<>();
    }

    protected Stream<Flag> flags() {
        return (Stream<Flag>) match.features().all(Flag.class);
    }

    protected void trackFlag(Flag flag) {
        if(lessThan(MINECRAFT_1_8, bukkit.getProtocolVersion())) {
            beams.put(flag, beams.getOrDefault(flag, new Beam(flag)));

//            flag.getLocation().ifPresent(location -> bukkit.sendBlockChange(location, Material.AIR, (byte) 0));
        }
    }

    protected void untrackFlag(Flag flag) {
        if(beams.containsKey(flag)) {
            beams.remove(flag).hide();
        }
    }

    @Override
    public void enable() {
        flags().filter(flag -> flag.state() instanceof Spawned)
               .forEach(this::trackFlag);
    }

    @Override
    public void disable() {
        flags().forEach(this::untrackFlag);
        beams.clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFlagStateChange(FlagStateChangeEvent event) {
        Flag flag = event.getFlag();
        untrackFlag(flag);
        if(event.getNewState() instanceof Spawned) {
            trackFlag(flag);
        }
    }

    @Repeatable(interval = @Time(seconds = 1))
    public void onSecond() {
        ImmutableList.copyOf(beams.values()).forEach(Beam::update);
    }

    class Beam {

        final Flag flag;
        final List<NMSHacks.FakeZombie> segments;

        Beam(Flag flag) {
            this.flag = flag;
            this.segments = range(0, 32).mapToObj(i -> new NMSHacks.FakeZombie(match.getWorld(), true, true))
                                        .collect(Collectors.toImmutableList());
            show();
        }

        Optional<Player> carrier() {
            return Optional.ofNullable(flag.state() instanceof Carried ? ((Carried) flag.state()).getCarrier().getBukkit() : null);
        }

        Location location() {
            Location location = flag.getLocation().get().clone();
            location.setPitch(0);
            return location;
        }

        ItemStack wool() {
            return new ItemBuilder().material(Material.WOOL)
                    .enchant(Enchantment.DURABILITY, 1)
                    .color(flag.getDyeColor())
                    .get();
        }

        void show() {
            if(carrier().map(carrier -> carrier.equals(bukkit)).orElse(false)) return;
            segments.forEach(segment -> {
                segment.spawn(bukkit, location());
                segment.wear(bukkit, EquipmentSlot.HEAD, wool());
            });
            range(1, segments.size()).forEachOrdered(i -> {
                segments.get(i - 1).ride(bukkit, segments.get(i).entity());
            });
            update();
        }

        void update() {
            Optional<Player> carrier = carrier();
            NMSHacks.FakeZombie base = segments.get(0);
            if(carrier.isPresent()) {
                base.mount(bukkit, carrier.get());
            } else {
                base.teleport(bukkit, location());
            }
        }

        void hide() {
            for(int i = segments.size() - 1; i >= 0; i--) {
                segments.get(i).destroy(bukkit);
            }
        }

    }

}
