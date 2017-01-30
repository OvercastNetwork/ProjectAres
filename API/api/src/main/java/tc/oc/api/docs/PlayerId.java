package tc.oc.api.docs;

import javax.annotation.Nonnull;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.CompetitorDoc;
import tc.oc.api.docs.virtual.Model;

/**
 * Subclass of {@link UserId} that adds a {@link #username()} field, which contains
 * the most recently seen username for the player. It also extends {@link Model},
 * so the {@link #_id()} field is available.
 *
 * This class is used to pass an ID and username around together, so that it can both be
 * displayed and used in the DB (like we used to be able to do with usernames alone).
 */
@Serialize
public interface PlayerId extends UserId, Model, CompetitorDoc {
    @Nonnull String username();

    @Override @Serialize(false)
    default String toShortString() {
        return username();
    }
}
