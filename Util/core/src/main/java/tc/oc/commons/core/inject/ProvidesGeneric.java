package tc.oc.commons.core.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.google.inject.Module;
import com.google.inject.TypeLiteral;

/**
 * @see {@link Injection#providerMethodsModule(Module, TypeLiteral)}
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ProvidesGeneric {}
