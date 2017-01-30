package tc.oc.pgm.match;

import com.google.inject.Provides;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.inject.BukkitPlayerModule;
import tc.oc.commons.core.inject.InjectorScoped;
import tc.oc.commons.core.inject.Manifest;

public class MatchPlayerManifest extends Manifest {

    private final Player player;

    MatchPlayerManifest(Player player) {
        this.player = player;
    }

    @Provides Player player() { return player; }

    @Override
    protected void configure() {
        install(new BukkitPlayerModule());
        bind(MatchPlayer.class).in(InjectorScoped.class);
        install(new MatchFacetContextManifest<>(MatchPlayerFacet.class, MatchPlayer.class));
    }
}
