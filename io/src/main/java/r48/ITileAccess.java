/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48;

/**
 * Abstract tile access.
 * Created 24th June, 2024.
 */
public interface ITileAccess {
    /**
     * Returns true if this X is out of bounds.
     */
    boolean xOOB(int x);

    /**
     * Returns true if this Y is out of bounds.
     */
    boolean yOOB(int y);

    /**
     * Returns true if this coordinate is out of bounds.
     */
    default boolean outOfBounds(int x, int y) {
        return xOOB(x) || yOOB(y);
    }

    /**
     * Gets a tiletype. Out of bounds access is undefined.
     */
    int getTiletype(int x, int y, int plane);

    interface RW extends ITileAccess {
        /**
         * Sets a tiletype. Out of bounds access is undefined and may lead to corruption.
         */
        void setTiletype(int x, int y, int plane, int value);
    }
}
