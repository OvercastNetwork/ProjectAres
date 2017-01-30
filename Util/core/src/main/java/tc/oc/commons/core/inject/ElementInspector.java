package tc.oc.commons.core.inject;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.ConvertedConstantBinding;
import com.google.inject.spi.DisableCircularProxiesOption;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.ExposedBinding;
import com.google.inject.spi.InjectionRequest;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.InterceptorBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.MembersInjectorLookup;
import com.google.inject.spi.Message;
import com.google.inject.spi.ModuleAnnotatedMethodScannerBinding;
import com.google.inject.spi.PrivateElements;
import com.google.inject.spi.ProviderBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import com.google.inject.spi.ProviderLookup;
import com.google.inject.spi.ProvisionListenerBinding;
import com.google.inject.spi.RequireAtInjectOnConstructorsOption;
import com.google.inject.spi.RequireExactBindingAnnotationsOption;
import com.google.inject.spi.RequireExplicitBindingsOption;
import com.google.inject.spi.ScopeBinding;
import com.google.inject.spi.StaticInjectionRequest;
import com.google.inject.spi.TypeConverterBinding;
import com.google.inject.spi.TypeListenerBinding;
import com.google.inject.spi.UntargettedBinding;

/**
 * Generates a descriptive {@link Message} for every visited element, and passes it to {@link #visit(Message)}.
 */
public abstract class ElementInspector<V> implements ElementVisitor<V> {

    protected V message(Object source, String text) {
        return visit(new Message(source, text));
    }

    protected V message(Element element, String text) {
        return message(element.getSource(), text);
    }

    @Override
    public <T> V visit(Binding<T> binding) {
        final String text = "Binding " + binding.getKey() + " to " + binding.acceptTargetVisitor(new BindingTargetVisitor<T, String>() {
            @Override
            public String visit(InstanceBinding<? extends T> binding) {
                return "instance " + binding.getInstance();
            }

            @Override
            public String visit(ProviderInstanceBinding<? extends T> binding) {
                return "provider instance " + binding.getUserSuppliedProvider();
            }

            @Override
            public String visit(ProviderKeyBinding<? extends T> binding) {
                return "provider key " + binding.getProviderKey();
            }

            @Override
            public String visit(LinkedKeyBinding<? extends T> binding) {
                return "linked key " + binding.getLinkedKey();
            }

            @Override
            public String visit(ExposedBinding<? extends T> binding) {
                return "exposed key from private environment";
            }

            @Override
            public String visit(UntargettedBinding<? extends T> binding) {
                return "itself";
            }

            @Override
            public String visit(ConstructorBinding<? extends T> binding) {
                return "constructor " + binding.getConstructor();
            }

            @Override
            public String visit(ConvertedConstantBinding<? extends T> binding) {
                return "converted value " + binding.getValue();
            }

            @Override
            public String visit(ProviderBinding<? extends T> binding) {
                return "provider of key " + binding.getProvidedKey();
            }
        });

        return message(binding, text);

    }

    @Override
    public V visit(InterceptorBinding binding) {
        return message(binding,
                       "Binding interceptors " + binding.getInterceptors() +
                       " to methods matching " + binding.getMethodMatcher() +
                       " on classes matching " + binding.getClassMatcher());
    }

    @Override
    public V visit(ScopeBinding binding) {
        return message(binding,
                       "Binding scope annotation " + binding.getAnnotationType() +
                       " to scope " + binding.getScope());
    }

    @Override
    public V visit(TypeConverterBinding binding) {
        return message(binding,
                       "Binding type converter " + binding.getTypeConverter() +
                       " to classes matching " + binding.getTypeMatcher());
    }

    @Override
    public V visit(InjectionRequest<?> request) {
        return message(request,
                       "Requesting injection of " + request.getType() +
                       " for instance " + request.getInstance());
    }

    @Override
    public V visit(StaticInjectionRequest request) {
        return message(request,
                       "Requesting static injection for " + request.getType());
    }

    @Override
    public <T> V visit(ProviderLookup<T> lookup) {
        return message(lookup,
                       "Looking up provider " + lookup.getProvider() +
                       " for key " + lookup.getKey());
    }

    @Override
    public <T> V visit(MembersInjectorLookup<T> lookup) {
        return message(lookup,
                       "Looking up members injector " + lookup.getMembersInjector() +
                       " for type " + lookup.getType());
    }

    @Override
    public V visit(PrivateElements elements) {
        final V result = message(elements, "Binding private module");

        for(Key<?> key : elements.getExposedKeys()) {
            message(elements.getExposedSource(key),
                    "    Exposing key " + key);
        }

        for(Element element : elements.getElements()) {
            element.acceptVisitor(new ElementInspector<V>() {
                @Override
                public V visit(Message message) {
                    return ElementInspector.this.visit(new Message(message.getSources(),
                                                                   "    " + message.getMessage(),
                                                                   message.getCause()));
                }
            });
        }

        return result;
    }

    @Override
    public V visit(TypeListenerBinding binding) {
        return message(binding,
                       "Binding type listener " + binding.getListener() +
                       " for types matching " + binding.getTypeMatcher());
    }

    @Override
    public V visit(ProvisionListenerBinding binding) {
        return message(binding,
                       "Binding provision listeners " + binding.getListeners() +
                       " for bindings matching " + binding.getBindingMatcher());
    }

    @Override
    public V visit(RequireExplicitBindingsOption option) {
        return message(option, "Requiring explicit bindings");
    }

    @Override
    public V visit(DisableCircularProxiesOption option) {
        return message(option, "Disabling circular proxies");
    }

    @Override
    public V visit(RequireAtInjectOnConstructorsOption option) {
        return message(option, "Requiring explicit @Inject on constructors");
    }

    @Override
    public V visit(RequireExactBindingAnnotationsOption option) {
        return message(option, "Requiring exact binding annotations");
    }

    @Override
    public V visit(ModuleAnnotatedMethodScannerBinding binding) {
        return message(binding,
                       "Scanning modules for methods annotated with " + binding.getScanner().annotationClasses());
    }
}
