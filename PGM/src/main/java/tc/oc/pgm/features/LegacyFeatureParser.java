package tc.oc.pgm.features;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.inject.TypeLiteral;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.pgm.map.MapProto;
import tc.oc.pgm.map.ProtoVersions;

/**
 * Adds support for < proto 1.4 feature parsing
 */
public class LegacyFeatureParser<T extends FeatureDefinition> extends FeatureParser<T> {

    protected boolean legacy;

    protected LegacyFeatureParser() {}

    @Inject public LegacyFeatureParser(@Nullable TypeLiteral<T> type) {
        super(type);
    }

    @Inject private void init(@MapProto SemanticVersion proto) {
        this.legacy = proto.isOlderThan(ProtoVersions.FILTER_FEATURES);
    }

    @Override
    public String idAttributeName() {
        return legacy ? "name"
                      : super.idAttributeName();
    }

    @Override
    public String mangleId(String unmangled) {
        return legacy ? "--" + propertyName() + "-" + unmangled.toLowerCase().replaceAll("\\s+", "-")
                      : super.mangleId(unmangled);
    }
}
