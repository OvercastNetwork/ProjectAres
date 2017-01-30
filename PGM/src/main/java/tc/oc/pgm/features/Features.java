package tc.oc.pgm.features;

import javax.annotation.Nullable;

import tc.oc.commons.core.reflect.ClassFormException;
import tc.oc.commons.core.reflect.Types;

public class Features {
    private Features() {}

    public static @Nullable FeatureInfo findInfo(Class<? extends FeatureDefinition> type) {
        return Types.inheritableAnnotation(type, FeatureInfo.class);
    }

    public static FeatureInfo info(Class<? extends FeatureDefinition> type) {
        final FeatureInfo info = Types.inheritableAnnotation(type, FeatureInfo.class);
        if(info == null) {
            throw new ClassFormException(type, "Can't find @FeatureInfo annotation");
        }
        return info;
    }

    public static String name(Class<? extends FeatureDefinition> type) {
        return info(type).name();
    }
}
