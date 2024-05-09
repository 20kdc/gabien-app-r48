/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

/**
 * Extracted from BooleanR2kStruct 9th May, 2024.
 */
public class IRIOBoolean extends IRIOTypedData {
    private boolean value;

    public IRIOBoolean(DMContext dm2, boolean i2) {
        super(dm2);
        value = i2;
    }

    @Override
    public int getType() {
        return value ? 'T' : 'F';
    }

    @Override
    public Runnable saveState() {
        return value ? () -> { value = true; } : () -> { value = false; };
    }

    @Override
    public IRIO setBool(boolean b) {
        trackingWillChange();
        value = b;
        return this;
    }

    /**
     * Convenience for subclasses/etc.
     */
    public boolean getBool() {
        return value;
    }

    @Override
    public String[] getIVars() {
        return new String[0];
    }

    @Override
    public IRIO addIVar(String sym) {
        return null;
    }

    @Override
    public IRIO getIVar(String sym) {
        return null;
    }
}
