/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48;

/**
 * Arrays are used everywhere, but they can be a bit inflexible
 * Created on 2/19/17.
 */
public class ArrayUtils {
    public static void removeRioElement(RubyIO target, int mi) {
        RubyIO[] old = target.arrVal;
        RubyIO[] newArr = new RubyIO[old.length - 1];
        System.arraycopy(old, 0, newArr, 0, mi);
        System.arraycopy(old, mi + 1, newArr, mi + 1 - 1, old.length - (mi + 1));
        target.arrVal = newArr;
    }

    public static void insertRioElement(RubyIO target, RubyIO rio, int i) {
        RubyIO[] old = target.arrVal;
        RubyIO[] newArr = new RubyIO[old.length + 1];
        System.arraycopy(old, 0, newArr, 0, i);
        newArr[i] = rio;
        System.arraycopy(old, i, newArr, i + 1, old.length - i);
        target.arrVal = newArr;
    }
}
