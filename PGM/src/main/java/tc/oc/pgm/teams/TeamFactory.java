package tc.oc.pgm.teams;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.inject.ImplementedBy;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.commons.core.chat.ChatUtils;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureFactory;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.features.SluggedFeatureDefinition;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.module.ModuleLoadException;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.finder.Attributes;
import tc.oc.pgm.xml.finder.ParentText;
import tc.oc.pgm.xml.validate.NonBlank;
import tc.oc.pgm.xml.validate.Validatable;

/**
 * Immutable class to represent a team in a map that is not tied to any
 * specific match.
 */
@FeatureInfo(name = "team")
@ImplementedBy(TeamFactoryImpl.class)
public interface TeamFactory extends SluggedFeatureDefinition, Validatable, FeatureFactory<Team> {

    /** Gets this team's default name as set by the map creator.
     * @return Default team name.
     */
    @Property(name="name")
    @Nodes({Attributes.class, ParentText.class})
    @Validate(NonBlank.class)
    String getDefaultName();

    @Property(name="plural")
    default boolean isDefaultNamePlural() {
        return false;
    }

    /** Gets this team's default color as set by the map creator.
     * @return Default team color.
     */
    @Property(name="color")
    default ChatColor getDefaultColor() {
        return ChatColor.WHITE;
    }

    default BaseComponent getComponentName() {
        return new Component(getDefaultName(), getDefaultColor());
    }

    @Property(name="min")
    Optional<Integer> getMinPlayers();

    /** Gets the maximum players that may be on this team.
     * @return Maximum players for this team.
     */
    @Property(name="max")
    int getMaxPlayers();

    /**
     * Gets the maximum overfill players that may be on this team.
     *
     * @return Maximum team overfill size for this team always >= maxPlayers
     */
    @Property(name="max-overfill")
    Optional<Integer> getMaxOverfill();

    @Property(name="show-name-tags")
    default org.bukkit.scoreboard.Team.OptionStatus getNameTagVisibility() {
        return org.bukkit.scoreboard.Team.OptionStatus.ALWAYS;
    }

    @Property(name="lives")
    Optional<Integer> getLives();

    MapDoc.Team getDocument();

    @Override
    default void validate() throws InvalidXMLException {
        if(getMaxOverfill().isPresent() && getMaxOverfill().get() < getMaxPlayers()) {
            throw new InvalidXMLException("Max overfill cannot be less than max players");
        }
        if(getLives().isPresent() && getLives().get() <= 0) {
            throw new InvalidXMLException("Lives must be at least 1");
        }
    }
}

abstract class TeamFactoryImpl extends FeatureDefinition.Impl implements TeamFactory {

    @Inject private Team.Factory factory;

    private final Document document = new Document();

    @Override
    public Team createFeature(Match match) throws ModuleLoadException {
        final Team team = factory.create(this);
        match.addParty(team);
        return team;
    }

    @Override
    public Optional<String> inspectIdentity() {
        return Optional.of(getDefaultName());
    }

    @Override
    public MapDoc.Team getDocument() {
        return document;
    }

    @Override
    public String defaultSlug() {
        return TeamFactory.super.defaultSlug() + "--" + slugify(getDefaultName());
    }

    public class Document implements MapDoc.Team {
        @Override
        public String _id() {
            return defaultSlug();
        }

        @Override
        public String name() {
            return getDefaultName();
        }

        @Override
        public @Nullable Integer min_players() {
            return getMinPlayers().orElse(0);
        }

        @Override
        public @Nullable Integer max_players() {
            return getMaxPlayers();
        }

        @Override
        public @Nullable net.md_5.bungee.api.ChatColor color() {
            return ChatUtils.convert(getDefaultColor());
        }
    }
}
