package tc.oc.evil;

public interface DecoratorGenerator {

    <T, D extends Decorator<T>> Meta<T, D> implement(Class<T> type, Class<D> decorator);

    class Meta<T, D extends Decorator<T>> {
        public final Class<T> type;
        public final Class<D> decorator;
        public final Class<? extends D> implementation;

        public Meta(Class<T> type, Class<D> decorator, Class<? extends D> implementation) {
            this.type = type;
            this.decorator = decorator;
            this.implementation = implementation;
        }

        public D newInstance() throws Exception {
            return implementation.newInstance();
        }

        public D newInstance(Class<?>[] parameterTypes, Object[] arguments) throws Exception {
            return implementation.getConstructor(parameterTypes).newInstance(arguments);
        }

        @Override
        public int hashCode() {
            return decorator.hashCode();
        }

        @Override
        public boolean equals(Object that) {
            return this == that || (that instanceof Meta && decorator.equals(((Meta) that).decorator));
        }
    }
}
