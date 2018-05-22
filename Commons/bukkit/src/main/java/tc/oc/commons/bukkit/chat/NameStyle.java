package tc.oc.commons.bukkit.chat;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Sets;

/**
 * The formatting properties for each different context in which names are displayed.
 * Unlike {@link NameType}, this varies only by context, and is independent of the viewer.
 */
public class NameStyle extends ForwardingSet<NameFlag> {

    // No formatting
    public static final NameStyle PLAIN = new NameStyle(EnumSet.noneOf(NameFlag.class));

    // Just color
    public static final NameStyle COLOR = new NameStyle(EnumSet.of(
        NameFlag.COLOR,
        NameFlag.TELEPORT)
    );

    // Flair, color, various other formatting, and click/hover actions
    public static final NameStyle FANCY = new NameStyle(EnumSet.of(
        NameFlag.COLOR,
        NameFlag.FLAIR,
        NameFlag.SELF,
        NameFlag.FRIEND,
        NameFlag.DISGUISE,
        NameFlag.TELEPORT,
        NameFlag.MAPMAKER)
    );

    // Fancy plus in-game status i.e. grey when dead
    public static final NameStyle GAME = new NameStyle(
        Sets.union(
            FANCY,
            Collections.singleton(NameFlag.DEATH)
        )
    );

    // Fancy plus full nickname
    public static final NameStyle VERBOSE = new NameStyle(
        Sets.union(
            FANCY,
            Collections.singleton(NameFlag.NICKNAME)
        )
    );


    public static final NameStyle VERBOSE_SIMPLE = new NameStyle(
        Sets.difference(
            VERBOSE,
            Sets.newHashSet(NameFlag.SELF, NameFlag.FRIEND)
        )
    );

    // Fancy minus mapmaker flair (for display in map credits)
    public static final NameStyle MAPMAKER = new NameStyle(
        Sets.difference(
            FANCY,
            Collections.singleton(NameFlag.MAPMAKER)
        )
    );

    private final EnumSet<NameFlag> flags;

    public NameStyle(EnumSet<NameFlag> flags) {
        this.flags = flags;
    }

    public NameStyle(Iterable<NameFlag> flags) {
        this(Sets.newEnumSet(flags, NameFlag.class));
    }

    @Override
    protected Set<NameFlag> delegate() { return flags; }
}
