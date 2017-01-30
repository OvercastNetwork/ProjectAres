package tc.oc.pgm.tracker.damage;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.ParticipantState;

import static com.google.common.base.Preconditions.checkNotNull;

public class PlayerInfo extends Inspectable.Impl implements OwnerInfo, MeleeInfo, PhysicalInfo {

    @Inspect private final ParticipantState player;
    @Inspect private final ItemInfo weapon;

    public PlayerInfo(ParticipantState player, @Nullable ItemInfo weapon) {
        this.player = checkNotNull(player);
        this.weapon = weapon;
    }

    public PlayerInfo(ParticipantState player) {
        this(player, null);
    }

    public PlayerInfo(MatchPlayer player) {
        this(player.getParticipantState(), new ItemInfo(player.getInventory().getItemInHand()));
    }

    @Override
    public @Nullable ItemInfo getWeapon() {
        return weapon;
    }

    @Override
    public ParticipantState getOwner() {
        return player;
    }

    @Override
    public ParticipantState getAttacker() {
        return player;
    }

    @Override
    public String getIdentifier() {
        return player.getPlayerId().player_id();
    }

    @Override
    public BaseComponent getLocalizedName() {
        return player.getStyledName(NameStyle.COLOR);
    }
}
