/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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
