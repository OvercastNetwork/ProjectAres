package tc.oc.commons.bukkit.chat;

import java.util.List;
import java.util.Objects;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.core.chat.ImmutableComponent;
import tc.oc.commons.core.util.Utils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A component that renders as a player's name.
 *
 * The "fancy" flag determines whether the name will include
 * flair and other decorations.
 *
 * The "big" flag shows the player's nickname after their
 * real name when they are nicked and the viewer can see
 * through it.
 *
 * A non-fancy, non-big name has color and nothing else.
 */
public class PlayerComponent extends ImmutableComponent {

    private final Identity identity;
    private final NameStyle style;

    public PlayerComponent(Identity identity, NameStyle style) {
        this.identity = checkNotNull(identity);
        this.style = checkNotNull(style);
    }

    public PlayerComponent(Identity identity) {
        this(identity, NameStyle.VERBOSE);
    }

    public PlayerComponent(PlayerComponent original) {
        this(original.identity, original.style);
    }

    public Identity getIdentity() {
        return identity;
    }

    public NameStyle getStyle() {
        return style;
    }

    @Override
    public BaseComponent duplicate() {
        return new PlayerComponent(this);
    }

    @Override
    public BaseComponent duplicateWithoutFormatting() {
        return duplicate();
    }

    @Override
    protected void toStringFirst(List<String> fields) {
        super.toStringFirst(fields);
        fields.add("identity=" + identity);
        fields.add("style=" + style);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), identity, style);
    }

    @Override
    protected boolean equals(BaseComponent obj) {
        return Utils.equals(PlayerComponent.class, this, obj, that ->
            identity.equals(that.getIdentity()) &&
            style.equals(that.getStyle()) &&
            super.equals(that)
        );
    }
}
