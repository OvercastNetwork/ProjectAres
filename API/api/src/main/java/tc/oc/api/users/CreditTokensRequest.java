package tc.oc.api.users;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface CreditTokensRequest extends Document {
    String type();
    int amount();

    static CreditTokensRequest raindrops(int amount) {
        return new CreditTokensRequest() {
            public String type() { return "raindrops"; }
            public int amount() { return amount; }
        };
    }

    static CreditTokensRequest maps(int amount) {
        return new CreditTokensRequest() {
            public String type() { return "maptokens"; }
            public int amount() { return amount; }
        };
    }

    static CreditTokensRequest mutations(int amount) {
        return new CreditTokensRequest() {
            public String type() { return "mutationtokens"; }
            public int amount() { return amount; }
        };
    }
}
