/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.Iterator;
import java.util.List;

import gabien.datum.DatumSymbol;
import r48.minivm.MVMEnv;
import r48.minivm.MVMU;
import r48.minivm.compiler.MVMFnCallCompiler;

/**
 * MiniVM standard library.
 * Created 2nd March 2023.
 */
public class MVMListsLibrary {
    public static void add(MVMEnv ctx) {
        ctx.defineSlot(new DatumSymbol("for-each")).v = new ForEach()
                .attachHelp("(for-each F L...) : Given a group of lists, iterates over all of them simultaneously, calling F with each set of results.");
        ctx.defineSlot(new DatumSymbol("append")).v = new Append()
                .attachHelp("(append L...) : Creates a new list from a set of appended lists.");
    }

    public static final class ForEach extends MVMFn {
        public ForEach() {
            super("for-each");
        }

        @Override
        protected Object callDirect() {
            throw new RuntimeException("for-each requires at least a procedure");
        }

        @Override
        protected Object callDirect(Object a0) {
            // no-op
            MVMFnCallCompiler.asFn(a0);
            return null;
        }

        @Override
        protected Object callDirect(Object a0, Object a1) {
            MVMFn fn = MVMFnCallCompiler.asFn(a0);
            @SuppressWarnings("unchecked")
            List<Object> l1 = (List<Object>) a1;
            for (Object o : l1)
                fn.clDirect(o);
            return null;
        }

        @Override
        protected Object callDirect(Object a0, Object a1, Object a2) {
            MVMFn fn = MVMFnCallCompiler.asFn(a0);
            @SuppressWarnings("unchecked")
            List<Object> l1 = (List<Object>) a1;
            @SuppressWarnings("unchecked")
            List<Object> l2 = (List<Object>) a2;
            Iterator<Object> i1 = l1.iterator();
            Iterator<Object> i2 = l2.iterator();
            while (i1.hasNext())
                fn.clDirect(i1.next(), i2.next());
            return null;
        }

        @Override
        protected Object callDirect(Object a0, Object a1, Object a2, Object a3) {
            MVMFn fn = MVMFnCallCompiler.asFn(a0);
            @SuppressWarnings("unchecked")
            List<Object> l1 = (List<Object>) a1;
            @SuppressWarnings("unchecked")
            List<Object> l2 = (List<Object>) a2;
            @SuppressWarnings("unchecked")
            List<Object> l3 = (List<Object>) a3;
            Iterator<Object> i1 = l1.iterator();
            Iterator<Object> i2 = l2.iterator();
            Iterator<Object> i3 = l3.iterator();
            while (i1.hasNext())
                fn.clDirect(i1.next(), i2.next(), i3.next());
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Object callIndirect(Object[] args) {
            if (args.length == 4)
                return callDirect(args[0], args[1], args[2], args[3]);
            else if (args.length == 3)
                return callDirect(args[0], args[1], args[2]);
            else if (args.length == 2)
                return callDirect(args[0], args[1]);
            else if (args.length == 1)
                return callDirect(args[0]);
            else if (args.length == 0)
                return callDirect();
            MVMFn fn = MVMFnCallCompiler.asFn(args[0]);
            Object[] temp = new Object[args.length - 1];
            Iterator<Object>[] it = new Iterator[temp.length];
            for (int i = 0; i < it.length; i++)
                it[i] = ((List<Object>) args[i + 1]).iterator();
            while (it[0].hasNext()) {
                for (int i = 0; i < it.length; i++)
                    temp[i] = it[i].next();
                fn.callIndirect(temp);
            }
            return null;
        }
    }

    public static final class Append extends MVMFn {
        public Append() {
            super("append");
        }

        @Override
        protected Object callDirect() {
            return MVMU.l();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Object callDirect(Object a0) {
            List<Object> target = MVMU.l();
            target.addAll((List<Object>) a0);
            return target;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Object callDirect(Object a0, Object a1) {
            List<Object> target = MVMU.l();
            target.addAll((List<Object>) a0);
            target.addAll((List<Object>) a1);
            return target;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Object callDirect(Object a0, Object a1, Object a2) {
            List<Object> target = MVMU.l();
            target.addAll((List<Object>) a0);
            target.addAll((List<Object>) a1);
            target.addAll((List<Object>) a2);
            return target;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Object callDirect(Object a0, Object a1, Object a2, Object a3) {
            List<Object> target = MVMU.l();
            target.addAll((List<Object>) a0);
            target.addAll((List<Object>) a1);
            target.addAll((List<Object>) a2);
            target.addAll((List<Object>) a3);
            return target;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Object callIndirect(Object[] args) {
            List<Object> target = MVMU.l();
            for (Object obj : args)
                target.addAll((List<Object>) obj);
            return target;
        }
    }
}
