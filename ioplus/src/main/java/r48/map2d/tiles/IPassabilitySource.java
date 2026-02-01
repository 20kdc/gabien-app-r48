/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map2d.tiles;

/**
 * Created on 09/06/17.
 */
public interface IPassabilitySource {
    public static final int PASS_DOWN = 1;
    public static final int PASS_RIGHT = 2;
    public static final int PASS_LEFT = 4;
    public static final int PASS_UP = 8;
    /**
     * See PASS_* flags.
     * A flag being present means you *can* pass in that direction.
     */
    int getPassability(int x, int y);
}
