package tc.oc.pgm.channels;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.match.inject.MatchModuleFixtureManifest;

public class ChannelManifest extends HybridManifest {
    @Override
    protected void configure() {
        installFactory(PartyChannel.Factory.class);
        install(new MatchModuleFixtureManifest<ChannelMatchModule>(){});
    }
}
