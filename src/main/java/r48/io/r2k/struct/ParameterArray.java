/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.struct;

import r48.io.data.IRIO;
import r48.io.data.IRIOFixedArray;
import r48.io.data.IRIOFixnum;
import r48.io.r2k.chunks.StringR2kStruct;

/**
 * Created on December 06, 2018.
 */
public class ParameterArray extends IRIOFixedArray<IRIO> {
    public final StringR2kStruct text = new StringR2kStruct();

    @Override
    public int getALen() {
        return super.getALen() + 1;
    }

    @Override
    public IRIO getAElem(int i) {
        if (i == 0)
            return text;
        return super.getAElem(i - 1);
    }

    @Override
    public IRIO addAElem(int i) {
        if (i == 0)
            throw new RuntimeException("The text cannot be inserted.");
        return super.addAElem(i - 1);
    }

    @Override
    public void rmAElem(int i) {
        if (i == 0)
            throw new RuntimeException("The text cannot be removed.");
        super.rmAElem(i - 1);
    }

    @Override
    public boolean getAFixedFormat() {
        return true;
    }

    @Override
    public IRIO newValue() {
        return new IRIOFixnum(0);
    }
}
