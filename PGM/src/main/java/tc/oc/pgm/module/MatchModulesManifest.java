package tc.oc.pgm.module;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.api.EngagementMatchModule;
import tc.oc.pgm.api.MatchPublishingMatchModule;
import tc.oc.pgm.api.ParticipationPublishingMatchModule;
import tc.oc.pgm.bossbar.BossBarMatchModule;
import tc.oc.pgm.cycle.CycleMatchModule;
import tc.oc.pgm.damage.HitboxMatchModule;
import tc.oc.pgm.death.DeathMessageMatchModule;
import tc.oc.pgm.doublejump.DoubleJumpMatchModule;
import tc.oc.pgm.effect.BloodMatchModule;
import tc.oc.pgm.effect.LongRangeExplosionMatchModule;
import tc.oc.pgm.effect.ProjectileTrailMatchModule;
import tc.oc.pgm.fireworks.PostMatchFireworkListener;
import tc.oc.pgm.goals.GoalMatchModule;
import tc.oc.pgm.inventory.ViewInventoryMatchModule;
import tc.oc.pgm.join.JoinMatchModule;
import tc.oc.pgm.listeners.WorldProblemMatchModule;
import tc.oc.pgm.mapratings.MapRatingsMatchModule;
import tc.oc.pgm.match.inject.MatchModuleFixtureManifest;
import tc.oc.pgm.modules.ArrowRemovalMatchModule;
import tc.oc.pgm.modules.EventFilterMatchModule;
import tc.oc.pgm.modules.MultiTradeMatchModule;
import tc.oc.pgm.projectile.ProjectileMatchModule;
import tc.oc.pgm.quota.QuotaMatchModule;
import tc.oc.pgm.respack.ResourcePackMatchModule;
import tc.oc.pgm.shield.ShieldMatchModule;
import tc.oc.pgm.skillreq.SkillRequirementMatchModule;
import tc.oc.pgm.snapshot.SnapshotMatchModule;
import tc.oc.pgm.start.StartMatchModule;
import tc.oc.pgm.stats.DeathPublishingMatchModule;
import tc.oc.pgm.stats.ObjectivePublishingMatchModule;
import tc.oc.pgm.victory.VictoryMatchModule;

public class MatchModulesManifest extends HybridManifest {

    @Override
    protected void configure() {
        install(new DeathMessageMatchModule.Manifest());
        install(new ViewInventoryMatchModule.Manifest());
        install(new JoinMatchModule.Manifest());
        install(new CycleMatchModule.Manifest());

        install(new MatchModuleFixtureManifest<EventFilterMatchModule>(){});
        install(new MatchModuleFixtureManifest<MultiTradeMatchModule>(){});
        install(new MatchModuleFixtureManifest<BossBarMatchModule>(){});
        install(new MatchModuleFixtureManifest<SnapshotMatchModule>(){});
        install(new MatchModuleFixtureManifest<ShieldMatchModule>(){});
        install(new MatchModuleFixtureManifest<QuotaMatchModule>(){});
        install(new MatchModuleFixtureManifest<SkillRequirementMatchModule>(){});
        install(new MatchModuleFixtureManifest<StartMatchModule>(){});
        install(new MatchModuleFixtureManifest<ArrowRemovalMatchModule>(){});
        install(new MatchModuleFixtureManifest<DoubleJumpMatchModule>(){});
        install(new MatchModuleFixtureManifest<GoalMatchModule>(){});
        install(new MatchModuleFixtureManifest<ProjectileMatchModule>(){});
        install(new MatchModuleFixtureManifest<MapRatingsMatchModule>(){});
        install(new MatchModuleFixtureManifest<MatchPublishingMatchModule>(){});
        install(new MatchModuleFixtureManifest<ParticipationPublishingMatchModule>(){});
        install(new MatchModuleFixtureManifest<ObjectivePublishingMatchModule>(){});
        install(new MatchModuleFixtureManifest<EngagementMatchModule>(){});
        install(new MatchModuleFixtureManifest<ResourcePackMatchModule>(){});
        install(new MatchModuleFixtureManifest<HitboxMatchModule>(){});
        install(new MatchModuleFixtureManifest<PostMatchFireworkListener>(){});
        install(new MatchModuleFixtureManifest<WorldProblemMatchModule>(){});
        install(new MatchModuleFixtureManifest<DeathPublishingMatchModule>(){});
        install(new MatchModuleFixtureManifest<ProjectileTrailMatchModule>(){});
        install(new MatchModuleFixtureManifest<BloodMatchModule>(){});
        install(new MatchModuleFixtureManifest<LongRangeExplosionMatchModule>(){});
        install(new MatchModuleFixtureManifest<VictoryMatchModule>(){});
    }
}
