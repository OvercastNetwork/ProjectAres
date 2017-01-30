package tc.oc.pgm.wool;

import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.DyeColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import org.bukkit.util.Vector;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.commons.core.chat.ChatUtils;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.features.GamemodeFeature;
import tc.oc.pgm.goals.ProximityGoalDefinition;
import tc.oc.pgm.goals.ProximityGoalDefinitionImpl;
import tc.oc.pgm.goals.ProximityMetric;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.teams.TeamFactory;

@FeatureInfo(name = "wool")
public interface MonumentWoolFactory extends ProximityGoalDefinition, GamemodeFeature {

    static BaseComponent makeComponentName(DyeColor color) {
        return new Component(makeName(color), ChatUtils.convert(BukkitUtils.dyeColorToChatColor(color)));
    }

    static String makeColorName(DyeColor color) {
        String[] name = StringUtils.split(color.toString(), '_');
        for (int i = 0; i < name.length; i++) {
            name[i] = (i > 0 ? " " : "") + StringUtils.capitalize(name[i].toLowerCase());
        }
        return StringUtils.join(name);
    }

    static String makeName(DyeColor color) {
        return makeColorName(color) + " Wool";
    }

    @Override
    MonumentWool getGoal(Match match);

    @Override
    String getColoredName();

    String getColorName();

    DyeColor getColor();

    @Override
    BaseComponent getComponentName();

    Vector getLocation();

    Region getPlacementRegion();

    boolean isCraftable();

    boolean isObjectiveWool(ItemStack stack);

    boolean isObjectiveWool(MaterialData material);

    boolean isHolding(InventoryHolder holder);

    boolean isHolding(Inventory inv);
}

class MonumentWoolFactoryImpl extends ProximityGoalDefinitionImpl implements MonumentWoolFactory {
    private final @Inspect DyeColor color;
    private final @Inspect Vector location;
    private final @Inspect Region placement;
    private final @Inspect boolean craftable;
    private final @Inspect boolean visible;
    private final @Inspect BaseComponent componentName;

    public MonumentWoolFactoryImpl(@Nullable Boolean required,
                                   boolean visible,
                                   TeamFactory owner,
                                   ProximityMetric woolProximityMetric,
                                   ProximityMetric monumentProximityMetric,
                                   DyeColor color,
                                   Vector location,
                                   Region placement,
                                   boolean craftable) {

        super(MonumentWoolFactory.makeName(color), required, visible, Optional.of(owner), woolProximityMetric, monumentProximityMetric);
        this.color = color;
        this.location = location;
        this.placement = placement;
        this.craftable = craftable;
        this.visible = visible;
        this.componentName = MonumentWoolFactory.makeComponentName(color);
    }

    @Override
    public String toString() {
        return "MonumentWoolFactory{owner=" + this.getOwner().getDefaultName() +
               ", color=" + this.color +
               ", location=" + this.location +
               ", placement=" + this.placement +
               ", craftable=" + this.craftable +
               ", visible="   + this.visible   + "}";
    }

    @Override
    public Stream<MapDoc.Gamemode> gamemodes() {
        return Stream.of(MapDoc.Gamemode.ctw);
    }

    @Override
    public MonumentWool getGoal(Match match) {
        return (MonumentWool) super.getGoal(match);
    }

    @Override
    public MonumentWool createFeature(Match match) {
        return new MonumentWool(this, match);
    }

    @Override
    public boolean isShared() {
        return false;
    }

    @Override
    public String getColoredName() {
        return BukkitUtils.dyeColorToChatColor(this.color) + this.getName();
    }

    @Override
    public String getColorName() {
        return MonumentWoolFactory.makeColorName(this.color);
    }

    @Override
    public DyeColor getColor() {
        return this.color;
    }

    @Override
    public BaseComponent getComponentName() {
        return componentName;
    }

    @Override
    public Vector getLocation() {
        return this.location;
    }

    @Override
    public Region getPlacementRegion() {
        return this.placement;
    }

    @Override
    public boolean isCraftable() {
        return this.craftable;
    }

    @Override
    public boolean isObjectiveWool(ItemStack stack) {
        return stack != null && this.isObjectiveWool(stack.getData());
    }

    @Override
    public boolean isObjectiveWool(MaterialData material) {
        return material instanceof Wool && ((Wool) material).getColor() == this.color;
    }

    @Override
    public boolean isHolding(InventoryHolder holder) {
        return this.isHolding(holder.getInventory());
    }

    @Override
    public boolean isHolding(Inventory inv) {
        return inv.contents().stream().anyMatch(this::isObjectiveWool);
    }
}
