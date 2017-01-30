package tc.oc.api.docs;

import javax.annotation.Nonnull;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.UserDoc;

/**
 * Wrapper for user.player_id values. It identifies a player stored in the DB,
 * but contains no username, which has a few ramifications:
 *
 *  - You cannot display this to the user
 *  - You cannot directly associate this with an online player
 *
 * Doing either of the above requires a lookup in the DB or PlayerIdMap.
 * See the subclasses of this class for more explanation.
 */
@Serialize
public interface UserId extends UserDoc.Partial {
    @Nonnull String player_id();
}
