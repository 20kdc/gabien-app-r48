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
        for (int j = 0; j < mi; j++)
            newArr[j] = old[j];
        for (int j = mi + 1; j < old.length; j++)
            newArr[j - 1] = old[j];
        target.arrVal = newArr;
    }

    public static void insertRioElement(RubyIO target, RubyIO rio, int i) {
        RubyIO[] old = target.arrVal;
        RubyIO[] newArr = new RubyIO[old.length + 1];
        for (int j = 0; j < i; j++)
            newArr[j] = old[j];
        newArr[i] = rio;
        for (int j = i; j < old.length; j++)
            newArr[j + 1] = old[j];
        target.arrVal = newArr;
    }
}
