package tc.oc.pgm.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayerState;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.goals.ModeChangeGoal;
import tc.oc.pgm.goals.TouchableGoal;
import tc.oc.pgm.regions.CuboidRegion;
import tc.oc.pgm.regions.FiniteBlockRegion;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.utils.MaterialPattern;

public class Core extends TouchableGoal<CoreFactory> implements ModeChangeGoal<CoreFactory> {

    protected final FiniteBlockRegion casingRegion;
    protected final FiniteBlockRegion lavaRegion;
    protected final Region leakRegion;

    protected MaterialData material;
    protected boolean leaked = false;
    protected Iterable<Location> proximityLocations;
    protected Map<MatchPlayerState, Material> breakers;

    public Core(CoreFactory definition, Match match) {
        super(definition, match);

        this.material = definition.getMaterial();

        Region region = definition.getRegion();

        final FiniteBlockRegion.Factory regionFactory = new FiniteBlockRegion.Factory(match.getMapInfo().proto);

        this.casingRegion = regionFactory.fromWorld(region, match.getWorld(), new MaterialPattern(this.material));
        if(this.casingRegion.blockVolume() == 0) {
            match.getServer().getLogger().warning("No casing material (" + this.material + ") found in core " + this.getName());
        }

        this.lavaRegion = regionFactory.fromWorld(region,
                                                  match.getWorld(),
                                                  new MaterialPattern(Material.LAVA, (byte) 0),
                                                  new MaterialPattern(Material.STATIONARY_LAVA, (byte) 0));
        if(this.lavaRegion.blockVolume() == 0) {
            match.getServer().getLogger().warning("No lava found in core " + this.getName());
        }

        Vector min = new Vector(region.getBounds().minimum()).subtract(new Vector(15, 0, 15));
        min.setY(0);
        Vector max = new Vector(region.getBounds().maximum()).add(new Vector(15, 0, 15));
        max.setY(region.getBounds().minimum().getY() - definition.getLeakLevel());
        this.leakRegion = new CuboidRegion(min, max);
        this.breakers = new HashMap<>();
    }

    // Remove @Nullable
    @Override
    public @Nonnull Team getOwner() {
        return super.getOwner();
    }

    @Override
    public boolean getDeferTouches() {
        return true;
    }

    @Override
    public BaseComponent getTouchMessage(@Nullable ParticipantState toucher, boolean self) {
        if(toucher == null) {
            return new TranslatableComponent("match.touch.core.owner",
                                             Components.blank(),
                                             getComponentName(),
                                             getOwner().getComponentName());
        } else if(self) {
            return new TranslatableComponent("match.touch.core.owner.you",
                                             Components.blank(),
                                             getComponentName(),
                                             getOwner().getComponentName());
        } else {
            return new TranslatableComponent("match.touch.core.owner.toucher",
                                             toucher.getStyledName(NameStyle.COLOR),
                                             getComponentName(),
                                             getOwner().getComponentName());
        }
    }

    @Override
    public Iterable<Location> getProximityLocations(ParticipantState player) {
        if(proximityLocations == null) {
            proximityLocations = Collections.singleton(casingRegion.getBounds().center().toLocation(this.getMatch().getWorld()));
        }
        return proximityLocations;
    }

    public MaterialData getMaterial() {
        return this.material;
    }

    public FiniteBlockRegion getCasingRegion() {
        return this.casingRegion;
    }

    public FiniteBlockRegion getLavaRegion() {
        return this.lavaRegion;
    }

    public Region getLeakRegion() {
        return this.leakRegion;
    }

    public void markLeaked() {
        this.leaked = true;
    }

    public boolean hasLeaked() {
        return this.leaked;
    }

    @Override
    public boolean canComplete(Competitor team) {
        return team != this.getOwner();
    }

    @Override
    public boolean isCompleted() {
        return this.leaked;
    }

    @Override
    public boolean isCompleted(Competitor team) {
        return this.leaked && this.canComplete(team);
    }

    @Override
    public boolean isAffectedByModeChanges() {
        return this.definition.hasModeChanges();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void replaceBlocks(MaterialData newMaterial) {
        for(Block block : this.getCasingRegion().getBlocks(match.getWorld())) {
            if(this.isObjectiveMaterial(block)) {
                block.setTypeIdAndData(newMaterial.getItemTypeId(), newMaterial.getData(), true);
            }
        }
        this.material = newMaterial;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isObjectiveMaterial(Block block) {
        return block.getType() == this.material.getItemType() && block.getData() == this.material.getData();
    }

    @Override
    public String getModeChangeMessage() {
        return "match.objectiveMode.name.core";
    }

    @Override
    public void touch(@Nullable ParticipantState toucher) {
        super.touch(toucher);
        breakers.putIfAbsent(toucher, material.getItemType());
    }

    public ImmutableList<CoreContribution> getContributions() {
        Set<ParticipantState> touchers = getTouchingPlayers();
        ImmutableList.Builder<CoreContribution> builder = ImmutableList.builder();
        for(MatchPlayerState player : touchers) {
            builder.add(new CoreContribution(player, 1d / touchers.size(), breakers.get(player)));
        }
        return builder.build();
    }
}
