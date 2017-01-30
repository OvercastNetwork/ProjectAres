package tc.oc.pgm.ffa;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import com.google.common.collect.Range;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.bukkit.Bukkit;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.pgm.Config;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapModuleFactory;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.module.ModuleLoadException;
import tc.oc.pgm.teams.TeamModule;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;

@ModuleDescription(name = "free-for-all", follows = { TeamModule.class })
public class FreeForAllModule implements MapModule, MatchModuleFactory<FreeForAllMatchModule> {

    private final FreeForAllOptions options;

    public FreeForAllModule(FreeForAllOptions options) {
        this.options = options;
    }

    @Override
    public Set<MapDoc.Gamemode> getGamemodes(MapModuleContext context) {
        return Collections.singleton(MapDoc.Gamemode.ffa);
    }

    public FreeForAllOptions getOptions() {
        return options;
    }

    @Override
    public Range<Integer> getPlayerLimits() {
        return Range.closed(options.minPlayers, options.maxPlayers);
    }

    @Override
    public FreeForAllMatchModule createMatchModule(Match match) throws ModuleLoadException {
        return new FreeForAllMatchModule(match);
    }

    public static class Factory extends MapModuleFactory<FreeForAllModule> {
        @Override
        protected void configure() {
            super.configure();
            install(new FactoryModuleBuilder().build(Tribute.Factory.class));
        }

        @Provides @MapScoped FreeForAllOptions options(FreeForAllModule module) {
            return module.getOptions();
        }

        @Override
        public @Nullable FreeForAllModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
            Element elPlayers = doc.getRootElement().getChild("players");

            if(context.hasModule(TeamModule.class)) {
                if(elPlayers != null) throw new InvalidXMLException("Cannot combine <players> and <teams>", elPlayers);
                return null;
            } else {
                int minPlayers = Config.minimumPlayers();
                int maxPlayers = Bukkit.getMaxPlayers();
                int maxOverfill = maxPlayers;
                org.bukkit.scoreboard.Team.OptionStatus nameTagVisibility = org.bukkit.scoreboard.Team.OptionStatus.ALWAYS;
                boolean colors = false;

                if(elPlayers != null) {
                    minPlayers = XMLUtils.parseNumber(elPlayers.getAttribute("min"), Integer.class, minPlayers);
                    maxPlayers = XMLUtils.parseNumber(elPlayers.getAttribute("max"), Integer.class, maxPlayers);
                    maxOverfill = XMLUtils.parseNumber(elPlayers.getAttribute("max-overfill"), Integer.class, maxOverfill);

                    nameTagVisibility = XMLUtils.parseNameTagVisibility(elPlayers, "show-name-tags").optional(nameTagVisibility);
                    colors = XMLUtils.parseBoolean(elPlayers.getAttribute("colors"), colors);
                }

                if(minPlayers > maxPlayers) {
                    throw new InvalidXMLException("min players (" + minPlayers + ") cannot be greater than max players (" + maxPlayers + ")", elPlayers);
                }

                return new FreeForAllModule(new FreeForAllOptions(minPlayers, maxPlayers, maxOverfill, nameTagVisibility, colors));
            }
        }
    }
}
