package tc.oc.pgm.map;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Range;
import com.google.common.hash.HashCode;
import org.jdom2.Document;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.commons.core.util.Lazy;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.module.ModuleContext;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.UnsupportedMapProtocolException;

/**
 * In addition to storing {@link MapModule}s, this class handles XML parsing from end to end.
 */
public class MapModuleContext extends ModuleContext<MapModule, MapScoped> {

    private final Collection<Provider<MapRootParser>> parsers;
    private final FeatureDefinitionContext featureDefinitionContext;
    private final MapFilePreprocessor preprocessor;
    private final Document xmlDocument;
    private final SemanticVersion proto;
    private final Path basePath;
    private final Provider<MapDocument> apiDocumentProvider;

    @Inject MapModuleContext(MapFolder mapFolder,
                             FeatureDefinitionContext featureDefinitionContext,
                             MapFilePreprocessor.Factory preprocessorFactory,
                             Provider<MapDocument> apiDocumentProvider,
                             Collection<Provider<MapRootParser>> parsers) throws InvalidXMLException {

        this.featureDefinitionContext = featureDefinitionContext;
        this.apiDocumentProvider = apiDocumentProvider;
        this.parsers = parsers;

        try {
            this.basePath = mapFolder.getAbsolutePath().toRealPath();
        } catch(IOException e) {
            throw new InvalidXMLException("File system error while resolving map folder " + mapFolder);
        }

        final Path descriptionFile = mapFolder.getAbsoluteDescriptionFilePath();
        if(!java.nio.file.Files.isRegularFile(descriptionFile)) {
            throw new MapNotFoundException(descriptionFile);
        }

        this.preprocessor = preprocessorFactory.create(mapFolder.getSource());
        this.xmlDocument = preprocessor.readRootDocument(descriptionFile);

        // verify proto
        final Node protoNode = Node.fromRequiredAttr(xmlDocument.getRootElement(), "proto");
        this.proto = XMLUtils.parseSemanticVersion(protoNode);
        if(proto.isNewerThan(ProtoVersions.CURRENT)) {
            throw new UnsupportedMapProtocolException(protoNode, proto);
        }
    }

    @Override
    public void load() {
        asCurrentScope(() -> {
            // Create MapModules
            super.load();

            // Call MapParser#parse
            parsers.forEach(
                parser -> ignoringFailures(
                    parser.get()::parse
                )
            );

            // Bail out early if there were any errors in the parse phase
            if(hasErrors()) return;

            // Resolve references and run validations
            addErrors(featureDefinitionContext.postParse());

            // Run module postParse methods
            loadedModules().forEach(
                module -> ignoringFailures(
                    () -> module.postParse(this, logger, xmlDocument)
                )
            );
        });
    }

    public Document xmlDocument() {
        return xmlDocument;
    }

    public MapDocument apiDocument() {
        return asCurrentScope(apiDocumentProvider::get);
    }

    public Path getBasePath() {
        return basePath;
    }

    public SemanticVersion getProto() {
        return proto;
    }

    public FeatureDefinitionContext features() {
        return this.featureDefinitionContext;
    }

    public Map<Path, HashCode> loadedFiles() {
        return preprocessor.getIncludedFiles();
    }

    private final Lazy<Range<Integer>> playerLimits = Lazy.from(() -> {
        int min = 0, max = 0;
        for(MapModule module : loadedModules()) {
            final Range<Integer> limits = module.getPlayerLimits();
            min += limits.lowerEndpoint();
            max += limits.upperEndpoint();
        }
        return Range.closed(min, max);
    });

    public Range<Integer> playerLimits() {
        return playerLimits.get();
    }

    public Integer playerLimitAverage() {
        Range<Integer> lims = playerLimits();
        int sum = lims.lowerEndpoint() + lims.upperEndpoint();

        return ((Double) Math.floor(sum/2)).intValue();
    }
}
