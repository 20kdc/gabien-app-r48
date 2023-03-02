/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm;

import java.util.LinkedList;

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
}
