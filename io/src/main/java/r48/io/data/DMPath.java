/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.io.data;

import java.util.LinkedList;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A path that routes from one place to another in IRIO-space.
 * Created 21st August, 2024.
 */
public abstract class DMPath implements Function<IRIO, IRIO> {
    public static final Empty EMPTY_RELAXED = new Empty(false);
    public static final Empty EMPTY_STRICT = new Empty(true);
    public static final Fail FAIL = new Fail();

    /**
     * Amount of elements that will be returned in traceRoute.
     * Importantly, this does not include the starting element.
     */
    public final int traceRouteSize;

    /**
     * Used by tests to make sure more issues are caught.
     */
    public final boolean strict;

    public DMPath(int trs, boolean strict) {
        this.strict = strict;
        traceRouteSize = trs;
    }

    @Override
    public final @Nullable IRIO apply(@Nullable IRIO t) {
        return (IRIO) getRO(t);
    }

    /**
     * Executes this path.
     */
    public final @Nullable RORIO getRO(@Nullable RORIO input) {
        try {
            return getImpl(input);
        } catch (Throwable t) {
            if (strict)
                throw t;
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Executes this path to add the target.
     */
    public final @Nullable IRIO add(@Nullable IRIO input) {
        try {
            return addImpl(input);
        } catch (Throwable t) {
            if (strict)
                throw t;
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Executes this path to delete the target.
     */
    public final @Nullable IRIO del(@Nullable IRIO input) {
        try {
            return delImpl(input);
        } catch (Throwable t) {
            if (strict)
                throw t;
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Writes traceRouteSize encountered elements to elements at offset.
     * The last element, if any, is the result. This is also returned.
     * Written elements may be null if an error is encountered along the way or null is passed in.
     */
    public final RORIO traceRoute(RORIO target, RORIO[] elements, int offset) {
        try {
            return traceRouteImpl(target, elements, offset);
        } catch (Throwable t) {
            if (strict)
                throw t;
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Executes this path.
     */
    public final @Nullable IRIO getRW(@Nullable IRIO input) {
        return (IRIO) getRO(input);
    }

    /**
     * Executes this path.
     */
    protected abstract @Nullable RORIO getImpl(@Nullable RORIO input);

    /**
     * Executes this path to add a target.
     */
    protected @Nullable IRIO addImpl(@Nullable IRIO input) {
        throw new RuntimeException("This path is unaddable.");
    }

    /**
     * Executes this path to delete a target.
     */
    protected @Nullable IRIO delImpl(@Nullable IRIO input) {
        throw new RuntimeException("This path is undeletable.");
    }

    /**
     * Writes traceRouteSize encountered elements to elements at offset.
     * The last element, if any, is the result. This is also returned.
     * Written elements may be null if an error is encountered along the way or null is passed in.
     */
    protected abstract @Nullable RORIO traceRouteImpl(RORIO target, RORIO[] elements, int offset);

    /**
     * Complete traceroute including start element.
     */
    public final RORIO[] traceRouteComplete(RORIO target) {
        RORIO[] res = new RORIO[traceRouteSize + 1];
        res[0] = target;
        traceRoute(target, res, 1);
        return res;
    }

    /**
     * Concatenates this path with another.
     */
    public DMPath with(DMPath next) {
        return Concat.concat(strict, this, next);
    }

    /**
     * Breaks down this DMPath. Only used here and in Concat.
     */
    public DMPath[] breakdown() {
        return new DMPath[] {this};
    }

    // --- Helpful Bits & Pieces ---

    /**
     * With instance variable.
     */
    public DMPath withIVar(String iv) {
        return with(new IVar(iv));
    }

    /**
     * With hash index.
     */
    public DMPath withHash(DMKey hashVal) {
        return with(new Hash(hashVal));
    }

    /**
     * With array index.
     */
    public DMPath withArray(int index) {
        return with(new Array(index));
    }

    /**
     * With array length.
     */
    public DMPath withArrayLength() {
        return with(new ArrayLength());
    }

    /**
     * With default value.
     */
    public DMPath withDefVal() {
        return with(new HashDefaultValue());
    }

    /**
     * With failure.
     */
    public DMPath withFail() {
        return with(new Fail());
    }

    // --- Central Utilities ---

    public static final class Concat extends DMPath {
        private final DMPath[] components;

        private static int totalTraceRouteSize(DMPath[] inputs) {
            int total = 0;
            for (DMPath dmp : inputs)
                total += dmp.traceRouteSize;
            return total;
        }

        private static DMPath concat(boolean strict, DMPath... contents) {
            LinkedList<DMPath> res = new LinkedList<>();
            for (DMPath dmp : contents) {
                if (dmp instanceof Concat) {
                    for (DMPath dmpi : ((Concat) dmp).components)
                        res.add(dmpi);
                } else if (dmp instanceof Empty) {
                    // skip
                } else {
                    res.add(dmp);
                }
            }
            if (res.size() == 0)
                return strict ? EMPTY_STRICT : EMPTY_RELAXED;
            if (res.size() == 1)
                return res.get(0);
            return new Concat(res.toArray(new DMPath[0]));
        }

        private Concat(DMPath... contents) {
            super(totalTraceRouteSize(contents), contents[0].strict);
            if (contents.length <= 1)
                throw new RuntimeException("invalid length");
            components = contents;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (DMPath dmp : components) {
                sb.append(dmp);
                sb.append(',');
            }
            return sb.toString();
        }

        @Override
        protected RORIO getImpl(RORIO input) {
            for (DMPath dmp : components) {
                if (input == null)
                    break;
                input = dmp.getImpl(input);
            }
            return input;
        }

        @Override
        protected IRIO addImpl(IRIO input) {
            for (int i = 0; i < components.length - 1; i++) {
                if (input == null)
                    return null;
                input = (IRIO) components[i].getImpl(input);
            }
            return components[components.length - 1].addImpl(input);
        }

        @Override
        protected IRIO delImpl(IRIO input) {
            for (int i = 0; i < components.length - 1; i++) {
                if (input == null)
                    return null;
                input = (IRIO) components[i].getImpl(input);
            }
            return components[components.length - 1].delImpl(input);
        }

        @Override
        protected RORIO traceRouteImpl(RORIO target, RORIO[] elements, int offset) {
            for (DMPath dmp : components) {
                target = dmp.traceRouteImpl(target, elements, offset);
                offset += dmp.traceRouteSize;
            }
            return target;
        }

        @Override
        public DMPath[] breakdown() {
            return components.clone();
        }
    }

    public static final class Empty extends DMPath {
        private Empty(boolean strict) {
            super(0, strict);
        }

        @Override
        public String toString() {
            return "empty";
        }

        @Override
        protected RORIO getImpl(RORIO input) {
            return input;
        }
        @Override
        protected IRIO addImpl(IRIO input) {
            return input;
        }
        @Override
        protected RORIO traceRouteImpl(RORIO target, RORIO[] elements, int offset) {
            return target;
        }
    }

    public static abstract class Unit extends DMPath {
        public Unit() {
            super(1, true);
        }

        @Override
        protected RORIO traceRouteImpl(RORIO target, RORIO[] elements, int offset) {
            return elements[offset] = getImpl(target);
        }
    }

    // --- With Implementors ---

    public static final class Fail extends DMPath {
        private Fail() {
            super(0, true);
        }

        @Override
        public String toString() {
            return "fail";
        }

        @Override
        public IRIO addImpl(IRIO input) {
            return null;
        }
        @Override
        public RORIO getImpl(RORIO input) {
            return null;
        }
        @Override
        protected RORIO traceRouteImpl(RORIO target, RORIO[] elements, int offset) {
            return null;
        }
    }

    public static final class IVar extends DMPath.Unit {
        public final String key;

        public IVar(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return "i:" + key;
        }

        @Override
        protected IRIO addImpl(IRIO input) {
            if (input == null)
                return null;
            IRIO res = input.addIVar(key);
            if (res == null)
                System.err.println("Warning: Failed to create IVar " + key + " in " + input);
            return res;
        }
        @Override
        protected IRIO delImpl(IRIO input) {
            if (input == null)
                return null;
            IRIO old = input.getIVar(key);
            input.rmIVar(key);
            return old;
        }
        @Override
        protected RORIO getImpl(RORIO input) {
            if (input == null)
                return null;
            return input.getIVar(key);
        }
    }

    public static final class Hash extends DMPath.Unit {
        public final DMKey key;

        public Hash(DMKey key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return "h:" + key;
        }

        @Override
        protected IRIO addImpl(IRIO input) {
            if (input == null)
                return null;
            return input.addHashVal(key);
        }
        @Override
        protected IRIO delImpl(IRIO input) {
            if (input == null)
                return null;
            IRIO old = input.getHashVal(key);
            input.removeHashVal(key);
            return old;
        }
        @Override
        protected RORIO getImpl(RORIO input) {
            if (input == null)
                return null;
            return input.getHashVal(key);
        }
    }

    public static final class Array extends DMPath.Unit {
        public final int index;

        public Array(int index) {
            this.index = index;
        }

        @Override
        public String toString() {
            return "a:" + index;
        }

        @Override
        protected IRIO addImpl(IRIO input) {
            if (input == null)
                return null;
            return input.getAElem(index);
        }
        @Override
        protected RORIO getImpl(RORIO input) {
            if (input == null)
                return null;
            return input.getAElem(index);
        }
    }

    public static final class ArrayLength extends DMPath.Unit {
        public ArrayLength() {
        }

        @Override
        public String toString() {
            return "arrayLength";
        }

        @Override
        protected RORIO getImpl(RORIO input) {
            if (input == null)
                return null;
            return DMKey.of(input.getALen());
        }
    }

    public static final class HashDefaultValue extends DMPath.Unit {
        public HashDefaultValue() {
        }

        @Override
        public String toString() {
            return "hashDefaultValue";
        }

        @Override
        protected RORIO getImpl(RORIO input) {
            if (input == null)
                return null;
            return input.getHashDefVal();
        }

        @Override
        protected IRIO addImpl(IRIO input) {
            if (input == null)
                return null;
            return input.getHashDefVal();
        }
    }
}
