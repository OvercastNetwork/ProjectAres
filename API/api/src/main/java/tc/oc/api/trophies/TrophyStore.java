package tc.oc.api.trophies;

import tc.oc.api.docs.Trophy;
import tc.oc.api.model.ModelStore;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class TrophyStore extends ModelStore<Trophy> {

    private final Map<String, Trophy> byName = new HashMap<>();

    @Override
    protected void reindex(Trophy doc) {
        super.reindex(doc);
        byName.put(doc.name(), doc);
    }

    @Override
    protected void unindex(Trophy doc) {
        super.unindex(doc);
        byName.remove(doc.name());
    }

    public Optional<Trophy> byName(String name) {
        return Optional.ofNullable(byName.get(name));
    }
}
