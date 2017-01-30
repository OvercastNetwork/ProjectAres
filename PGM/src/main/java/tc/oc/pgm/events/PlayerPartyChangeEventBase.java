package tc.oc.pgm.events;

import java.util.Optional;
import javax.annotation.Nullable;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;

/**
 * <p>Base for all events related to players joining/leaving a party/match.</p>
 *
 * <strong>
 * NOTE: Just use {@link PlayerChangePartyEvent} for everything, don't bother
 * with the rest of these. I'll leave the documentation here for maintenance
 * purposes.
 * </strong>
 *
 * <p>This is an abstract event that cannot be listened for. It exists only
 * to provide common code to its subclasses.</p>
 *
 * <p>The two descendent lineages, starting with {@link PlayerLeavePartyEvent} and
 * {@link PlayerPartyChangeEvent}, fire before and after the player changes parties, respectively.
 * The events within each lineage are mutually exclusive: only one of them will fire for any
 * particular change, depending on the type of change. However, they share a {@link HandlerList},
 * so listening for one of them will also receive any of its subclasses.</p>
 *
 * <p>The complete hiearchy looks like this:</p>
 *
 * <pre>
 * {@link PlayerPartyChangeEventBase}           Abstract, never fired
 *  ┣━ {@link PlayerChangePartyEvent}           Fired around all party changes
 *  ┣━ {@link PlayerLeavePartyEvent}            Fired before leaving one party to join another party
 *  ┃   ┗━ {@link PlayerLeaveMatchEvent}        Fired before leaving the final party before leaving the match
 *  ┗━ {@link PlayerPartyChangeEvent}           Fired after leaving the final party before leaving the match
 *      ┗━ {@link PlayerJoinPartyEvent}         Fired after leaving one party and joining another party
 *          ┗━ {@link PlayerJoinMatchEvent}     Fired after joining the initial party after joining the match
 * </pre>
 *
 * <p>But this table might clarify the concept:</p>
 *
 * <table>
 *     <tr>
 *         <td></td>
 *         <th>Join match</th>
 *         <th>Switch parties</th>
 *         <th>Leave match</th>
 *     </tr>
 *     <tr>
 *         <th>Before change</th>
 *         <td></td>
 *         <td><code>PlayerLeavePartyEvent</code></td>
 *         <td><code>PlayerLeaveMatchEvent</code></td>
 *     </tr>
 *     <tr>
 *         <th>After change</th>
 *         <td><code>PlayerJoinMatchEvent</code></td>
 *         <td><code>PlayerJoinPartyEvent</code></td>
 *         <td><code>PlayerPartyChangeEvent</code></td>
 *     </tr>
 * </table>
 */
public abstract class PlayerPartyChangeEventBase extends SingleMatchPlayerEvent {

    protected final Optional<Party> oldParty;
    protected final Optional<Party> newParty;

    protected PlayerPartyChangeEventBase(MatchPlayer player, @Nullable Party oldParty, @Nullable Party newParty) {
        super(player);
        this.newParty = Optional.ofNullable(newParty);
        this.oldParty = Optional.ofNullable(oldParty);
    }

    public Optional<Party> oldParty() {
        return oldParty;
    }

    public Optional<Party> newParty() {
        return newParty;
    }

    public @Nullable Party getOldParty() {
        return this.oldParty.orElse(null);
    }

    public @Nullable Party getNewParty() {
        return this.newParty.orElse(null);
    }

    public boolean wasParticipating() {
        return oldParty.filter(Party::isParticipatingType).isPresent();
    }

    public boolean isParticipating() {
        return newParty.filter(Party::isParticipatingType).isPresent();
    }

    public boolean isJoiningMatch() {
        return !oldParty().isPresent();
    }

    public boolean isLeavingMatch() {
        return !newParty().isPresent();
    }
}
