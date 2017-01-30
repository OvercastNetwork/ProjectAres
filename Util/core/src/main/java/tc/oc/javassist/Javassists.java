package tc.oc.javassist;

import java.util.Objects;
import javax.annotation.Nullable;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.scopedpool.ScopedClassPoolRepositoryImpl;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Helpers for use with the Javassist bytecode generation library
 */
public class Javassists {

    /**
     * Return a {@link ClassPool} scoped to the given {@link ClassLoader}.
     *
     * This is essential in a Bukkit/Bungee environment. The default
     * ClassPool is practically useless, since it does not have access to
     * the private plugin ClassLoaders.
     */
    public static ClassPool getPool(ClassLoader loader) {
        return ScopedClassPoolRepositoryImpl.getInstance().registerClassLoader(loader);
    }

    public static CtClass getClass(Class<?> cls) throws NotFoundException {
        return getPool(cls.getClassLoader()).get(cls.getName());
    }

    public static AttributeInfo inPool(AttributeInfo attr, ConstPool pool) {
        return pool.equals(attr.getConstPool()) ? attr
                                                : attr.copy(pool, null);
    }

    public static CtMethod getMethod(CtClass decl, CtMethod method) throws NotFoundException {
        return decl.getMethod(method.getName(), method.getSignature());
    }

    public static @Nullable CtMethod tryMethod(CtClass decl, CtMethod method) {
        try {
            return getMethod(decl, method);
        } catch(NotFoundException e) {
            return null;
        }
    }

    public static AttributeInfo addAttribute(MethodInfo to, AttributeInfo attr) {
        attr = inPool(attr, to.getConstPool());
        to.addAttribute(attr);
        return attr;
    }

    public static @Nullable AttributeInfo copyAttribute(MethodInfo from, MethodInfo to, String attributeName) {
        final AttributeInfo attr = from.getAttribute(attributeName);
        if(attr != null) {
            addAttribute(to, attr);
        }
        return attr;
    }

    public static void copyAnnotations(MethodInfo from, MethodInfo to) {
        copyAttribute(from, to, AnnotationsAttribute.invisibleTag);
        copyAttribute(from, to, AnnotationsAttribute.visibleTag);
        copyAttribute(from, to, ParameterAnnotationsAttribute.invisibleTag);
        copyAttribute(from, to, ParameterAnnotationsAttribute.visibleTag);
    }

    public static boolean isSubtype(CtClass base, CtClass sub) throws NotFoundException {
        if(sub == null) return false;
        if(base.equals(sub)) return true;
        if(isSubtype(base, sub.getSuperclass())) return true;
        for(CtClass iface : sub.getInterfaces()) {
            if(isSubtype(base, iface)) return true;
        }
        return false;
    }

    public static boolean isPrivate(CtBehavior method) {
        return Modifier.isPrivate(method.getModifiers());
    }

