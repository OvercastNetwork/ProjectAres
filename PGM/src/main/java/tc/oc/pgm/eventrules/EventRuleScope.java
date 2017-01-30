package tc.oc.pgm.eventrules;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

public enum EventRuleScope {
    PLAYER_ENTER("enter"),
    PLAYER_LEAVE("leave"),
    BLOCK_PLACE("block", "block-place"),
    BLOCK_BREAK("block", "block-break"),
    USE("use"),
    EFFECT(),
    BLOCK_PLACE_AGAINST("block-place-against"),
    BLOCK_PHYSICS("block-physics");

    public final String[] tags;

    EventRuleScope(String... tags) {
        this.tags = tags;
    }

    public static final SetMultimap<String, EventRuleScope> byTag;

    static {
        ImmutableSetMultimap.Builder<String, EventRuleScope> builder = ImmutableSetMultimap.builder();
        for(EventRuleScope scope : values()) {
            for(String tag : scope.tags) {
                builder.put(tag, scope);
            }
        }
        byTag = builder.build();
    }
}
