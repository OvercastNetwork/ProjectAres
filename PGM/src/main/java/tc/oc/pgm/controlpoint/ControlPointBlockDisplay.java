package tc.oc.pgm.controlpoint;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.util.BlockUtils;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.filters.operator.AllFilter;
import tc.oc.pgm.filters.operator.InverseFilter;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.filters.query.BlockQuery;
import tc.oc.pgm.controlpoint.events.CapturingTimeChangeEvent;
import tc.oc.pgm.controlpoint.events.ControllerChangeEvent;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.regions.FiniteBlockRegion;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.SectorRegion;
import tc.oc.pgm.renewable.BlockImage;

import java.util.Objects;

/**
 * Displays the status of a ControlPoint by coloring blocks in specified regions
 */
@ListenerScope(MatchScope.LOADED)
public class ControlPointBlockDisplay implements Listener {
    protected final Match match;
    protected final ControlPoint controlPoint;

    protected final FiniteBlockRegion progressDisplayRegion;
    protected final BlockImage progressDisplayImage;
    protected final FiniteBlockRegion controllerDisplayRegion;
    protected final BlockImage controllerDisplayImage;

    protected Competitor controllingTeam;

    public ControlPointBlockDisplay(Match match, ControlPoint controlPoint) {
        this.match = match;
        this.controlPoint = controlPoint;

        Filter visualMaterials = controlPoint.getDefinition().getVisualMaterials();
        Region progressDisplayRegion = controlPoint.getDefinition().getProgressDisplayRegion();
        Region controllerDisplayRegion = controlPoint.getDefinition().getControllerDisplayRegion();

        final FiniteBlockRegion.Factory regionFactory = new FiniteBlockRegion.Factory(match.getMapInfo().proto);

        if(progressDisplayRegion == null) {
            this.progressDisplayRegion = null;
            this.progressDisplayImage = null;
        } else {
            this.progressDisplayRegion = regionFactory.fromWorld(progressDisplayRegion,
                                                                 match.getWorld(),
                                                                 visualMaterials);
            this.progressDisplayImage = new BlockImage(match.getWorld(), this.progressDisplayRegion.getBounds());
            this.progressDisplayImage.save();
        }

        if(controllerDisplayRegion == null) {
            this.controllerDisplayRegion = null;
            this.controllerDisplayImage = null;
        } else {
            // Ensure the controller and progress display regions do not overlap. The progress display has priority.
            this.controllerDisplayRegion = regionFactory.fromWorld(
                controllerDisplayRegion,
                match.getWorld(),
                this.progressDisplayRegion == null ? visualMaterials
                                                   : AllFilter.of(visualMaterials, new InverseFilter(progressDisplayRegion))
            );

            this.controllerDisplayImage = new BlockImage(match.getWorld(), this.controllerDisplayRegion.getBounds());
            this.controllerDisplayImage.save();
        }
    }

    /**
     * Change the controller display to the given team's color, or reset the display if team is null
     */
    @SuppressWarnings("deprecation")
    public void setController(Competitor controllingTeam) {
        if(!Objects.equals(this.controllingTeam, controllingTeam) && this.controllerDisplayRegion != null) {
            if(controllingTeam == null) {
                for(BlockVector block : this.controllerDisplayRegion.getBlockVectors()) {
                    this.controllerDisplayImage.restore(block);
                }
            } else {
                byte blockData = BukkitUtils.chatColorToDyeColor(controllingTeam.getColor()).getWoolData();
                for(BlockVector pos : this.controllerDisplayRegion.getBlockVectors()) {
                    BlockUtils.blockAt(match.getWorld(), pos).setData(blockData);
                }
            }
            this.controllingTeam = controllingTeam;
        }
    }

    private void setBlock(BlockVector pos, Competitor team) {
        final Block block = BlockUtils.blockAt(match.getWorld(), pos);
        if(this.controlPoint.getDefinition().getVisualMaterials().query(new BlockQuery(block)).isAllowed()) {
            if(team != null) {
                block.setData(BukkitUtils.chatColorToDyeColor(team.getColor()).getWoolData());
            } else {
                this.progressDisplayImage.restore(pos);
            }
        }
    }

    protected void setProgress(Competitor controllingTeam, Competitor capturingTeam, double capturingProgress) {
        if(this.progressDisplayRegion != null) {
            Vector center = this.progressDisplayRegion.getBounds().center();

            // capturingProgress can be zero, but it can never be one, so invert it to avoid
            // a zero-area SectorRegion that can cause glitchy rendering
            SectorRegion sectorRegion = new SectorRegion(center.getX(), center.getZ(), 0, (1 - capturingProgress) * 2 * Math.PI);

            for(BlockVector pos : this.progressDisplayRegion.getBlockVectors()) {
                if(sectorRegion.contains(pos)) {
                    this.setBlock(pos, controllingTeam);
                } else {
                    this.setBlock(pos, capturingTeam);
                }
            }
        }
    }

    public void render() {
        this.setController(this.controlPoint.getOwner());
        this.setProgress(this.controlPoint.getOwner(),
                         this.controlPoint.getCapturer(),
                         this.controlPoint.getCompletion());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTimeChange(CapturingTimeChangeEvent event) {
        if(this.controlPoint == event.getControlPoint()) {
            this.setProgress(event.getControlPoint().getOwner(),
                             event.getControlPoint().getCapturer(),
                             event.getControlPoint().getCompletion());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onControllerChange(ControllerChangeEvent event) {
        if(this.controlPoint == event.getControlPoint()) {
            this.setController(event.getNewController());
        }
    }
}
