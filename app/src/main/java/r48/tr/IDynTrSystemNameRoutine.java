/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

/**
 * Used to implement all sorts of fun stuff
 * Created 17th March 2023.
 */
public interface IDynTrSystemNameRoutine extends IDynTr {
    @Override
    default String r() {
        throw new RuntimeException("System name routine run with incorrect arg count.");
    }
    @Override
    default String r(Object a0, Object a1) {
        throw new RuntimeException("System name routine run with incorrect arg count.");
    }
    @Override
    default String r(Object a0, Object a1, Object a2) {
        throw new RuntimeException("System name routine run with incorrect arg count.");
    }
    @Override
    default String r(Object a0, Object a1, Object a2, Object a3) {
        throw new RuntimeException("System name routine run with incorrect arg count.");
    }
}
