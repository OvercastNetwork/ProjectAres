package tc.oc.pgm.join;

import javax.annotation.Nullable;

import tc.oc.pgm.match.MatchPlayer;

/**
 * Something that is able to join the player to the match, or prevent them from doing so.
 */
public interface JoinHandler {
    /**
     * Without side-effects or output, test what would happen if the given player tried to join the match right now.
     *
     * @param joining           The joining player
     * @param request           Object describing the way they want to join
     * @return                  Result of the join request. If the implementor does not know how to handle the
     *                          query, it can return null to delegate to whatever
     *                          other handlers are available. Any other result will be the final result of the
     *                          query, and no other handlers will be called.
     */
    @Nullable JoinResult queryJoin(MatchPlayer joining, JoinRequest request);

    /**
     * Try to join the given player to the match, or tell them why they can't. This handler does not have to
     * handle the request if it doesn't know how, or doesn't care. Note that a handler is allowed to return a
     * result from {@link #queryJoin} that it does not handle in {@link #join}, and vice-versa.
     *
     * @param joining           The joining player
     * @param request           Object describing the way they want to join
     * @param result            A fresh result from {@link #queryJoin} that should be used
     * @return                  True if this implementor "handled" the join, meaning either the player
     *                          joined the match successfully, or received some feedback explaining why they
     *                          didn't. Returning true prevents any other handlers from being called after
     *                          this one.
     */
    default boolean join(MatchPlayer joining, JoinRequest request, JoinResult result) { return false; }

    /**
     * Try to join all of the given players simultaneously. This is called with
     * all queued players when the match starts. This method will be called on
     * all handlers, breaking if the queue becomes empty. Any players left in
     * the queue will be joined through {@link #join}, and finally sent to obs
     * if that fails.
     */
    default void queuedJoin(QueuedParticipants queue) {}
}
