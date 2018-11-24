/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.data;

/**
 * An IRIO describing a fixed-layout object.
 * Created on November 22, 2018.
 */
public abstract class IRIOFixedObject extends IRIOFixed {
    private final String objType;
    private final String[] iv;

    public IRIOFixedObject(String sym, String[] ivs) {
        super('o');
        objType = sym;
        iv = ivs;
    }

    @Override
    public IRIO setObject(String symbol) {
        if (!symbol.equals(objType))
            return super.setObject(symbol);
        initialize();
        return this;
    }

    protected void initialize() {
        for (String s : iv)
            addIVar(s);
    }

    @Override
    public String getSymbol() {
        return objType;
    }

    @Override
    public String[] getIVars() {
        return copyStringArray(iv);
    }
}
