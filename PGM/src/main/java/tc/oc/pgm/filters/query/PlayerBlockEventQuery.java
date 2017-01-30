package tc.oc.pgm.filters.query;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.material.MaterialData;

import static com.google.common.base.Preconditions.checkNotNull;

public class PlayerBlockEventQuery extends PlayerEventQuery implements IPlayerBlockEventQuery {

    private final BlockState block;

    public PlayerBlockEventQuery(IPlayerQuery player, Event event, BlockState block) {
        super(player, event);
        this.block = checkNotNull(block);
    }

    public static IBlockQuery of(BlockState block, Optional<? extends Event> event, Optional<? extends IPlayerQuery> player) {
        return player.<IBlockQuery>map(p -> new PlayerBlockEventQuery(p, event.get(), block))
                     .orElseGet(() -> BlockEventQuery.of(block, event));
    }

    public static IBlockEventQuery of(BlockState block, Event event, Optional<? extends IPlayerQuery> player) {
        return player.<IBlockEventQuery>map(p -> new PlayerBlockEventQuery(p, event, block))
                     .orElseGet(() -> new BlockEventQuery(event, block));
    }

    @Override
    public BlockState getBlock() {
        return block;
    }

    @Override
    public Location getLocation() {
        return block.getLocation();
    }

    @Override
    public MaterialData getMaterial() {
        return block.getMaterialData();
    }

    @Override
    public int randomSeed() {
        return IPlayerBlockEventQuery.super.randomSeed();
    }
}
