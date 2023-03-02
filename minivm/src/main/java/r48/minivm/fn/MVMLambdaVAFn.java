/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.List;

import r48.minivm.MVMU;

/**
 * MiniVM macro bound into a neat little package.
 * Created 2nd March 2023.
 */
public class MVMLambdaVAFn extends MVMFn {
    public final MVMLambdaFn underlying;
    public final int fixedArgs;
    public MVMLambdaVAFn(MVMLambdaFn u) {
        super(u.nameHint);
        help = u.help;
        underlying = u;
        fixedArgs = underlying.argL.length - 1;
    }

    @Override
    public String toString() {
        return "lambda-" + fixedArgs + "VA: " + nameHint;
    }

    @Override
    protected Object callDirect() {
        if (fixedArgs > 0)
            throw new RuntimeException("Not enough args to " + this + ", got 0");
        else
            return underlying.callDirect(MVMU.l());
    }

    @Override
    protected Object callDirect(Object a0) {
        if (fixedArgs > 1)
            throw new RuntimeException("Not enough args to " + this + ", got 1");
        else if (fixedArgs == 1)
            return underlying.callDirect(a0, MVMU.l());
        else
            return underlying.callDirect(MVMU.l(a0));
    }

    @Override
    protected Object callDirect(Object a0, Object a1) {
        if (fixedArgs > 2)
            throw new RuntimeException("Not enough args to " + this + ", got 2");
        else if (fixedArgs == 2)
            return underlying.callDirect(a0, a1, MVMU.l());
        else if (fixedArgs == 1)
            return underlying.callDirect(a0, MVMU.l(a1));
        else
            return underlying.callDirect(MVMU.l(a0, a1));
    }

    @Override
    protected Object callDirect(Object a0, Object a1, Object a2) {
        if (fixedArgs > 3)
            throw new RuntimeException("Not enough args to " + this + ", got 3");
        else if (fixedArgs == 3)
            return underlying.callDirect(a0, a1, a2, MVMU.l());
        else if (fixedArgs == 2)
            return underlying.callDirect(a0, a1, MVMU.l(a2));
        else if (fixedArgs == 1)
            return underlying.callDirect(a0, MVMU.l(a1, a2));
        else
            return underlying.callDirect(MVMU.l(a0, a1, a2));
    }

    @Override
    protected Object callDirect(Object a0, Object a1, Object a2, Object a3) {
        if (fixedArgs > 4)
            throw new RuntimeException("Not enough args to " + this + ", got 4");
        else if (fixedArgs == 4)
            return underlying.callL5(a0, a1, a2, a3, MVMU.l());
        else if (fixedArgs == 3)
            return underlying.callDirect(a0, a1, a2, MVMU.l(a3));
        else if (fixedArgs == 2)
            return underlying.callDirect(a0, a1, MVMU.l(a2, a3));
        else if (fixedArgs == 1)
            return underlying.callDirect(a0, MVMU.l(a1, a2, a3));
        else
            return underlying.callDirect(MVMU.l(a0, a1, a2, a3));
    }

    @Override
    protected Object callIndirect(Object[] args) {
        if (args.length < fixedArgs)
            throw new RuntimeException("Not enough args to " + this + ", got " + fixedArgs);
        if (fixedArgs > 4) {
            // have to do an indirect call past this point
            List<Object> p2 = MVMU.lArr(args, fixedArgs, args.length - fixedArgs);
            Object[] argv = new Object[fixedArgs + 1];
            System.arraycopy(args, 0, argv, 0, fixedArgs);
            argv[fixedArgs] = p2;
            return underlying.callIndirect(argv);
        } else if (fixedArgs == 4)
            return underlying.callL5(args[0], args[1], args[2], args[3], MVMU.lArr(args, 4, args.length - 4));
        else if (fixedArgs == 3)
            return underlying.callDirect(args[0], args[1], args[2], MVMU.lArr(args, 3, args.length - 3));
        else if (fixedArgs == 2)
            return underlying.callDirect(args[0], args[1], MVMU.lArr(args, 2, args.length - 2));
        else if (fixedArgs == 1)
            return underlying.callDirect(args[0], MVMU.lArr(args, 1, args.length - 1));
        else
            return underlying.callDirect(MVMU.lArr(args));
    }
}
