/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.io.data.obj;

import java.lang.reflect.Field;

import r48.io.data.DMContext;
import r48.io.data.IRIO;

/**
 * How this works:
 * It's in two states (decided by hasUnpackedYet):
 * false: Packed. All fields are INVALID, apart from the data.
 * true: Unpacked. Fields become valid, data nulled.
 *
 * Split from DM2R2kObject 8th May, 2024.
 */
public abstract class IRIOFixedObjectPacked extends IRIOFixedObject {
    private boolean hasUnpackedYet = false;

    public IRIOFixedObjectPacked(DMContext ctx, String sym) {
        super(ctx, sym);
    }

    /**
     * Ensures the object is unpacked.
     */
    protected final void unpack() {
        if (!hasUnpackedYet) {
            hasUnpackedYet = true;
            unpackImpl();
        }
    }

    /**
     * Override this to implement the core logic that unpacks the object.
     */
    protected abstract void unpackImpl();

    /**
     * This function is expected to delete the packed data.
     * The object will be forcibly reinitialized by the caller after this completes.
     */
    protected abstract void erasePackedDataImpl();

    @Override
    protected final void initialize() {
        // Disable automatic initialization, permanently
    }

    // --- Actual IRIO impl.

    @Override
    public IRIO setObject(String symbol) {
        // make sure these errors happen before the reset actually occurs
        if (!symbol.equals(objType))
            super.setObject(symbol);
        hasUnpackedYet = true;
        erasePackedDataImpl();
        // unknownChunks & main members (since normal initialize() disabled)
        // Any remaining stuff 'should be fine'
        super.setObject(symbol);
        return this;
    }

    @Override
    public final String[] getIVars() {
        unpack();
        return super.getIVars();
    }

    @Override
    public final IRIO getIVar(String sym) {
        unpack();
        return super.getIVar(sym);
    }

    @Override
    public final IRIO addIVar(String sym) {
        unpack();
        return super.addIVar(sym);
    }

    @Override
    public final Object addField(Field f) {
        unpack();
        return super.addField(f);
    }

    @Override
    public final void rmIVar(String sym) {
        unpack();
        super.rmIVar(sym);
    }
}
