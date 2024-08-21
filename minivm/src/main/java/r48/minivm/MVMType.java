/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import datum.DatumSymbol;
import r48.minivm.fn.MVMFn;

/**
 * A type in the MiniVM type system.
 * Created 17th May, 2024.
 */
public abstract class MVMType {
    /**
     * Class map.
     */
    private static final ConcurrentHashMap<Class<?>, MVMType> classMap = new ConcurrentHashMap<>();
    /**
     * 'ANY' is the unknown type.
     * Importantly, 'ANY' can be implicitly casted to any other type!
     */
    public static final MVMType ANY = new MVMType() {
        @Override
        protected boolean canImplicitlyCastFromImpl(MVMType other) {
            return true;
        }
        @Override
        protected boolean canImplicitlyCastToImpl(MVMType other) {
            return true;
        }
        @Override
        public String toString() {
            return "type.any";
        }
    };
    /**
     * The type of Object, to which any non-null value may be assigned.
     */
    public static final MVMType OBJ = typeOfClass(Object.class);
    /**
     * The type of List.
     */
    public static final MVMType LIST = typeOfClass(List.class);
    /**
     * The type of Long.
     */
    public static final MVMType I64 = typeOfClass(Long.class);
    /**
     * The type of Double.
     */
    public static final MVMType F64 = typeOfClass(Double.class);
    /**
     * The type of String.
     */
    public static final MVMType STR = typeOfClass(String.class);
    /**
     * The type of DatumSymbol.
     */
    public static final MVMType SYM = typeOfClass(DatumSymbol.class);
    /**
     * The type of Boolean.
     */
    public static final MVMType BOOL = typeOfClass(Boolean.class);
    /**
     * The type of Boolean.
     */
    public static final MVMType FN = typeOfClass(MVMFn.class);
    /**
     * Double or Long
     */
    public static final MVMType NUM = Union.of(I64, F64, true);
    /**
     * The type of MVMEnv.
     */
    public static final MVMType ENV = typeOfClass(MVMEnv.class);
    /**
     * The type of char.
     */
    public static final MVMType CHAR = typeOfClass(Character.class);

    /**
     * Gets the MVMType of a value.
     */
    public static MVMType typeOf(@Nullable Object v) {
        if (v == null)
            return ANY;
        if (v instanceof IMVMTypable)
            return ((IMVMTypable) v).getMVMType();
        return typeOfClass(v.getClass());
    }

    /**
     * Gets the MVMType of a class.
     */
    public static MVMType typeOfClass(@NonNull Class<?> cls) {
        if (cls == boolean.class)
            return BOOL;
        if (cls == long.class)
            return I64;
        if (cls == double.class)
            return F64;
        if (cls == char.class)
            return CHAR;
        // fast-path
        MVMType typeA = classMap.get(cls);
        if (typeA != null)
            return typeA;
        // now get a write lock
        synchronized (classMap) {
            typeA = classMap.get(cls);
            if (typeA != null)
                return typeA;
            typeA = new Clazz(cls);
            classMap.put(cls, typeA);
            return typeA;
        }
    }

    public final void assertCanImplicitlyCastTo(@NonNull MVMType type, Object context) {
        if (!canImplicitlyCastTo(type))
            throw new RuntimeException("Cannot implicitly cast " + this + " to " + type + " at " + context);
    }

    public final void assertCanImplicitlyCastFrom(@NonNull MVMType type, Object context) {
        if (!canImplicitlyCastFrom(type))
            throw new RuntimeException("Cannot implicitly cast " + type + " to " + this + " at " + context);
    }

    /**
     * If this MVMType can be cast from another MVMType.
     */
    public final boolean canImplicitlyCastFrom(@NonNull MVMType other) {
        if (other == this)
            return true;
        return canImplicitlyCastFromImpl(other) || other.canImplicitlyCastToImpl(this);
    }

    /**
     * If this MVMType can be cast to another MVMType.
     */
    public final boolean canImplicitlyCastTo(@NonNull MVMType other) {
        if (other == this)
            return true;
        return canImplicitlyCastToImpl(other) || other.canImplicitlyCastFromImpl(this);
    }

    protected abstract boolean canImplicitlyCastFromImpl(@NonNull MVMType other);

    protected abstract boolean canImplicitlyCastToImpl(@NonNull MVMType other);

    /**
     * Represents a type for a Java class.
     */
    public static class Clazz extends MVMType {
        public final Class<?> cls;

        private Clazz(Class<?> cls) {
            this.cls = cls;
        }

        @Override
        public int hashCode() {
            return cls.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj.getClass() == Clazz.class) {
                Clazz clz = (Clazz) obj;
                return clz.cls.equals(cls);
            }
            return false;
        }

        @Override
        protected boolean canImplicitlyCastFromImpl(MVMType other) {
            if (other instanceof Clazz)
                return cls.isAssignableFrom(((Clazz) other).cls);
            return false;
        }

        @Override
        protected boolean canImplicitlyCastToImpl(MVMType other) {
            if (other instanceof Clazz)
                return ((Clazz) other).cls.isAssignableFrom(cls);
            return false;
        }

