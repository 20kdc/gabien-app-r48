/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import r48.minivm.MVMFn;
import r48.minivm.MVMScope;
import r48.minivm.expr.MVMCExpr;

/**
 * Function that wraps an expression, passing arguments via the vars.
 * Created 1st March 2023.
 */
public class MVMLambdaFn extends MVMFn {
    public final MVMScope scope;
    public final MVMCExpr content;
    public final int argCount;
    public MVMLambdaFn(String nh, MVMScope scope, MVMCExpr content, int ac) {
        super(nh);
        this.scope = scope;
        this.content = content;
        argCount = ac;
        if (argCount > 8)
            throw new RuntimeException("lambda " + nh + " has too many args");
    }

    @Override
    public String toString() {
        return "lambda " + nameHint;
    }

    @Override
    public Object callDirect() {
        if (argCount != 0)
            throw new RuntimeException(this + " expects " + argCount + " args, not 0");
        return content.exc(scope);
    }

    @Override
    public Object callDirect(Object a0) {
        if (argCount != 1)
            throw new RuntimeException(this + " expects " + argCount + " args, not 1");
        return content.exc(scope, a0);
    }

    @Override
    public Object callDirect(Object a0, Object a1) {
        if (argCount != 2)
            throw new RuntimeException(this + " expects " + argCount + " args, not 2");
        return content.exc(scope, a0, a1);
    }

    @Override
    public Object callDirect(Object a0, Object a1, Object a2) {
        if (argCount != 3)
            throw new RuntimeException(this + " expects " + argCount + " args, not 3");
        return content.exc(scope, a0, a1, a2);
    }

    @Override
    public Object callDirect(Object a0, Object a1, Object a2, Object a3) {
        if (argCount != 4)
            throw new RuntimeException(this + " expects " + argCount + " args, not 4");
        return content.exc(scope, a0, a1, a2, a3);
    }

    @Override
    public Object callIndirect(Object[] args) {
        if (args.length != argCount)
            throw new RuntimeException(this + " expects " + argCount + " args, not " + args.length);
        switch (args.length) {
        case 0:
            return content.exc(scope);
        case 1:
            return content.exc(scope, args[0]);
        case 2:
            return content.exc(scope, args[0], args[1]);
        case 3:
            return content.exc(scope, args[0], args[1], args[2]);
        case 4:
            return content.exc(scope, args[0], args[1], args[2], args[3]);
        case 5:
            return content.exc(scope, args[0], args[1], args[2], args[3], args[4]);
        case 6:
            return content.exc(scope, args[0], args[1], args[2], args[3], args[4], args[5]);
        case 7:
            return content.exc(scope, args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
        case 8:
            return content.execute(scope, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
        default:
            throw new RuntimeException("Impossible!");
        }
    }
}
