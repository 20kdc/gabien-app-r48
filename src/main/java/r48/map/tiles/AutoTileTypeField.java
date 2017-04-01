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

    public AutoTileTypeField(int a, int b, int c) {
        start = a;
        length = b;
        databaseId = c;
    }
}
