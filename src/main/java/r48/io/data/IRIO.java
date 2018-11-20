/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.data;

import r48.RubyBigNum;

/**
 * Ok, so here's the deal. Data Model 2 implementation attempt 1 *failed miserably*.
 * That doesn't mean Data Model 2 is completely unimplementable.
 * This interface represents the 'Data Model 2 plan'.
 * Methods are named as they will be in the final version of DM2.
 * RubyIO will provide stronger type guarantees on them so the code won't break.
 * The plan is: Make haste, carefully.
 * Created on November 19, 2018.
 */
public abstract class IRIO {
    public abstract int getType();

    // Primitive Setters.
    // They return self.
    public abstract IRIO setNull();

    public abstract IRIO setFX(long fx);

    public abstract IRIO setBool(boolean b);

    public abstract IRIO setSymbol(String s);

    public abstract IRIO setString(String s);

    public abstract void rmIVar(String sym);

    public abstract IRIO addIVar(String sym);

    public abstract IRIO getIVar(String sym);

    public abstract long getFX();

    public abstract String decString();

    public abstract String getSymbol();

    public abstract byte[] getBuffer();

    public abstract int getALen();

    @Override
    public String toString() {
        // NOTE: The following rules are relied upon by schema name-routines, at least in theory:
        // 1. "null" means t0.
        // 2. Any valid number is a number.
        // 3. T/F are booleans.
        String data = "";
        int type = getType();
        if (type == 'u')
            return getSymbol() + ";" + getBuffer().length + "b";
        if (type == 'o')
            return getSymbol();
        if (type == '[')
            data = getALen() + "]";
        if (type == ':')
            data = getSymbol();
        if (type == '"')
            return "\"" + decString() + "\"";
        if (type == 'f')
            return decString() + "f";
        if (type == 'i')
            return Long.toString(getFX());
        if (type == 'l') {
            String str2 = "L";
            RubyBigNum working = new RubyBigNum(getBuffer(), false);
            boolean negated = false;
            if (working.isNegative()) {
                negated = true;
                working = working.negate();
            }
            if (working.compare(RubyBigNum.ZERO) == 0) {
                str2 = "0L";
            } else {
                while (working.compare(RubyBigNum.ZERO) > 0) {
                    RubyBigNum[] res = working.divide(RubyBigNum.TEN);
                    str2 = ((char) ('0' + res[1].truncateToLong())) + str2;
                    working = res[0];
                }
            }
            if (negated)
                str2 = "-" + str2;
            return str2;
        }
        if (type == '0')
            return "null";
        return ((char) type) + data;
    }

    public static boolean rubyTypeEquals(IRIO a, IRIO b) {
        if (a == b)
            return true;
        int aType = a.getType();
        if (aType != b.getType())
            return false;
        if (aType == 'o')
            return a.getSymbol().equals(b.getSymbol());
        if (aType == 'u')
            return a.getSymbol().equals(b.getSymbol());
        return true;
    }

    // used to check Hash stuff
    public static boolean rubyEquals(IRIO a, IRIO b) {
        if (a == b)
            return true;
        int aType = a.getType();
        if (aType != b.getType())
            return false;
        // primitive types
        if (aType == 'i')
            return a.getFX() == b.getFX();
        if (aType == '\"')
            return a.decString().equals(b.decString());
        if (aType == 'f')
            return a.decString().equals(b.decString());
        if (aType == 'l')
            return new RubyBigNum(a.getBuffer(), true).compare(new RubyBigNum(b.getBuffer(), true)) == 0;
        if (aType == ':')
            return a.getSymbol().equals(b.getSymbol());
        if (aType == 'T')
            return true;
        if (aType == 'F')
            return true;
        if (aType == '0')
            return true;
        return false;
    }
}
