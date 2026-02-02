/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.mapinfos;

import java.util.function.Consumer;

import r48.schema.util.SchemaPath;
import r48.ui.AppUI;
import r48.ui.Art;

/**
 * Attempting to bridge the gap between RXP+ and R2k's map info systems so the same UI code can be used.
 * Plan here is to abstract all of RMMapInfos's complicated shifting-about code away,
 * then make it work on RXP, then make it work on R2k.
 * All relevant GUMs are assumed to have their own map ID.
 * However, not all map IDs must have GUMs.
 * Created on 02/06/17.
 */
public interface IRMLikeMapInfoBackendWPub extends IRMLikeMapInfoBackend {
    void registerModificationHandler(Consumer<SchemaPath> onMapInfoChange);

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

    void triggerEditInfoOf(AppUI U, long k);

    // It is assumed that any children have been reparented before you use this.
    void removeMap(long k);

    // Key goes in, order comes out.
    // Assumes you've checked for conflict first.
    int createNewMap(long k);

    // A modification was completed, trigger modification handlers
    void complete();

    // Gets a symbol index for treeview
    Art.Symbol getIconForMap(long k);

    // Translates a map entry to a GUM.
    // Returning null is fine, and will return the user to the No Map Selected display.
    String translateToGUM(long k);
}
