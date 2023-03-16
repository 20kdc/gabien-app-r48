/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import gabien.datum.DatumSymbol;
import gabien.datum.DatumWriter;

/**
 * Common complicated casts and things.
 * Created 2nd March 2023.
 */
public class MVMU {
    /**
     * Ensure the object is a long.
     */
    public static long cLong(Object v) {
        Number n = (Number) v;
        // How bytes, shorts, and characters get into this code is left as an exercise for the reader.
        if (n instanceof Byte || n instanceof Short || n instanceof Integer || n instanceof Long)
            return n.longValue();
        throw new RuntimeException(n + " not an integer");
    }

    /**
     * Ensure the object is an integer.
     */
    public static int cInt(Object v) {
        Number n = (Number) v;
        // How bytes, shorts, and characters get into this code is left as an exercise for the reader.
        if (n instanceof Byte || n instanceof Short || n instanceof Integer || n instanceof Long)
            return n.intValue();
        throw new RuntimeException(n + " not an integer");
    }

    /**
     * Ensure the object is a list.
     */
    @SuppressWarnings("unchecked")
    public static List<Object> cList(Object o) {
        return (List<Object>) o;
    }
    /**
     * User-readable string at any cost, do not use for serialization!
     */
    public static String userStr(Object v) {
        try {
            return DatumWriter.objectToString(v);
        } catch (Exception ex) {
            if (v == null) {
                return "?congratulations, it broke";
            } else {
                try {
                    return "?" + v.toString();
                } catch (Exception ex2) {
                    return "?toString error in " + v.getClass();
                }
            }
        }
    }

    /**
     * Allows either a symbol or a string as a string, or uses toString otherwise.
     * This is strictly speaking only defined for symbols, strings, and numbers.
     */
    public static String coerceToString(Object object) {
        if (object instanceof String)
            return (String) object;
        if (object instanceof DatumSymbol)
            return ((DatumSymbol) object).id;
        return object.toString();
    }

    /**
     * Creates a LinkedList with the given elements.
     */
    public static LinkedList<Object> l() {
        return new LinkedList<>();
    }

    /**
     * Creates a LinkedList with the given elements.
     */
    public static LinkedList<Object> l(Object a) {
        LinkedList<Object> ll = new LinkedList<>();
        ll.add(a);
        return ll;
    }

    /**
     * Creates a LinkedList with the given elements.
     */
    public static LinkedList<Object> l(Object a, Object b) {
        LinkedList<Object> ll = new LinkedList<>();
        ll.add(a);
        ll.add(b);
        return ll;
    }

    /**
     * Creates a LinkedList with the given elements.
     */
    public static LinkedList<Object> l(Object a, Object b, Object c) {
        LinkedList<Object> ll = new LinkedList<>();
        ll.add(a);
        ll.add(b);
        ll.add(c);
        return ll;
    }

    /**
     * Creates a LinkedList with the given elements.
     */
    public static LinkedList<Object> l(Object a, Object b, Object c, Object d) {
        LinkedList<Object> ll = new LinkedList<>();
        ll.add(a);
        ll.add(b);
        ll.add(c);
        ll.add(d);
        return ll;
    }

    /**
     * Creates a LinkedList with the given elements.
     */
    public static LinkedList<Object> l(Object a, Object b, Object c, Object d, Object... e) {
        LinkedList<Object> ll = new LinkedList<>();
        ll.add(a);
        ll.add(b);
        ll.add(c);
        ll.add(d);
        for (Object obj : e)
            ll.add(obj);
        return ll;
    }

    /**
     * Creates a LinkedList from an array.
     */
    public static LinkedList<Object> lArr(Object[] args) {
        LinkedList<Object> ll = new LinkedList<>();
        for (Object o : args)
            ll.add(o);
        return ll;
    }

    /**
     * Creates a LinkedList from part of an array.
     */
    public static LinkedList<Object> lArr(Object[] args, int base, int length) {
        LinkedList<Object> ll = new LinkedList<>();
        for (int i = 0; i < length; i++)
            ll.add(args[base + i]);
        return ll;
    }

    /**
     * Creates a LinkedList from fixed elements and an array.
     */
    public static LinkedList<Object> lArr(Object a, Object[] args) {
        LinkedList<Object> ll = new LinkedList<>();
        ll.add(a);
        for (Object o : args)
            ll.add(o);
        return ll;
    }

    /**
     * Creates a LinkedList from fixed elements and an array.
     */
    public static LinkedList<Object> lArr(Object a, Object b, Object[] args) {
        LinkedList<Object> ll = new LinkedList<>();
        ll.add(a);
        ll.add(b);
        for (Object o : args)
            ll.add(o);
        return ll;
    }

