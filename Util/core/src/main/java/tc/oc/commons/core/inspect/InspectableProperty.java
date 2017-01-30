package tc.oc.commons.core.inspect;

import java.util.function.Supplier;

import tc.oc.commons.core.stream.BiStream;

public interface InspectableProperty {

    String name();

    Object value(Inspectable inspectable) throws Throwable;

    default Inspection options() {
        return Inspection.defaults();
    }

    default Object valueWrappingException(Inspectable inspectable) throws InspectionException {
        try {
            return value(inspectable);
        } catch(Throwable e) {
            throw new InspectionException(e.getClass().getSimpleName() +
                                          " inspecting '" + name() + "' property: " +
                                          e.getMessage(),
                                          e);
        }
    }

    default Object valueOrException(Inspectable inspectable) {
        try {
            return valueWrappingException(inspectable);
        } catch(InspectionException e) {
            return e;
        }
    }

    default BiStream<InspectableProperty, Object> flatValues(Inspectable inspectable) {
        Object value = options().unwrap(valueOrException(inspectable));

        if(options().inline() && value instanceof Inspectable) {
            final Inspectable child = (Inspectable) value;
            return BiStream.from(child.inspectableProperties()
                                      .flatMap(prop -> prop.flatValues(child)));
        }

        if(options().isPresent(value)) {
            return BiStream.of(this, value);
        }

        return BiStream.empty();
    }

    static InspectableProperty of(String name, Object value) {
        return new InspectableProperty() {
            @Override public String name() {
                return name;
            }

            @Override public Object value(Inspectable inspectable) throws InspectionException {
                return value;
            }
        };
    }

    static InspectableProperty reading(String name, Supplier<?> value) {
        return new InspectableProperty() {
            @Override public String name() {
                return name;
            }

            @Override public Object value(Inspectable inspectable) throws InspectionException {
                return value.get();
            }
        };
    }
}
