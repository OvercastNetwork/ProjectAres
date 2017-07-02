package tc.oc.pgm.tracker;

import com.google.inject.multibindings.Multibinder;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.inject.Manifest;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.tracker.resolvers.DamageResolver;
import tc.oc.pgm.tracker.resolvers.ExplosionDamageResolver;
import tc.oc.pgm.tracker.resolvers.FallingBlockDamageResolver;
import tc.oc.pgm.tracker.resolvers.GenericDamageResolver;
import tc.oc.pgm.tracker.resolvers.PotionDamageResolver;
import tc.oc.pgm.tracker.trackers.AnvilTracker;
import tc.oc.pgm.tracker.trackers.BlockTracker;
import tc.oc.pgm.tracker.trackers.CombatLogTracker;
import tc.oc.pgm.tracker.trackers.DeathTracker;
import tc.oc.pgm.tracker.trackers.DispenserTracker;
import tc.oc.pgm.tracker.trackers.EntityTracker;
import tc.oc.pgm.tracker.trackers.FallTracker;
import tc.oc.pgm.tracker.trackers.FireTracker;
import tc.oc.pgm.tracker.trackers.OwnedMobTracker;
import tc.oc.pgm.tracker.trackers.PlayerLocationTracker;
import tc.oc.pgm.tracker.trackers.ProjectileTracker;
import tc.oc.pgm.tracker.trackers.SpleefTracker;
import tc.oc.pgm.tracker.trackers.TNTTracker;

public class TrackerManifest extends HybridManifest implements MatchBinders {
    @Override
    protected void configure() {
        installIn(MatchScoped.class, new Manifest() {
            @Override protected void configure() {
                bind(MasterResolver.class);
                bind(EntityTracker.class);
                bind(BlockTracker.class);

                bind(SpleefTracker.class);
                bind(ProjectileTracker.class);
                bind(FallTracker.class);
                bind(FireTracker.class);
                bind(DispenserTracker.class);
                bind(TNTTracker.class);
                bind(OwnedMobTracker.class);
                bind(AnvilTracker.class);
                bind(CombatLogTracker.class);
                bind(DeathTracker.class);
                bind(PlayerLocationTracker.class);

                bind(PotionDamageResolver.class);
                bind(ExplosionDamageResolver.class);
                bind(FallingBlockDamageResolver.class);
                bind(GenericDamageResolver.class);
            }
        });

        bind(EventResolver.class).to(MasterResolver.class);
        bind(ProjectileResolver.class).to(MasterResolver.class);
        bind(EntityResolver.class).to(EntityTracker.class);
        bind(BlockResolver.class).to(BlockTracker.class);

        bindProxy(EventResolver.class);
        bindProxy(ProjectileResolver.class);
        bindProxy(EntityResolver.class);
        bindProxy(BlockResolver.class);

        matchListener(EntityTracker.class, MatchScope.RUNNING);
        matchListener(BlockTracker.class, MatchScope.RUNNING);
        matchListener(SpleefTracker.class, MatchScope.RUNNING);
        matchListener(ProjectileTracker.class, MatchScope.RUNNING);
        matchListener(FallTracker.class, MatchScope.RUNNING);
        matchListener(FireTracker.class, MatchScope.RUNNING);
        matchListener(DispenserTracker.class, MatchScope.RUNNING);
        matchListener(TNTTracker.class, MatchScope.RUNNING);
        matchListener(OwnedMobTracker.class, MatchScope.RUNNING);
        matchListener(AnvilTracker.class, MatchScope.RUNNING);
        matchListener(CombatLogTracker.class, MatchScope.RUNNING);
        matchListener(DeathTracker.class, MatchScope.RUNNING);
        matchListener(PlayerLocationTracker.class, MatchScope.RUNNING);

        // Damage resolvers - order is important!
        final Multibinder<DamageResolver> damageResolvers = inSet(DamageResolver.class);
        damageResolvers.addBinding().to(FallTracker.class);
        damageResolvers.addBinding().to(FireTracker.class);
        damageResolvers.addBinding().to(PotionDamageResolver.class);
        damageResolvers.addBinding().to(ExplosionDamageResolver.class);
        damageResolvers.addBinding().to(FallingBlockDamageResolver.class);
        damageResolvers.addBinding().to(GenericDamageResolver.class);
    }
}
