package tc.oc.api.docs;

import java.time.Instant;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Serialize
public interface Objective extends Model {

    @Nonnull String name();
    @Nonnull String type();
    @Nonnull String feature_id();
    @Nonnull Instant date();
    @Nonnull String match_id();
    @Nonnull String server_id();
    @Nonnull String family();
    @Nullable Double x();
    @Nullable Double y();
    @Nullable Double z();
    @Nullable String team();
    @Nullable String player();

    @Serialize
    interface Colored extends Objective {
        @Nonnull String color();
    }

    @Serialize
    interface DestroyableDestroy extends Objective {
        default String type() { return "destroyable_destroy"; }
        int blocks_broken();
        double blocks_broken_percentage();
    }

    @Serialize
    interface CoreBreak extends Objective {
        default String type() { return "core_break"; }
        @Nonnull String material();
    }

    @Serialize
    interface FlagCapture extends Colored {
        default String type() { return "flag_capture"; }
        @Nullable String net_id();
    }

    @Serialize
    interface WoolPlace extends Colored {
        default String type() { return "wool_place"; }
    }

    @Override @Serialize(false)
    default String toShortString() {
        return name() + "[" + type() + "]";
    }

}
