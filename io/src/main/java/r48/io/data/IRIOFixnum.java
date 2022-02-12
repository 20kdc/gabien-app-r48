/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.data;

/**
 * Created on November 24, 2018.
 */
public class IRIOFixnum extends IRIOFixed {

    public long val;

    public IRIOFixnum(long t) {
        super('i');
        val = t;
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
    public long getFX() {
        return val;
    }

    @Override
    public IRIO setFX(long fx) {
        val = fx;
        return this;
    }
}
