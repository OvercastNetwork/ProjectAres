package tc.oc.test.mockito;

import javax.inject.Provider;

import com.google.inject.TypeLiteral;
import org.mockito.Mockito;

public class MockProvider<T> implements Provider<T> {

    private final Class<T> type;

    private MockProvider(Class<T> type) {
        this.type = type;
    }

    public static <T> MockProvider<T> of(Class<T> type) {
        return new MockProvider<>(type);
    }

    public static <T> MockProvider<T> of(TypeLiteral<T> type) {
        return new MockProvider<>((Class<T>) type.getRawType());
    }

    @Override
    public T get() {
        return Mockito.mock(type);
    }
}
