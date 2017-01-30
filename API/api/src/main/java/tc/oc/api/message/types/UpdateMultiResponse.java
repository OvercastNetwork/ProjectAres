package tc.oc.api.message.types;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;

import static com.google.common.base.Preconditions.checkNotNull;

public class UpdateMultiResponse implements Reply {
    @Serialize public int created;
    @Serialize public int updated;
    @Serialize public int skipped;
    @Serialize public int failed;

    // _id -> property -> messages[]
    @Serialize public Map<String, Map<String, List<String>>> errors;

    protected UpdateMultiResponse() {}

    protected UpdateMultiResponse(int created, int updated, int skipped, int failed, Map<String, Map<String, List<String>>> errors) {
        this.created = created;
        this.updated = updated;
        this.skipped = skipped;
        this.failed = failed;
        this.errors = checkNotNull(errors);
    }

    public static final UpdateMultiResponse EMPTY = new UpdateMultiResponse(0, 0, 0, 0, Collections.emptyMap());

    @Override
    public boolean success() {
        return failed <= 0;
    }

    @Override
    public @Nullable String error() {
        return success() ? null : formattedErrors();
    }

    private transient String formattedErrors;
    public String formattedErrors() {
        if(formattedErrors == null) {
            StringBuilder text = new StringBuilder(failed + " documents failed:\n");
            for(Map.Entry<String, Map<String, List<String>>> document : errors.entrySet()) {
                text.append("  ").append(document.getKey()).append(" :\n");
                for(Map.Entry<String, List<String>> property : document.getValue().entrySet()) {
                    for(String problem : property.getValue()) {
                        text.append("    ").append(property.getKey()).append(" ").append(problem);
                    }
                }
            }
            formattedErrors = text.toString();
        }
        return formattedErrors;
    }

    @Override
    public String toString() {
        return created + " created, " +
               updated + " updated, " +
               skipped + " skipped, " +
               failed + " failed";
    }
}
