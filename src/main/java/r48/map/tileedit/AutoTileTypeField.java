/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.tileedit;

/**
 * AT Field
 * Created on 2/18/17.
 */
public class AutoTileTypeField {
    public final int start, length, databaseId;
    public final int represent;
    // Note: This can be asymmetric, is processed from the centre tile's point of view,
    //        ought to contain this AT field, and is based on start index.
    private final int[] considerSameAs;

    public AutoTileTypeField(int a, int b, int c, int d) {
        start = a;
        length = b;
        databaseId = c;
        represent = d;
        considerSameAs = new int[] {start};
    }

    public AutoTileTypeField(int a, int b, int c, int d, int[] considerSame) {
        start = a;
        length = b;
        databaseId = c;
        represent = d;
        considerSameAs = considerSame;
    }

    public boolean considersSameAs(AutoTileTypeField autotileType) {
        if (autotileType == null)
            return false;
        for (int i = 0; i < considerSameAs.length; i++)
            if (considerSameAs[i] == autotileType.start)
                return true;
        return false;
    }

    public boolean contains(int tile) {
        return ((tile >= start) && (tile < (start + length)));
    }
}
