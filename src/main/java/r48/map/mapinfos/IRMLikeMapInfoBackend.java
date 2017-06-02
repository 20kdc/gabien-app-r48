/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.mapinfos;

import r48.RubyIO;

import java.util.Set;

/**
 * Contains some of the bare reading primitives for use by MapInfoReparentUtil as part of WPriv
 * Some notes:
 * Orders start from 1 according to MapInfoReparentUtil.
 * Created on 02/06/17.
 */
public interface IRMLikeMapInfoBackend {
    // Reading primitives
    Set<Integer> getHashKeys();

    RubyIO getHashBID(int k);

    int getOrderOfMap(int k);

    // -1 means failure.
    int getMapOfOrder(int order);
}
