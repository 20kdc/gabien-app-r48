/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.mapinfos;

import r48.io.data.IRIO;

import java.util.HashMap;
import java.util.Set;

/**
 * Contains some of the bare reading primitives for use by MapInfoReparentUtil as part of WPriv
 * Some notes:
 * Orders start from 1 according to MapInfoReparentUtil.
 * Created on 02/06/17.
 */
public interface IRMLikeMapInfoBackend {
    // Reading primitives
    Set<Long> getHashKeys();

    IRIO getHashBID(long k);

    // -1 still means failure.
    int getOrderOfMap(long k);

    // -1 means failure.
    long getMapOfOrder(int order);

    // MapInfos should be disabled if this returns non-null.
    String calculateIndentsAndGetErrors(HashMap<Long, Integer> id);
}
