package tc.oc.api.docs.virtual;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatColor;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.api.model.ModelName;

@Serialize
@Nonnull
@ModelName("Map")
public interface MapDoc extends Model {
    String slug();
    String name();
    @Nullable String url();
    @Nullable Path path();
    Collection<String> images();
    SemanticVersion version();
    int min_players();
    int max_players();
    String objective();

    enum Phase {
        DEVELOPMENT, PRODUCTION;
        public static final Phase DEFAULT = PRODUCTION;
    }
    Phase phase();

    enum Edition {
        STANDARD, RANKED, TOURNAMENT;
        public static final Edition DEFAULT = STANDARD;
    }
    Edition edition();

    enum Genre { OBJECTIVES, DEATHMATCH, OTHER }
    Genre genre();

    enum Gamemode { tdm, ctw, ctf, dtc, dtm, ad, koth, blitz, rage, scorebox, arcade, gs, ffa, mixed, skywars, survival, payload }
    Set<Gamemode> gamemode();

    List<Team> teams();

    Collection<UUID> author_uuids();
    Collection<UUID> contributor_uuids();

    @Serialize(false)
    default Stream<UUID> authorAndContributorUuids() {
        return Stream.concat(author_uuids().stream(),
                             contributor_uuids().stream());
    }

    @Serialize
    interface Team extends Model {
        @Nonnull String name();

        // Fields below shouldn't be nullable, but data is missing in some old match documents
        @Nullable Integer min_players();
        @Nullable Integer max_players();
        @Nullable ChatColor color();
    }
}
