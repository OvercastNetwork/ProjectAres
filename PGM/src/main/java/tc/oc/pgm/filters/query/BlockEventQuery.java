package tc.oc.pgm.filters.query;

import java.util.Optional;
import javax.annotation.Nullable;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;

import static com.google.common.base.Preconditions.checkNotNull;

public class BlockEventQuery extends BlockQuery implements IBlockEventQuery {

    private final Event event;

    public BlockEventQuery(Event event, World world, int x, int y, int z, @Nullable BlockState block) {
        super(world, x, y, z, block);
        this.event = checkNotNull(event);
    }

    public BlockEventQuery(Event event, BlockState block) {
        this(event, block.getWorld(), block.getX(), block.getY(), block.getZ(), block);
    }

    public BlockEventQuery(Event event, Block block) {
        this(event, block.getWorld(), block.getX(), block.getY(), block.getZ(), null);
    }

    public static IBlockQuery of(BlockState block, Optional<? extends Event> event) {
        return event.<IBlockQuery>map(e -> new BlockEventQuery(e, block))
                    .orElseGet(() -> new BlockQuery(block));
    }

    @Override
    public Event getEvent() {
        return event;
    }
}
