package tc.oc.test.mockito;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class MockBinder {

    private final Binder binder;

    public MockBinder(Binder binder) {
        this.binder = binder;
    }

    public <T> void mock(Key<T> key) {
        binder.bind(key).toProvider(MockProvider.of(key.getTypeLiteral()));
    }

    public <T> void mock(TypeLiteral<T> type) {
        binder.bind(type).toProvider(MockProvider.of(type));
    }

    public <T> void mock(Class<T> type) {
        binder.bind(type).toProvider(MockProvider.of(type));
    }
}
