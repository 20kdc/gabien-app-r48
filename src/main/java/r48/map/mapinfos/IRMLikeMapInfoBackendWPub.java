/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.mapinfos;

import gabien.ui.IConsumer;
import gabien.ui.Rect;
import r48.schema.util.SchemaPath;

/**
 * Attempting to bridge the gap between RXP+ and R2k's map info systems so the same UI code can be used.
 * Plan here is to abstract all of RMMapInfos's complicated shifting-about code away,
 * then make it work on RXP, then make it work on R2k.
 * Created on 02/06/17.
 */
public interface IRMLikeMapInfoBackendWPub extends IRMLikeMapInfoBackend {
    void registerModificationHandler(IConsumer<SchemaPath> onMapInfoChange);

    // Gets the actual mapinfo object.
    // The following are PRIMITIVES:
    // @parent_id: 0 is root. 0 does not exist.
    //  (*cough* R2k's "root" will be dealt with by a decorator that will exist for the sole purpose of getting rid of it. *cough*)

    // The relocation primitives. (Implement using a IRMLikeMapInfoBackendWPriv implementation and MapInfoReparentUtil)
    // Note:
    // orderFrom and orderTo are relative to the *current state*.
    // This makes it consistent no matter which direction you're moving things about, but makes things act a bit odd.
    // If you move something from <arbitrary order> to <some order + 1>, it will attach itself directly under <some order>.
    // Makes sense, but it makes the logic in stuff a bit hard to explain...
    boolean wouldRelocatingInOrderFail(int orderFrom, int orderTo);

    // Actually perform the operation.
    // Returned is the new index, in case it differs.
    int relocateInOrder(int orderFrom, int orderTo);

    void triggerEditInfoOf(int k);

    // It is assumed that any children have been reparented before you use this.
    void removeMap(int k);

    // Key goes in, order comes out.
    // Assumes you've checked for conflict first.
    int createNewMap(int k);

    // A modification was completed, trigger modification handlers
    void complete();

    Rect getIconForMap(int k);
}
