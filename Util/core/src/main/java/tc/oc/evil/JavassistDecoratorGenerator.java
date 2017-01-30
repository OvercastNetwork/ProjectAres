package tc.oc.evil;

import java.util.Set;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import tc.oc.javassist.Javassists;

/**
 * This never worked properly, and was abandoned in favor of {@link LibCGDecoratorGenerator}
 */
public class JavassistDecoratorGenerator implements DecoratorGenerator {

    private final CtMethod delegateMethod;
    private final Set<CtMethod> forbiddenMethods;

    public JavassistDecoratorGenerator() {
        try {
            this.delegateMethod = Javassists.getClass(Decorator.class).getDeclaredMethod("delegate", null);
            final CtClass object = Javassists.getClass(Object.class);
            this.forbiddenMethods = ImmutableSet.of(
                object.getDeclaredMethod("finalize", null),
                object.getDeclaredMethod("clone", null)
            );
        } catch(NotFoundException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public <T, D extends Decorator<T>> Meta<T, D> implement(Class<T> type, Class<D> decorator) {
        try {
            // If the decorator is not abstract, there will be no delegating methods to generate,
            // so we don't need a subclass at all.
            if(!Modifier.isAbstract(decorator.getModifiers())) {
                return new Meta<>(type, decorator, decorator);
            }

            final CtClass originalClass = Javassists.getClass(type);
            final ClassPool decoratorPool = Javassists.getPool(decorator.getClassLoader());
            final CtClass decoratorClass = decoratorPool.get(decorator.getName());
            final CtClass implClass = decoratorPool.makeClass(decorator.getName() + "$Impl", decoratorClass);
            Javassists.setAccessModifiers(implClass, decoratorClass.getModifiers());

            for(CtMethod decoratorMethod : decoratorClass.getMethods()) {
                if(Modifier.isStatic(decoratorMethod.getModifiers())) continue;
                if(forbiddenMethods.contains(decoratorMethod)) continue;

                final CtMethod originalMethod = Javassists.tryMethod(originalClass, decoratorMethod);
                final boolean decoratorAbstract = Javassists.isAbstract(decoratorMethod);

                if(decoratorMethod.equals(delegateMethod)) {
                    // If we find the delegate() method, just check that it's callable
                    // CtMethod.equals only compares the signature, so this will work
                    if(decoratorAbstract) {
                        throw new IllegalStateException("Decorator class must implement method " + delegateMethod.getLongName());
                    }
                } else if(originalMethod != null &&
                          !Javassists.isFinal(decoratorMethod) &&
                          (decoratorAbstract || originalMethod.getDeclaringClass().equals(decoratorMethod.getDeclaringClass()))) {
                    // If the method was found in the original class, and the decorator
                    // does not override it, delegate to the original.
                    Javassists.delegateMethod(implClass, decoratorMethod, delegateMethod);
                } else if(originalMethod == null && decoratorAbstract) {
                    // If the method is not in the original class at all, and the
                    // decorator doesn't implement it, then we can't implement the class
                    throw new IllegalStateException("No implementation for method " + decoratorMethod.getLongName() +
                                                    " found in either the decorator or the decorated class");
                }
            }

            // Inherit all accessible constructors, including their annotations
            Javassists.inheritAllConstructors(implClass);

            return new Meta<>(type, decorator, implClass.toClass());

        } catch(NotFoundException | CannotCompileException e) {
            throw Throwables.propagate(e);
        }
    }
}
