/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.tiles;

/**
 * AT Field
 * Created on 2/18/17.
 */
public class AutoTileTypeField {
    public final int start, length, databaseId;
    // Note: This can be asymmetric, is processed from the centre tile's point of view,
    //        ought to contain this AT field, and is based on start index.
    private final int[] considerSameAs;

    public AutoTileTypeField(int a, int b, int c) {
        start = a;
        length = b;
        databaseId = c;
        considerSameAs = new int[] { start };
    }
    public AutoTileTypeField(int a, int b, int c, int[] considerSame) {
        start = a;
        length = b;
        databaseId = c;
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
}
