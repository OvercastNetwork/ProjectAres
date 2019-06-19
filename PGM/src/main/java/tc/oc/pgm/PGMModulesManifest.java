package tc.oc.pgm;

import tc.oc.pgm.channels.ChannelManifest;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.animation.AnimationManifest;
import tc.oc.pgm.broadcast.BroadcastManifest;
import tc.oc.pgm.classes.ClassManifest;
import tc.oc.pgm.control.ControllableGoalManifest;
import tc.oc.pgm.core.CoreManifest;
import tc.oc.pgm.damage.DamageManifest;
import tc.oc.pgm.destroyable.DestroyableManifest;
import tc.oc.pgm.filters.FilterManifest;
import tc.oc.pgm.flag.FlagManifest;
import tc.oc.pgm.highlights.HighlightManifest;
import tc.oc.pgm.itemkeep.ItemKeepManifest;
import tc.oc.pgm.kits.KitManifest;
import tc.oc.pgm.lane.LaneManifest;
import tc.oc.pgm.blitz.BlitzManifest;
import tc.oc.pgm.legacy.LegacyManifest;
import tc.oc.pgm.loot.LootManifest;
import tc.oc.pgm.menu.MenuManifest;
import tc.oc.pgm.modes.ObjectiveModeManifest;
import tc.oc.pgm.payload.PayloadManifest;
import tc.oc.pgm.physics.PlayerPhysicsManifest;
import tc.oc.pgm.picker.PickerManifest;
import tc.oc.pgm.playerstats.StatsManifest;
import tc.oc.pgm.raindrops.RaindropManifest;
import tc.oc.pgm.regions.RegionManifest;
import tc.oc.pgm.scoreboard.ScoreboardManifest;
import tc.oc.pgm.structure.StructureManifest;
import tc.oc.pgm.teams.TeamManifest;
import tc.oc.pgm.terrain.TerrainManifest;
import tc.oc.pgm.tnt.TNTManifest;
import tc.oc.pgm.tokens.TokenManifest;
import tc.oc.pgm.tracker.TrackerManifest;
import tc.oc.pgm.tutorial.TutorialManifest;
import tc.oc.pgm.wool.WoolManifest;

/**
 * List of PGM modules that combine their various components into a single manifest.
 *
 * This is the preferred way to structure modules now.
 */
public class PGMModulesManifest extends HybridManifest {
    @Override
    protected void configure() {
        install(new FilterManifest());
        install(new RegionManifest());
        install(new KitManifest());
        install(new TeamManifest());
        install(new TrackerManifest());
        install(new StructureManifest());
        install(new AnimationManifest());
        install(new PickerManifest());
        install(new ScoreboardManifest());
        install(new DamageManifest());
        install(new ClassManifest());
        install(new ItemKeepManifest());
        install(new PlayerPhysicsManifest());
        install(new TNTManifest());
        install(new TutorialManifest());
        install(new FlagManifest());
        install(new LegacyManifest());
        install(new LootManifest());
        install(new TerrainManifest());
        install(new CoreManifest());
        install(new DestroyableManifest());
        install(new WoolManifest());
        install(new ControllableGoalManifest());
        install(new PayloadManifest());
        install(new LaneManifest());
        install(new BroadcastManifest());
        install(new StatsManifest());
        install(new ChannelManifest());
        install(new RaindropManifest());
        install(new TokenManifest());
        install(new ObjectiveModeManifest());
        install(new BlitzManifest());
        install(new HighlightManifest());
        install(new MenuManifest());
    }
}
