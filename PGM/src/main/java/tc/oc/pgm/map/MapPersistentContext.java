package tc.oc.pgm.map;

import com.google.common.collect.Range;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.modules.InfoModule;

import javax.inject.Inject;

/**
 * This is a replacement for classes that need information from MapModuleContext to use. MapModuleContext will not be
 * guaranteed to be available if a match isn't being played for the map, but MapPersistentContext will always contain
 * basic information required to reference the map (like map name, authors, or player amount).
 */
@MapScoped
public class MapPersistentContext {

    @Inject private @MapProto SemanticVersion proto;
    @Inject private InfoModule infoModule;
    private Range<Integer> playerLimits;
    private MapDocument apiDocument;

    @Inject public MapPersistentContext(MapModuleContext context) {
        this.playerLimits = context.playerLimits();
        this.apiDocument = context.apiDocument();
    }

    public MapDocument apiDocument() {
        return apiDocument;
    }

    public SemanticVersion getProto() {
        return proto;
    }

    public Range<Integer> playerLimits() {
        return this.playerLimits;
    }

    public Integer playerLimitAverage() {
        Range<Integer> lims = playerLimits();
        return (lims.lowerEndpoint() + lims.upperEndpoint()) / 2;
    }

    public InfoModule getInfoModule() {
        return infoModule;
    }

}