    public static boolean isAbstract(CtBehavior method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    public static boolean isFinal(CtBehavior method) {
        return Modifier.isFinal(method.getModifiers());
    }

    public static void setModifier(CtBehavior method, int mask, boolean value) {
        method.setModifiers(value ? method.getModifiers() | mask
                                  : method.getModifiers() & ~mask);
    }

    public static final int ACCESS_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;

    public static int getAccessModifiers(int modifiers) {
        return modifiers & ACCESS_MODIFIERS;
    }

    public static void setAccessModifiers(CtClass cls, int modifiers) {
        cls.setModifiers((cls.getModifiers() & ~ACCESS_MODIFIERS) | (modifiers & ACCESS_MODIFIERS));
    }

    public static void setAccessModifiers(CtBehavior method, int modifiers) {
        method.setModifiers((method.getModifiers() & ~ACCESS_MODIFIERS) | (modifiers & ACCESS_MODIFIERS));
    }

    public static boolean isAccessible(CtBehavior accessible, CtClass from) throws NotFoundException {
        return isAccessible(accessible.getDeclaringClass(), accessible.getModifiers(), from);
    }

    public static boolean isAccessible(CtClass accessible, CtClass from) throws NotFoundException {
        return isAccessible(accessible, accessible.getModifiers(), from);
    }

    /**
     * TODO: This doesn't handle every case yet
     */
    public static boolean isAccessible(CtClass accessible, int modifiers, CtClass from) throws NotFoundException {
        final CtClass decl = accessible.getDeclaringClass();
        if(decl != null && !isAccessible(decl, from)) return false;

        if(Modifier.isPrivate(modifiers)) {
            return from.equals(accessible);
        } else if(Modifier.isProtected(modifiers)) {
            return isSubtype(accessible, from);
        } else if(Modifier.isPublic(modifiers)) {
            return true;
        } else {
            return Objects.equals(accessible.getPackageName(), from.getPackageName());
        }
    }

    public static CtConstructor inheritConstructor(CtClass subclass, CtConstructor constructor) throws NotFoundException, CannotCompileException {
        checkArgument(Objects.equals(subclass.getSuperclass(), constructor.getDeclaringClass()),
                      "Constructor " + constructor.getLongName() +
                      " is not declared in " + subclass.getSuperclass().getName() +
                      ", the superclass of " + subclass.getName());
        checkArgument(isAccessible(constructor, subclass),
                      "Constructor " + constructor.getLongName() + " is not accessible from " + subclass.getName());


        final CtConstructor delegator = CtNewConstructor.make(constructor.getParameterTypes(),
                                                              constructor.getExceptionTypes(),
                                                              CtNewConstructor.PASS_PARAMS,
                                                              null, null,
                                                              subclass);

        // Must use getMethodInfo2 on the original ctor because the class is frozen
        copyAnnotations(constructor.getMethodInfo2(), delegator.getMethodInfo());
        subclass.addConstructor(delegator);
        return delegator;
    }

    public static void inheritAllConstructors(CtClass subclass) throws NotFoundException, CannotCompileException {
        final CtClass superclass = subclass.getSuperclass();
        if(superclass != null) {
            for(CtConstructor ctor : superclass.getConstructors()) {
                if(!isPrivate(ctor)) {
                    inheritConstructor(subclass, ctor);
                }
            }
        }
    }

    public static CtMethod delegateMethod(CtClass implClass, CtMethod method, CtMethod delegateGetter) throws NotFoundException, CannotCompileException {
        checkArgument(!Modifier.isStatic(method.getModifiers()),
                      "Cannot delegate static method " + method.getLongName());
        checkArgument(!Modifier.isStatic(delegateGetter.getModifiers()),
                      "Delegate getter method " + delegateGetter.getLongName() + " must not be static");
        checkArgument(isSubtype(delegateGetter.getDeclaringClass(), implClass),
                      "Implementation class " + implClass + " does not contain delegate getter method " + delegateGetter.getLongName());
        checkArgument(delegateGetter.getParameterTypes().length == 0,
                      "Delegate getter method " + delegateGetter.getLongName() + " must not take any parameters");
        checkArgument(!delegateGetter.getReturnType().isPrimitive(),
                      "Delegate getter method " + delegateGetter.getLongName() + " must return an object");

        try {
            final CtMethod conflict = implClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
            throw new IllegalArgumentException("Class " + implClass.getName() +
                                               " already contains a method " + conflict.getLongName() +
                                               " that conflicts with the delegated method " + method.getLongName());
        } catch(NotFoundException ignored) {}

        final String name = method.getName();
        final CtClass[] paramTypes = method.getParameterTypes();
        final CtClass returnType = method.getReturnType();
        final MethodInfo info = method.getMethodInfo2(); // Must be read-only
        final String descriptor = info.getDescriptor();
        final CtClass decl = method.getDeclaringClass();

        final Bytecode bc = new Bytecode(implClass.getClassFile().getConstPool());
        int maxStack = 1;
        bc.addAload(0); // push this
        bc.addInvokeinterface(delegateGetter.getDeclaringClass(), delegateGetter.getName(), delegateGetter.getReturnType(), null, 1); // get delegate
        bc.addCheckcast(decl); // make the verifier happy
        maxStack += bc.addLoadParameters(paramTypes, 1); // push all params
        if(decl.isInterface()) { // call the method on delegate
            bc.addInvokeinterface(decl, name, descriptor, 1 + paramTypes.length);
        } else {
            bc.addInvokevirtual(decl, name, descriptor);
        }
        bc.addReturn(returnType); // forward the return value

        bc.setMaxLocals(false, paramTypes, 0);
        bc.setMaxStack(Math.max(2, maxStack)); // return value might be up to 2 stack ops

        final CtMethod delegator = CtNewMethod.copy(method, implClass, null);
        setAccessModifiers(delegator, method.getModifiers());
        setModifier(delegator, Modifier.ABSTRACT, false);
        setModifier(delegator, Modifier.NATIVE, false);
        delegator.getMethodInfo().setCodeAttribute(bc.toCodeAttribute());
        implClass.addMethod(delegator);
        return delegator;
    }
}
