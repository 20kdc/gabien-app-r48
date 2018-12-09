/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.chunks;

import r48.io.IntUtils;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 02/06/17.
 */
public class BitfieldR2kStruct extends IRIOFixed implements IR2kInterpretable {

    // Ascending
    private final String[] flags;
    private final BitfieldElement[] flagData;

    public BitfieldR2kStruct(String[] f, int def) {
        super('o');
        flags = f;
        flagData = new BitfieldElement[8];
        for (int i = 0; i < 8; i++)
            flagData[i] = new BitfieldElement((def & (1 << i)) != 0);
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
    public boolean exportData(OutputStream baos) throws IOException {
        int value = 0;
        for (int i = 0; i < 8; i++)
            if (flagData[i].getType() == 'T')
                value |= 1 << i;
        baos.write(value);
        return false;
    }

    @Override
    public IRIO setObject(String symbol) {
        if (!symbol.equals("__bitfield__"))
            return super.setObject(symbol);
        for (int i = 0; i < 8; i++)
            flagData[i].setBool(false);
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
            if (s.equals(sym))
                return flagData[i] = new BitfieldElement(false);
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

    private static class BitfieldElement extends IRIOFixed {
        public BitfieldElement(boolean v) {
            super(v ? 'T' : 'F');
        }

        @Override
        public IRIO setBool(boolean b) {
            type = b ? 'T' : 'F';
            return this;
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
}
