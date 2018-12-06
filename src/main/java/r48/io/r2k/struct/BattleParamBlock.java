/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.struct;

import r48.RubyIO;
import r48.io.IntUtils;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixed;
import r48.io.data.IRIOFixnum;
import r48.io.r2k.chunks.IR2kStruct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * BPB
 * Created on 06/06/17.
 */
public class BattleParamBlock extends IRIOFixed implements IR2kStruct {
    public IRIOFixnum[] array = new IRIOFixnum[] {
            new IRIOFixnum(0),
            new IRIOFixnum(0),
            new IRIOFixnum(0),
            new IRIOFixnum(0),
            new IRIOFixnum(0),
            new IRIOFixnum(0)
    };

    public BattleParamBlock() {
        super('[');
    }

    @Override
    public IRIO setArray() {
        for (int i = 0; i < array.length; i++)
            array[i] = new IRIOFixnum(0);
        return this;
    }

    @Override
    public RubyIO asRIO() {
        return new RubyIO().setDeepClone(this);
    }

    @Override
    public void fromRIO(IRIO src) {
        setDeepClone(src);
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        for (int i = 0; i < array.length; i++)
            array[i].val = IntUtils.readU16(bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        for (int i = 0; i < array.length; i++)
            IntUtils.writeU16(baos, (int) array[i].val);
        return false;
    }

    @Override
    public int getALen() {
        return array.length;
    }

    @Override
    public IRIO getAElem(int i) {
        return array[i];
    }

    @Override
    public boolean getAFixedFormat() {
        return true;
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
