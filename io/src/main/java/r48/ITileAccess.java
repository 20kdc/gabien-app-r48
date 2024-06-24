/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48;

import gabien.uslx.append.Rect;

/**
 * Abstract tile access.
 * This is built in such a way as to allow semi-efficient loop-and-remap without repeated checks.
 * Mainly, it optimizes for TileMapViewDrawLayer, as this is the truly "hot" codepath.
 * Created 24th June, 2024.
 */
public interface ITileAccess {
    /**
     * Remaps X to an "internal column base"
     * Returns -1 if this X is out of bounds.
     */
    int getXBase(int x);

    /**
     * Remaps Y to an "internal row base".
     * Returns -1 if this Y is out of bounds.
     */
    int getYBase(int y);

    /**
     * Remaps plane to an "internal plane base".
     * Returns -1 if this plane is out of bounds.
     */
    int getPBase(int p);

    /**
     * Gets the number of planes.
     * This is a constant even for unbounded tilemaps.
     */
    int getPlanes();

    /**
     * Returns true if this coordinate is accessible.
     * Note that a coordinate that is out of the formal bounds of a "Bounded" accessor, but can still be accessed, is still accessible.
     */
    default boolean coordAccessible(int x, int y) {
        return (getXBase(x) != -1) && (getYBase(y) != -1);
    }

    /**
     * Gets a tiletype. Out of bounds access will throw.
     */
    default int getTiletype(int x, int y, int plane) {
        int xBase = getXBase(x);
        if (xBase == -1)
            throw new IndexOutOfBoundsException("pos (" + x + ", " + y + "): x OOB");
        int yBase = getYBase(y);
        if (yBase == -1)
            throw new IndexOutOfBoundsException("pos (" + x + ", " + y + "): x OOB");
        int pBase = getPBase(plane);
        if (pBase == -1)
            throw new IndexOutOfBoundsException("plane (" + plane + "): P OOB");
        return getTiletypeRaw(xBase + yBase + pBase);
    }

    /**
     * Gets a tiletype. Out of bounds access is undefined.
     */
    int getTiletypeRaw(int cellID);

    interface RW extends ITileAccess {
        default void setTiletype(int x, int y, int plane, int value) {
            int xBase = getXBase(x);
            if (xBase == -1)
                throw new IndexOutOfBoundsException("pos (" + x + ", " + y + "): x OOB");
            int yBase = getYBase(y);
            if (yBase == -1)
                throw new IndexOutOfBoundsException("pos (" + x + ", " + y + "): x OOB");
            int pBase = getPBase(plane);
            if (pBase == -1)
                throw new IndexOutOfBoundsException("plane (" + plane + "): P OOB");
            setTiletypeRaw(xBase + yBase + pBase, value);
        }
        /**
         * Sets a tiletype. Out of bounds access is undefined and may lead to corruption.
         */
        void setTiletypeRaw(int cellID, int value);
    }

    /**
     * Bounded implies the ITileAccess has a fixed region (the bounds).
     * Importantly, access may or may not be permitted outside of that region.
     * Basically, all content is stored in this region, but the content may repeat outside of that region.
     * These accesses are still defined behaviour, so long as they're properly checked.
     */
    interface Bounded extends ITileAccess {
        /**
         * Gets the boundary rectangle of the map.
         */
        Rect getBounds();

        /**
         * If the given coordinate is in-bounds.
         * This is <i>distinct</i> from 'accessible'.
         */
        default boolean coordInbounds(int x, int y) {
            return getBounds().contains(x, y);
        }
    }

    /**
     * RW+Bounded
     */
    interface RWBounded extends Bounded, RW {
        
    }
}
