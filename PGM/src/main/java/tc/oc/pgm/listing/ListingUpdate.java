package tc.oc.pgm.listing;

import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface ListingUpdate extends Document {

    @Nullable String host();

    int port();

    boolean online();

    String session();
}