        @Override
        public String toString() {
            return "type." + cls.getSimpleName();
        }
    }

    /**
     * Represents List of X.
     */
    public static final class TypedList extends Clazz {
        public final MVMType content;

        public TypedList(MVMType content) {
            super(List.class);
            this.content = content;
        }

        @Override
        public int hashCode() {
            return content.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj instanceof TypedList) {
                TypedList lt = (TypedList) obj;
                return lt.content.equals(content);
            }
            return false;
        }

        @Override
        protected boolean canImplicitlyCastFromImpl(MVMType other) {
            if (other instanceof TypedList)
                return content.canImplicitlyCastFrom(((TypedList) other).content);
            return false;
        }

        @Override
        protected boolean canImplicitlyCastToImpl(MVMType other) {
            if (other instanceof TypedList)
                return content.canImplicitlyCastTo(((TypedList) other).content);
            return false;
        }

        @Override
        public String toString() {
            return "list." + content;
        }
    }

    /**
     * Union of two types; can be either type.
     * A forgiving union can be cast from either type or to either type implicitly.
     */
    public static final class Union extends MVMType {
        public final MVMType a, b;
        public final boolean forgiving;

        private Union(MVMType a, MVMType b, boolean f) {
            this.a = a;
            this.b = b;
            forgiving = f;
        }

        /**
         * Creates unions with auto-collapsing.
         */
        public static MVMType of(MVMType a, MVMType b, boolean forgiving) {
            if (!forgiving) {
                if (a.canImplicitlyCastFrom(b))
                    return a;
                if (b.canImplicitlyCastFrom(a))
                    return b;
            } else if (a.canImplicitlyCastFrom(b) && b.canImplicitlyCastFrom(a))
                return a;
            return new Union(a, b, forgiving);
        }

        @Override
        public boolean equals(Object obj) {
            // this will have to be "good enough" for effective equivalence
            if (obj instanceof Union)
                if (canImplicitlyCastFrom((MVMType) a) && canImplicitlyCastTo((MVMType) a))
                    return true;
            return false;
        }

        @Override
        public int hashCode() {
            return a.hashCode() ^ b.hashCode();
        }

        @Override
        protected boolean canImplicitlyCastFromImpl(MVMType other) {
            return a.canImplicitlyCastFrom(other) || b.canImplicitlyCastFrom(other);
        }

        @Override
        protected boolean canImplicitlyCastToImpl(MVMType other) {
            if (forgiving)
                return a.canImplicitlyCastTo(other) || b.canImplicitlyCastTo(other);
            return a.canImplicitlyCastTo(other) && b.canImplicitlyCastTo(other);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(a);
            sb.append("|");
            sb.append(b);
            return sb.toString();
        }
    }

    /**
     * Function type. Uhoh!
     */
    public static final class Fn extends Clazz {
        public final @NonNull MVMType returnType;
        public final int minArgs;
        public final @NonNull MVMType[] args;
        public final @Nullable MVMType vaType;

        /**
         * Return type only.
         */
        public Fn(@NonNull MVMType returnType) {
            this(returnType, 0, new MVMType[0], MVMType.ANY);
        }

        public Fn(@NonNull MVMType returnType, int ma, @NonNull MVMType[] args, @Nullable MVMType vaType) {
            super(MVMFn.class);
            this.returnType = returnType;
            this.minArgs = ma;
            this.args = args;
            this.vaType = vaType;
        }

        /**
         * Any simple case goes here.
         */
        public static Fn simple(MVMType res, MVMType... args) {
            return new Fn(res, args.length, args, null);
        }

        @Override
        public int hashCode() {
            return 2;
        }

        @Override
        public boolean equals(Object obj) {
            // just give up
            return this == obj;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("fn[");
            for (MVMType t : args) {
                sb.append(t);
                sb.append(",");
            }
            if (vaType != null) {
                sb.append(vaType);
                sb.append("...");
            }
            sb.append("]->");
            sb.append(returnType);
            return sb.toString();
        }

        /**
         * Gets the type of an arg, or null if off the end
         */
        public @Nullable MVMType argAt(int idx) {
            if (idx < args.length)
                return args[idx];
            return vaType;
        }

        @Override
        protected boolean canImplicitlyCastFromImpl(MVMType other) {
            if (other instanceof Fn) {
                Fn o = (Fn) other;
                // return type is the most "obvious" one
                if (!returnType.canImplicitlyCastFrom(o.returnType))
                    return false;
                // if it requires more args, it won't work
                if (((Fn) other).minArgs > minArgs)
                    return false;
                // figure out args by figuring out a "window"
                // past this window, we know that it will extend infinitely
                int relevantArgCount = args.length;
                if (o.args.length > relevantArgCount)
                    relevantArgCount = o.args.length;
                relevantArgCount++;
                for (int i = 0; i < relevantArgCount; i++) {
                    MVMType us = argAt(i);
                    MVMType them = o.argAt(i);
                    // if we ran out of args first, but their minArgs is still lower than ours, we're still fine
                    if (us == null)
                        break;
                    // if they ran out of args first, it's possible we get given more args than they can handle
                    if (them == null)
                        return false;
                    if (!them.canImplicitlyCastFrom(us))
                        return false;
                }
                return true;
            }
            return false;
        }

        @Override
        protected boolean canImplicitlyCastToImpl(MVMType other) {
            if (other instanceof Fn)
                return other.canImplicitlyCastFromImpl(this);
            return false;
        }
    }

    /**
     * A subtype.
     * This requires explicit casting to "enter" the subtype, though it can be implicitly "left".
     */
    public static final class Subtype extends MVMType {
        public final MVMType baseType;
        public final String name;

        public Subtype(MVMType mt, String name) {
            this.baseType = mt;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        protected boolean canImplicitlyCastFromImpl(MVMType other) {
            return false;
        }

        @Override
        protected boolean canImplicitlyCastToImpl(MVMType other) {
            return other.canImplicitlyCastFrom(baseType);
        }
    }
}
