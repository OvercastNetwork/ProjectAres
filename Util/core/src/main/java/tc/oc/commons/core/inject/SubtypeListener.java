package tc.oc.commons.core.inject;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

public interface SubtypeListener<I> {
    void hear(TypeLiteral<I> type, TypeEncounter<I> encounter);
}
