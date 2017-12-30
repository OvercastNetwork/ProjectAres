package tc.oc.pgm.control.point;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.util.BlockUtils;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.pgm.control.ControllableGoal;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.operator.AllFilter;
import tc.oc.pgm.filters.operator.InverseFilter;
import tc.oc.pgm.filters.query.BlockQuery;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.regions.FiniteBlockRegion;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.SectorRegion;
import tc.oc.pgm.renewable.BlockImage;

public class ControlPoint extends ControllableGoal<ControlPointDefinition> {

    protected final FiniteBlockRegion progressDisplayRegion;
    protected final BlockImage progressDisplayImage;
    protected final FiniteBlockRegion controllerDisplayRegion;
    protected final BlockImage controllerDisplayImage;

    public ControlPoint(Match match, ControlPointDefinition definition) {
        super(definition, match);
        Filter visualMaterials = definition.visualMaterials();
        Region progressDisplayRegion = definition.progressDisplayRegion();
        Region controllerDisplayRegion = definition.controllerDisplayRegion();
        final FiniteBlockRegion.Factory regionFactory = new FiniteBlockRegion.Factory(match.getMapInfo().proto);
        if(progressDisplayRegion == null) {
            this.progressDisplayRegion = null;
            this.progressDisplayImage = null;
        } else {
            this.progressDisplayRegion = regionFactory.fromWorld(progressDisplayRegion, match.getWorld(), visualMaterials);
            this.progressDisplayImage = new BlockImage(match.getWorld(), this.progressDisplayRegion.getBounds());
            this.progressDisplayImage.save();
        }
        if(controllerDisplayRegion == null) {
            this.controllerDisplayRegion = null;
            this.controllerDisplayImage = null;
        } else {
            this.controllerDisplayRegion = regionFactory.fromWorld(controllerDisplayRegion, match.getWorld(), this.progressDisplayRegion == null ? visualMaterials : AllFilter.of(visualMaterials, new InverseFilter(progressDisplayRegion)));
            this.controllerDisplayImage = new BlockImage(match.getWorld(), this.controllerDisplayRegion.getBounds());
            this.controllerDisplayImage.save();
        }
    }

    @Override
    public Region region() {
        return definition.captureRegion();
    }

    @Override
    protected boolean tracking(MatchPlayer player, Location location) {
        return region().contains(location);
    }

    @Override
    protected void displayProgress(Competitor controlling, Competitor capturing, double progress) {
        if(progressDisplayRegion != null) {
            Vector center = progressDisplayRegion.getBounds().center();
            // capturingProgress can be zero, but it can never be one, so invert it to avoid
            // a zero-area SectorRegion that can cause glitchy rendering
            SectorRegion sectorRegion = new SectorRegion(center.getX(), center.getZ(), 0, (1 - progress) * 2 * Math.PI);
            for(BlockVector pos : progressDisplayRegion.getBlockVectors()) {
                if(sectorRegion.contains(pos)) {
                    setBlock(pos, controlling);
                } else {
                    setBlock(pos, capturing);
                }
            }
        }
    }

    @Override
    protected void displaySet(Competitor owner) {
        if(controllerDisplayRegion != null) {
            if(owner == null) {
                for(BlockVector block : controllerDisplayRegion.getBlockVectors()) {
                    controllerDisplayImage.restore(block);
                }
            } else {
                byte blockData = BukkitUtils.chatColorToDyeColor(owner.getColor()).getWoolData();
                for(BlockVector pos : controllerDisplayRegion.getBlockVectors()) {
                    BlockUtils.blockAt(match.getWorld(), pos).setData(blockData);
                }
            }
        }
    }

    private void setBlock(BlockVector pos, Competitor team) {
        final Block block = BlockUtils.blockAt(match.getWorld(), pos);
        if(definition.visualMaterials().query(new BlockQuery(block)).isAllowed()) {
            if(team != null) {
                block.setData(BukkitUtils.chatColorToDyeColor(team.getColor()).getWoolData());
            } else {
                progressDisplayImage.restore(pos);
            }
        }
    }

}
