package tc.oc.pgm.map.inject;

import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.jdom2.Document;
import org.jdom2.JDOMFactory;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.SAXHandler;
import org.jdom2.input.sax.SAXHandlerFactory;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.MapModulesManifest;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.map.MapConfiguration;
import tc.oc.pgm.map.MapDefinition;
import tc.oc.pgm.map.MapDocument;
import tc.oc.pgm.map.MapFilePreprocessor;
import tc.oc.pgm.map.MapFolder;
import tc.oc.pgm.map.MapLogger;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapProto;
import tc.oc.pgm.map.MapRootParser;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.map.PGMMapConfiguration;
import tc.oc.pgm.module.ModuleExceptionHandler;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.BoundedJDOMFactory;
import tc.oc.pgm.xml.BoundedSAXHandler;
import tc.oc.pgm.xml.validate.ValidationContext;

/**
 * Configuration related to {@link MapDefinition}s, {@link MapModule}s, and other map stuff.
 *
 * @see MapModulesManifest
 */
public class MapManifest extends HybridManifest {
    @Override
    protected void configure() {
        // Setup @MapScoped and bind MapDefinition as a seed object
        install(new MapInjectionScope().new Manifest());

        final FactoryModuleBuilder fmb = new FactoryModuleBuilder();
        install(fmb.build(MapFilePreprocessor.Factory.class));
        install(fmb.build(MapLogger.Factory.class));

        bind(SAXHandler.class).to(BoundedSAXHandler.class);
        bind(SAXHandlerFactory.class).toInstance(BoundedSAXHandler::new);
        bind(JDOMFactory.class).to(BoundedJDOMFactory.class);

        bind(PGMMap.Factory.class);
        bind(MapConfiguration.class).to(PGMMapConfiguration.class);
        bind(MapDoc.class).to(MapDocument.class);

        inSet(MapRootParser.class);

        bind(ValidationContext.class).to(FeatureDefinitionContext.class);

        bind(MapModuleContext.class).in(MapScoped.class);
        bind(ModuleExceptionHandler.class).to(MapModuleContext.class);

        expose(MapDefinition.class);
        expose(PGMMap.class);

        requestStaticInjection(XMLUtils.class);
    }

    @Provides
    SAXBuilder saxBuilder(SAXHandlerFactory saxHandlerFactory, JDOMFactory jdomFactory) {
        return new SAXBuilder(null, saxHandlerFactory, jdomFactory);
    }

    @Provides @MapScoped
    PGMMap pgmMap(MapDefinition map) {
        return (PGMMap) map;
    }

    @Provides @MapScoped
    MapFolder mapFolder(MapDefinition map) {
        return map.getFolder();
    }

    @Provides @MapScoped
    MapLogger mapLogger(MapDefinition map) {
        return map.getLogger();
    }

    @Provides @MapScoped @MapProto
    SemanticVersion mapProto(MapModuleContext context) {
        return context.getProto();
    }

    @Provides @MapScoped
    Document xmlDocument(MapModuleContext context) {
        return context.xmlDocument();
    }
}
