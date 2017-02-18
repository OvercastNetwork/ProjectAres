package tc.oc.pgm;

import tc.oc.commons.bukkit.chat.FlairRenderer;
import tc.oc.commons.bukkit.nick.UsernameRenderer;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;
import tc.oc.pgm.analytics.MatchAnalyticsManifest;
import tc.oc.pgm.antigrief.DefuseListener;
import tc.oc.pgm.chat.MatchFlairRenderer;
import tc.oc.pgm.chat.MatchNameInvalidator;
import tc.oc.pgm.chat.MatchUsernameRenderer;
import tc.oc.pgm.commands.AdminCommands;
import tc.oc.pgm.commands.MatchCommands;
import tc.oc.pgm.debug.PGMLeakListener;
import tc.oc.pgm.development.MapDevelopmentCommands;
import tc.oc.pgm.development.MapErrorTracker;
import tc.oc.pgm.freeze.FreezeCommands;
import tc.oc.pgm.freeze.FreezeListener;
import tc.oc.pgm.listeners.BlockTransformListener;
import tc.oc.pgm.listeners.MatchAnnouncer;
import tc.oc.pgm.listeners.PGMListener;
import tc.oc.pgm.listing.ListingManifest;
import tc.oc.pgm.map.MapLibrary;
import tc.oc.pgm.map.MapLibraryImpl;
import tc.oc.pgm.map.MapLoader;
import tc.oc.pgm.map.MapLoaderImpl;
import tc.oc.pgm.map.inject.MapManifest;
import tc.oc.pgm.match.MatchFinder;
import tc.oc.pgm.match.MatchLoader;
import tc.oc.pgm.match.MatchManager;
import tc.oc.pgm.match.MatchManifest;
import tc.oc.pgm.match.MatchPlayerEventRouter;
import tc.oc.pgm.module.MatchModulesManifest;
import tc.oc.pgm.mutation.command.MutationCommands;
import tc.oc.pgm.restart.RestartListener;
import tc.oc.pgm.settings.Settings;
import tc.oc.pgm.spawns.states.State;
import tc.oc.pgm.tnt.license.LicenseBroker;
import tc.oc.pgm.tnt.license.LicenseCommands;
import tc.oc.pgm.tnt.license.LicenseMonitor;
import tc.oc.pgm.xml.parser.ParserManifest;

public final class PGMManifest extends HybridManifest {
    @Override
    protected void configure() {
        install(new Settings());

        install(new ParserManifest());

        install(new MapManifest());
        install(new MatchManifest());

        install(new PGMModulesManifest());
        install(new MatchModulesManifest());
        install(new MapModulesManifest());

        install(new MatchPlayerEventRouter.Manifest());
        install(new MatchAnalyticsManifest());

        install(new ListingManifest());

        bind(MatchManager.class);
        bind(MatchLoader.class);
        bind(MatchFinder.class).to(MatchLoader.class);

        bind(MapLibrary.class).to(MapLibraryImpl.class);
        bind(MapLoader.class).to(MapLoaderImpl.class);

        // Tourney needs this
        expose(MapLibrary.class);

        bind(MatchUsernameRenderer.class);
        bind(MatchFlairRenderer.class);
        bindAndExpose(UsernameRenderer.class).to(MatchUsernameRenderer.class);
        bindAndExpose(FlairRenderer.class).to(MatchFlairRenderer.class);

        final PluginFacetBinder facets = new PluginFacetBinder(binder());
        facets.register(AdminCommands.class);
        facets.register(MatchNameInvalidator.class);
        facets.register(MapDevelopmentCommands.class);
        facets.register(MapErrorTracker.class);
        facets.register(MatchAnnouncer.class);
        facets.register(MatchCommands.class);
        facets.register(MutationCommands.class);
        facets.register(MutationCommands.Parent.class);
        facets.register(PGMLeakListener.class);
        facets.register(PGMListener.class);
        facets.register(RestartListener.class);
        facets.register(LicenseBroker.class);
        facets.register(LicenseMonitor.class);
        facets.register(LicenseCommands.class);
        facets.register(LicenseCommands.Parent.class);
        facets.register(BlockTransformListener.class);
        facets.register(DefuseListener.class);
        facets.register(FreezeCommands.class);
        facets.register(FreezeListener.class);

        requestStaticInjection(State.class);
    }
}
