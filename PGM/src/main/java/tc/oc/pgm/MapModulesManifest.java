package tc.oc.pgm;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.beacon.BeaconModule;
import tc.oc.pgm.blockdrops.BlockDropsModule;
import tc.oc.pgm.crafting.CraftingModule;
import tc.oc.pgm.eventrules.EventRuleModule;
import tc.oc.pgm.fallingblocks.FallingBlocksModule;
import tc.oc.pgm.ffa.FreeForAllModule;
import tc.oc.pgm.gamerules.GameRulesModule;
import tc.oc.pgm.ghostsquadron.GhostSquadronModule;
import tc.oc.pgm.goals.GoalModule;
import tc.oc.pgm.hunger.HungerModule;
import tc.oc.pgm.itemmeta.ItemModifyModule;
import tc.oc.pgm.killreward.KillRewardModule;
import tc.oc.pgm.blitz.BlitzModule;
import tc.oc.pgm.map.MapModuleFactory;
import tc.oc.pgm.map.StaticMethodMapModuleFactory;
import tc.oc.pgm.modules.*;
import tc.oc.pgm.mutation.MutationMapModule;
import tc.oc.pgm.pickup.PickupModule;
import tc.oc.pgm.portals.PortalModule;
import tc.oc.pgm.projectile.ProjectileModule;
import tc.oc.pgm.proximity.ProximityAlarmModule;
import tc.oc.pgm.rage.RageModule;
import tc.oc.pgm.renewable.RenewableModule;
import tc.oc.pgm.score.ScoreModule;
import tc.oc.pgm.spawns.SpawnModule;
import tc.oc.pgm.stamina.StaminaModule;
import tc.oc.pgm.timelimit.TimeLimitModule;
import tc.oc.pgm.worldborder.WorldBorderModule;

/**
 * Registration of {@link MapModuleFactory}s
 */
public class MapModulesManifest extends HybridManifest {
    @Override
    protected void configure() {
        // MapModuleFactories
        install(new InfoModule.Factory());
        install(new FreeForAllModule.Factory());
        install(new CraftingModule.Factory());
        install(new ItemModifyModule.Factory());
        install(new PickupModule.Factory());
        install(new BeaconModule.Factory());
        install(new GoalModule.Factory());
        install(new ProjectileModule.Factory());
        install(new SpawnModule.Factory());
        install(new TimeLimitModule.Factory());
        install(new BlitzModule.Factory());

        // MapModules with static parse methods
        install(new StaticMethodMapModuleFactory<EventRuleModule>(){});
        install(new StaticMethodMapModuleFactory<PlayableRegionModule>(){});
        install(new StaticMethodMapModuleFactory<TimeLockModule>(){});
        install(new StaticMethodMapModuleFactory<ScoreModule>(){});
        install(new StaticMethodMapModuleFactory<ItemDestroyModule>(){});
        install(new StaticMethodMapModuleFactory<ToolRepairModule>(){});
        install(new StaticMethodMapModuleFactory<PortalModule>(){});
        install(new StaticMethodMapModuleFactory<MaxBuildHeightModule>(){});
        install(new StaticMethodMapModuleFactory<FlyingBoatModule>(){});
        install(new StaticMethodMapModuleFactory<ModifyBowProjectileModule>(){});
        install(new StaticMethodMapModuleFactory<MobsModule>(){});
        install(new StaticMethodMapModuleFactory<HungerModule>(){});
        install(new StaticMethodMapModuleFactory<KillRewardModule>(){});
        install(new StaticMethodMapModuleFactory<GhostSquadronModule>(){});
        install(new StaticMethodMapModuleFactory<RageModule>(){});
        install(new StaticMethodMapModuleFactory<FriendlyFireRefundModule>(){});
        install(new StaticMethodMapModuleFactory<BlockDropsModule>(){});
        install(new StaticMethodMapModuleFactory<RenewableModule>(){});
        install(new StaticMethodMapModuleFactory<InternalModule>(){});
        install(new StaticMethodMapModuleFactory<ProximityAlarmModule>(){});
        install(new StaticMethodMapModuleFactory<GameRulesModule>(){});
        install(new StaticMethodMapModuleFactory<FallingBlocksModule>(){});
        install(new StaticMethodMapModuleFactory<DiscardPotionBottlesModule>(){});
        install(new StaticMethodMapModuleFactory<WorldBorderModule>(){});
        install(new StaticMethodMapModuleFactory<StaminaModule>(){});
        install(new StaticMethodMapModuleFactory<MutationMapModule>(){});
    }
}
