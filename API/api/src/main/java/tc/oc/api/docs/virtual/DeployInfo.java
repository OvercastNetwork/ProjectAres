package tc.oc.api.docs.virtual;

import java.util.Map;

import tc.oc.api.annotations.Serialize;

@Serialize
public interface DeployInfo extends Document {
    @Serialize
    interface Version extends Document {
        String branch();
        String commit();
    }

    @Serialize
    interface Nextgen extends Document {
        String path();
        Version version();
    }

    Nextgen nextgen();
    Map<String, Version> packages();
}
