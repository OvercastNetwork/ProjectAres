package tc.oc.pgm.map;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.Collections2;
import tc.oc.api.docs.AbstractModel;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.modules.InfoModule;
import tc.oc.pgm.teams.TeamFactory;

import static tc.oc.commons.core.stream.Collectors.toImmutableList;

@MapScoped
public class MapDocument extends AbstractModel implements MapDoc {

    private final MapFolder folder;
    private final InfoModule infoModule;
    private final MapInfo info;
    private final List<MapDoc.Team> teams;
    private final List<UUID> authors, contributors;

    @Inject private MapDocument(MapFolder folder, InfoModule infoModule, MapInfo info, List<TeamFactory> teams) {
        this.folder = folder;
        this.infoModule = infoModule;
        this.info = info;
        this.teams = teams.stream()
                          .map(TeamFactory::getDocument)
                          .collect(toImmutableList());

        this.authors = info.authors.stream()
                                   .map(Contributor::getUuid)
                                   .filter(Objects::nonNull)
                                   .collect(toImmutableList());

        this.contributors = info.contributors.stream()
                                             .map(Contributor::getUuid)
                                             .filter(Objects::nonNull)
                                             .collect(toImmutableList());
    }

    @Override
    public String _id() {
        return info.id.toString();
    }

    @Override
    public String slug() {
        return info.slug();
    }

    @Override
    public MapDoc.Edition edition() {
        return info.edition();
    }

    @Override
    public Phase phase() {
        return info.phase();
    }

    @Override
    public String name() {
        return info.name;
    }

    @Override
    public Set<Gamemode> gamemode() { // (s)
        return infoModule.getGamemodes();
    }

    @Override
    public String objective() {
        return info.objective.toPlainText();
    }

    @Override
    public int min_players() {
        return infoModule.getGlobalPlayerLimits().lowerEndpoint();
    }

    @Override
    public int max_players() {
        return infoModule.getGlobalPlayerLimits().upperEndpoint();
    }

    @Override
    public Path path() {
        return folder.getAbsolutePath();
    }

    @Override
    public @Nullable String url() {
        final URL url = folder.getUrl();
        return url == null ? null : url.toExternalForm();
    }

    @Override
    public Collection<String> images() {
        return Collections2.transform(folder.getImages(), path -> path.getFileName().toString());
    }

    @Override
    public SemanticVersion version() {
        return info.version;
    }

    @Override
    public MapDoc.Genre genre() {
        return info.genre;
    }

    @Override
    public List<Team> teams() {
        return teams;
    }

    @Override
    public Collection<UUID> author_uuids() {
        return authors;
    }

    @Override
    public Collection<UUID> contributor_uuids() {
        return contributors;
    }

}