    /**
     * Creates a LinkedList from fixed elements and an array.
     */
    public static LinkedList<Object> lArr(Object a, Object b, Object c, Object[] args) {
        LinkedList<Object> ll = new LinkedList<>();
        ll.add(a);
        ll.add(b);
        ll.add(c);
        for (Object o : args)
            ll.add(o);
        return ll;
    }

    /**
     * Creates a LinkedList from fixed elements and an array.
     */
    public static LinkedList<Object> lArr(Object a, Object b, Object c, Object d, Object[] args) {
        LinkedList<Object> ll = new LinkedList<>();
        ll.add(a);
        ll.add(b);
        ll.add(c);
        ll.add(d);
        for (Object o : args)
            ll.add(o);
        return ll;
    }

    /**
     * If something is truthy.
     */
    public static boolean isTruthy(Object v) {
        return !((v instanceof Boolean) && (false == (Boolean) v));
    }

    /**
     * If something is truthy.
     */
    public static boolean isFalsy(Object v) {
        return (v instanceof Boolean) && (false == (Boolean) v);
    }

    /**
     * Implements the eq? operation.
     * For sanity's sake, these are implemented to roughly act like Guile.
     */
    public static boolean eqQ(Object a, Object b) {
        // pretend that valuetypes are directly comparable
        if (a instanceof Number) {
            // note that this is distinct from = behaviour, which considers 1 and 1.0 the same number
            // for integer numbers
            if ((a instanceof Byte || a instanceof Short || a instanceof Integer || a instanceof Long) && (b instanceof Byte || b instanceof Short || b instanceof Integer || b instanceof Long))
                return ((Number) a).longValue() == ((Number) b).longValue();
            // for floating-point numbers
            if ((a instanceof Double || a instanceof Float) && (b instanceof Double || b instanceof Float))
                return ((Number) a).doubleValue() == ((Number) b).doubleValue();
        } else if (a instanceof Character || a instanceof DatumSymbol) {
            // types Scheme considers interned
            return a.equals(b);
        }
        // but for, say, Strings, they don't count!
        return a == b;
    }

    /**
     * Implements the eqv? operation.
     * For sanity's sake, these are implemented to roughly act like Guile.
     */
    public static boolean eqvQ(Object a, Object b) {
        // pretend that value types are directly comparable, but also treat String as a value type
        if (a instanceof Number) {
            // note that this is distinct from = behaviour, which considers 1 and 1.0 the same number
            // for integer numbers
            if ((a instanceof Byte || a instanceof Short || a instanceof Integer || a instanceof Long) && (b instanceof Byte || b instanceof Short || b instanceof Integer || b instanceof Long))
                return ((Number) a).longValue() == ((Number) b).longValue();
            // for floating-point numbers
            if ((a instanceof Double || a instanceof Float) && (b instanceof Double || b instanceof Float))
                return ((Number) a).doubleValue() == ((Number) b).doubleValue();
        } else if (a instanceof Character || a instanceof DatumSymbol || a instanceof String) {
            // interned + types that don't require deep comparison
            return a.equals(b);
        }
        return a == b;
    }

    /**
     * Implements the equal? operation.
     * For sanity's sake, these are implemented to roughly act like Guile.
     */
    public static boolean equalQ(Object a, Object b) {
        // need to ensure List recursive equals makes actual sense
        if (a instanceof List && b instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> la = ((List<Object>) a);
            @SuppressWarnings("unchecked")
            List<Object> lb = ((List<Object>) b);
            if (la.size() != lb.size())
                return false;
            Iterator<Object> ia = la.iterator();
            Iterator<Object> ib = lb.iterator();
            while (ia.hasNext())
                if (!equalQ(ia.next(), ib.next()))
                    return false;
            return true;
        } else if (a instanceof Number) {
            // note that this is distinct from = behaviour, which considers 1 and 1.0 the same number
            // for integer numbers
            if ((a instanceof Byte || a instanceof Short || a instanceof Integer || a instanceof Long) && (b instanceof Byte || b instanceof Short || b instanceof Integer || b instanceof Long))
                return ((Number) a).longValue() == ((Number) b).longValue();
            // for floating-point numbers
            if ((a instanceof Double || a instanceof Float) && (b instanceof Double || b instanceof Float))
                return ((Number) a).doubleValue() == ((Number) b).doubleValue();
        } else if (a == null || b == null)
            return a == b;
        return a.equals(b);
    }
}
