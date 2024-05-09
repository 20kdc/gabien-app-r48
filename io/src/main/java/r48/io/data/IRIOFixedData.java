/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import org.eclipse.jdt.annotation.NonNull;

/**
 * An IRIO with a fixed type.
 * The IRIO cannot be changed from this type.
 * All methods apart from IVars (left unimplemented except for rmIVar, which is not supported) are implemented as 'not supported' by default.
 * The setter method for your specific type should be reimplemented.
 * Created on November 22, 2018. Method impls pushed into IRIOTypedData 9th May, 2024.
 */
public abstract class IRIOFixedData extends IRIOTypedData {
    private final int type;

    public IRIOFixedData(@NonNull DMContext context, int t) {
        super(context);
        type = t;
    }

    @Override
    public int getType() {
        return type;
    }
}
