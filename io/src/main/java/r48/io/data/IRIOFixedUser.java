/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import org.eclipse.jdt.annotation.NonNull;

import gabien.uslx.io.MemoryishR;
import gabien.uslx.io.MemoryishRW;

/**
 * Created on November 24, 2018.
 */
public class IRIOFixedUser extends IRIOFixedData {
    private final String objType;
    private DMBlob userVal;

    public IRIOFixedUser(@NonNull DMContext context, String user, byte[] def) {
        super(context, 'u');
        objType = user;
        userVal = new DMBlob(context, def);
    }

    @Override
    public Runnable saveState() {
        final DMBlob saved = userVal;
        return () -> userVal = saved;
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

    @Override
    public String getSymbol() {
        return objType;
    }

    @Override
    public MemoryishR getBuffer() {
        return userVal;
    }

    @Override
    public MemoryishRW editUser() {
        return userVal;
    }

    @Override
    public void putBuffer(byte[] data) {
        trackingWillChange();
        userVal = new DMBlob(context, data.clone());
    }

    @Override
    public IRIO setUser(String symbol, byte[] data) {
        if (!symbol.equals(objType))
            return super.setUser(symbol, data);
        trackingWillChange();
        userVal = new DMBlob(context, data.clone());
        return this;
    }
}
