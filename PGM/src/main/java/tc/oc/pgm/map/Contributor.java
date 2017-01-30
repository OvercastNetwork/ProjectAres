package tc.oc.pgm.map;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.chat.Named;
import tc.oc.commons.bukkit.chat.PlayerComponent;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.PGM;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A contributor to a {@link PGMMap}. Can have either or both of a UUID
 * and arbitrary String name. If a UUID is present, it is used to lookup
 * a username when the map loads. The fallback name is only used if the
 * lookup fails, or no UUID is provided (this could be used to credit
 * somebody without a Minecraft account, like mom or Jesus).
 */
public class Contributor implements Named {
    protected final @Nullable UUID uuid;
    protected final @Nullable String fallbackName;
    protected final @Nullable String contribution;

    protected @Nullable UserDoc.Identity user;

    /** Creates a contributor with a name and a contribution. */
    public Contributor(@Nullable UUID uuid, @Nullable String fallbackName, @Nullable String contribution) {
        this.uuid = uuid;
        this.fallbackName = fallbackName;
        this.contribution = contribution;

        checkArgument(uuid != null || fallbackName != null);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public @Nullable UUID getUuid() {
        return uuid;
    }

    /** Gets the name of this contributor. */
    public @Nullable String getName() {
        return user != null ? user.username() : this.fallbackName;
    }

    public @Nullable UserDoc.Identity getUser() {
        return user;
    }

    public void setUser(UserDoc.Identity user) {
        this.user = user;
    }

    public @Nullable Identity getIdentity() {
        return user == null ? null : PGM.get().injector().getInstance(IdentityProvider.class).createIdentity(getUser(), null);
    }

    @Override
    public BaseComponent getStyledName(NameStyle style) {
        return user != null ? new PlayerComponent(getIdentity(), style)
                            : new Component(fallbackName);
    }

    /**
     * @return true only if a username is available
     */
    public boolean hasName() {
        return this.user != null || this.fallbackName != null;
    }

    public boolean needsLookup() {
        return this.uuid != null && this.user == null;
    }

    /** Indicates whether or not this contributor has a specific contribution. */
    public boolean hasContribution() {
        return this.contribution != null;
    }

    /** Gets this contributor's contribution or null if none exists. */
    public @Nullable String getContribution() {
        return this.contribution;
    }

    public static List<Contributor> filterNamed(List<Contributor> contributors) {
        List<Contributor> resolved = new ArrayList<>();
        for(Contributor contributor : contributors) {
            if(contributor.hasName()) {
                resolved.add(contributor);
            }
        }
        return resolved;
    }
}
