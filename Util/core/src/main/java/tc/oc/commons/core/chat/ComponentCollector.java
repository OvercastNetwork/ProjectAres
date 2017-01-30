package tc.oc.commons.core.chat;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.chat.BaseComponent;

public class ComponentCollector implements Collector<BaseComponent, Component, Component> {

    @Override
    public Supplier<Component> supplier() {
        return Component::new;
    }

    @Override
    public BiConsumer<Component, BaseComponent> accumulator() {
        return Component::extra;
    }

    @Override
    public BinaryOperator<Component> combiner() {
        return Component::extra;
    }

    @Override
    public Function<Component, Component> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return ImmutableSet.of();
    }
}
