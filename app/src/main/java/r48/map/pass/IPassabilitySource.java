/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.pass;

/**
 * Created on 09/06/17.
 */
public interface IPassabilitySource {
    // 0x01: down 0x02 right 0x04 left 0x08 up
    // -1 means don't even bother.
    int getPassability(int x, int y);
}
