/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import r48.minivm.MVMScope;
import r48.minivm.compiler.MVMCompileFrame;
import r48.minivm.compiler.MVMSubScope;
import r48.minivm.expr.MVMCExpr;
import r48.minivm.expr.MVMCLocal;

/**
 * Function that wraps an expression, passing arguments via the vars.
 * Created 1st March 2023.
 */
public class MVMLambdaFn extends MVMFn {
    public final MVMScope scope;
    public final MVMCExpr content;
    public final MVMSubScope.LocalRoot[] argL;
    public final MVMCompileFrame rootFrame;
    public MVMLambdaFn(String nh, MVMScope scope, MVMCExpr content, MVMSubScope.LocalRoot[] args, MVMCompileFrame rootFrame) {
        super(nh);
        this.scope = scope;
        this.content = content;
        this.argL = args;
        this.rootFrame = rootFrame;
    }

    @Override
    public String toString() {
        return "lambda " + nameHint;
    }

    @Override
    public Object callDirect() {
        // no args, fast-path
        if (argL.length != 0)
            throw new RuntimeException(this + " expects " + argL.length + " args, not 0");
        MVMScope sc = rootFrame.wrapRuntimeScope(scope);
        return content.execute(sc, null, null, null, null, null, null, null, null);
    }

    @Override
    public Object callDirect(Object a0) {
        return execSmall(1, a0, null, null, null);
    }

    @Override
    public Object callDirect(Object a0, Object a1) {
        return execSmall(2, a0, a1, null, null);
    }

    @Override
    public Object callDirect(Object a0, Object a1, Object a2) {
        return execSmall(3, a0, a1, a2, null);
    }

    @Override
    public Object callDirect(Object a0, Object a1, Object a2, Object a3) {
        return execSmall(4, a0, a1, a2, a3);
    }

    @Override
    public Object callIndirect(Object[] argv) {
        if (argv.length != argL.length)
            throw new RuntimeException(this + " expects " + argL.length + " args, not " + argv.length);
        MVMScope sc = rootFrame.wrapRuntimeScope(scope);
        Object l0 = null, l1 = null, l2 = null, l3 = null, l4 = null, l5 = null, l6 = null, l7 = null;
        // load args into locals
        for (int i = 0; i < argv.length; i++) {
            Object aV = argv[i];
            MVMCLocal cLocal = argL[i].local;
            switch (cLocal.getFastSlot()) {
            case 0:
                l0 = aV;
                break;
            case 1:
                l1 = aV;
                break;
            case 2:
                l2 = aV;
                break;
            case 3:
                l3 = aV;
                break;
            case 4:
                l4 = aV;
                break;
            case 5:
                l5 = aV;
                break;
            case 6:
                l6 = aV;
                break;
            case 7:
                l7 = aV;
                break;
            default:
                cLocal.directWrite(sc, aV);
                break;
            }
        }
        // run!
        return content.execute(sc, l0, l1, l2, l3, l4, l5, l6, l7);
    }

    /**
     * Executes the lambda for a small number of arguments.
     */
    public Object execSmall(int ac, Object a0, Object a1, Object a2, Object a3) {
        if (ac != argL.length)
            throw new RuntimeException(this + " expects " + argL.length + " args, not " + ac);
        MVMScope sc = rootFrame.wrapRuntimeScope(scope);
        Object l0 = null, l1 = null, l2 = null, l3 = null, l4 = null, l5 = null, l6 = null, l7 = null;
        // load args into locals
        for (int i = 0; i < ac; i++) {
            Object aV;
            switch (i) {
            case 0:
                aV = a0;
                break;
            case 1:
                aV = a1;
                break;
            case 2:
                aV = a2;
                break;
            case 3:
                aV = a3;
                break;
            default:
                throw new RuntimeException("Shouldn't be calling execSmall for > 4 args");
            }
            MVMCLocal cLocal = argL[i].local;
            switch (cLocal.getFastSlot()) {
            case 0:
                l0 = aV;
                break;
            case 1:
                l1 = aV;
                break;
            case 2:
                l2 = aV;
                break;
            case 3:
                l3 = aV;
                break;
            case 4:
                l4 = aV;
                break;
            case 5:
                l5 = aV;
                break;
            case 6:
                l6 = aV;
                break;
            case 7:
                l7 = aV;
                break;
            default:
                cLocal.directWrite(sc, aV);
                break;
            }
        }
        // run!
        return content.execute(sc, l0, l1, l2, l3, l4, l5, l6, l7);
    }
}
