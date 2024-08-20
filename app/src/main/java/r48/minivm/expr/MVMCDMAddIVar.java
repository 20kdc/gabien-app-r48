/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.expr;

import static datum.DatumTreeUtils.sym;

import org.eclipse.jdt.annotation.NonNull;

import r48.io.data.IRIO;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMScope;
import r48.minivm.MVMType;
import r48.minivm.MVMU;

/**
 * Pulled out of PathSyntax 20th August 2024
 */
public class MVMCDMAddIVar extends MVMCLinear.Step {
    public final String key;

    public MVMCDMAddIVar(String k) {
        key = k;
    }

    @Override
    public MVMType getTypeForInput(MVMType inputType) {
        inputType.assertCanImplicitlyCastTo(MVMEnvR48.IRIO_TYPE, this);
        return MVMEnvR48.IRIO_TYPE;
    }

    @Override
    public Object execute(@NonNull MVMScope ctx, Object l0, Object l1, Object l2, Object l3, Object l4, Object l5, Object l6, Object l7, Object value) {
        IRIO res = (IRIO) value;
        if (res == null)
            return null;
        IRIO ivv = res.getIVar(key);
        // As of DM2 this is guaranteed to create a defined value,
        //  and setting it to null will break things.
        if (ivv == null)
            ivv = res.addIVar(key);
        if (ivv == null)
            System.err.println("Warning: Failed to create IVar " + key + " in " + res);
        return ivv;
    }

    @Override
    public Object disasm() {
        return MVMU.l(sym("addIVar"), key);
    }
}
