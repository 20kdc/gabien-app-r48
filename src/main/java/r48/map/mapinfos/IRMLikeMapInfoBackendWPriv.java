/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.mapinfos;

/**
 * Used by MapInfoReparentUtil to do it's job
 * Created on 02/06/17.
 */
public interface IRMLikeMapInfoBackendWPriv extends IRMLikeMapInfoBackend {
    void swapOrders(int orderA, int orderB);

    // Returns 0 if there are no maps, 1 if there is one map...
    int getLastOrder();
}
