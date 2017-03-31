package tc.oc.pgm.blitz;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.jdom2.Document;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapModuleFactory;
import tc.oc.pgm.xml.InvalidXMLException;

import javax.inject.Inject;
import javax.inject.Provider;

public class BlitzModule implements MapModule {

    private final BlitzProperties properties;

    public BlitzModule(BlitzProperties properties) {
        this.properties = properties;
    }

    public boolean active() {
        return !properties.empty();
    }

    @Override
    public Set<MapDoc.Gamemode> getGamemodes(MapModuleContext context) {
        return active() ? Collections.singleton(MapDoc.Gamemode.blitz) : Collections.emptySet();
    }

    @Override
    public BaseComponent getGameName(MapModuleContext context) {
        if(!active()) {
            return null;
        } else if(!properties.multipleLives()) {
            return new TranslatableComponent("match.scoreboard.playersRemaining.title");
        } else if(properties.teams.isEmpty()) {
            return new TranslatableComponent("match.scoreboard.livesRemaining.title");
        } else {
            return new TranslatableComponent("match.scoreboard.blitz.title");
        }
    }

    public static class Factory extends MapModuleFactory<BlitzModule> {

        @Inject Provider<BlitzProperties> propertiesProvider;

        @Override
        public BlitzModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
            return new BlitzModule(propertiesProvider.get());
        }

    }

}
