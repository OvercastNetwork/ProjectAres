package tc.oc.pgm.kits;

import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.Party;

public abstract class DelayedKit implements Kit {

    public abstract void applyDelayed(MatchPlayer player, boolean force);

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        Party party = player.getParty();
        player.getMatch().getScheduler(MatchScope.RUNNING).createDelayedTask(1L, () -> {
            if (player.isOnline() && player.getParty().equals(party))
                applyDelayed(player, force);
        });
    }

}
