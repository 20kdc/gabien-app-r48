/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.chunks;

import r48.io.IntUtils;
import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.IRIOBoolean;
import r48.io.data.IRIOFixedData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Created on 02/06/17.
 */
public class BitfieldR2kStruct extends IRIOFixedData implements IR2kInterpretable {

    // Ascending
    private final String[] flags;
    private final IRIOBoolean[] flagData;

    public BitfieldR2kStruct(@NonNull DMContext context, String[] f, int def) {
        super(context, 'o');
        flags = f;
        flagData = new IRIOBoolean[8];
        for (int i = 0; i < 8; i++)
            flagData[i] = new IRIOBoolean(context, (def & (1 << i)) != 0);
    }

    @Override
    public Runnable saveState() {
        IRIOBoolean[] storedArray = flagData.clone();
        return () -> System.arraycopy(storedArray, 0, flagData, 0, flagData.length);
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        int value = IntUtils.readU8(bais);
        importData(value);
    }

    public void importData(int flag) {
        for (int i = 0; i < 8; i++)
            flagData[i].setBool((flag & (1 << i)) != 0);
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        int value = 0;
        for (int i = 0; i < 8; i++)
            if (flagData[i].getType() == 'T')
                value |= 1 << i;
        baos.write(value);
    }

    @Override
    public IRIO setObject(String symbol) {
        if (!symbol.equals("__bitfield__"))
            return super.setObject(symbol);
        trackingWillChange();
        for (int i = 0; i < 8; i++)
            flagData[i] = new IRIOBoolean(context, false);
        return this;
    }

    @Override
    public String getSymbol() {
        return "__bitfield__";
    }

    @Override
    public String[] getIVars() {
        return copyStringArray(flags);
    }

    @Override
    public IRIO addIVar(String sym) {
        for (int i = 0; i < flags.length; i++) {
            String s = flags[i];
            if (s.equals(sym)) {
                trackingWillChange();
                return flagData[i] = new IRIOBoolean(context, false);
            }
        }
        return null;
    }

    @Override
    public IRIO getIVar(String sym) {
        for (int i = 0; i < flags.length; i++) {
            String s = flags[i];
            if (s.equals(sym))
                return flagData[i];
        }
        return null;
    }
}
