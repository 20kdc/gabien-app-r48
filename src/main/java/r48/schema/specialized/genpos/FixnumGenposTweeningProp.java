/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos;

import r48.io.data.IRIO;

/**
 * Created on October 10, 2018.
 */
public class FixnumGenposTweeningProp implements IGenposTweeningProp {
    public final IRIO sp;

    public FixnumGenposTweeningProp(IRIO r) {
        sp = r;
    }

    @Override
    public double getValue() {
        return sp.getFX();
    }

    @Override
    public void setValue(double value) {
        sp.setFX((long) value);
    }

    @Override
    public boolean round() {
        return true;
    }
}
