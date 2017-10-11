/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
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
