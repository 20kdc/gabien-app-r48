/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.mapinfos;

import gabien.ui.IConsumer;
import r48.AppMain;
import r48.RubyIO;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixedArray;
import r48.io.data.IRIOFixnum;
import r48.io.r2k.dm2chk.DM2SparseArrayH;
import r48.io.r2k.obj.MapInfo;
import r48.io.r2k.struct.MapTree;
import r48.schema.util.SchemaPath;
import r48.ui.Art;

import java.util.*;

/**
 * Just another one of those classes.
 * (As of midnight between December 4th & December 5th 2018, this is now kind of type-reliant for sanity reasons.)
 * Created on 02/06/17.
 */
public class R2kRMLikeMapInfoBackend implements IRMLikeMapInfoBackendWPub, IRMLikeMapInfoBackendWPriv {
    public IConsumer<SchemaPath> modHandler;
    public IObjectBackend.ILoadedObject mapTree = AppMain.objectDB.getObject("RPG_RT.lmt");
    // Note: The orders table is [order] = map.
    // So swapping orders is probably the easiest operation here.
    public DM2SparseArrayH<MapInfo> mapTreeHash = ((MapTree) (mapTree.getObject())).mapInfos;
    public IRIOFixedArray<IRIOFixnum> mapTreeOrders = ((MapTree) (mapTree.getObject())).mapOrder;

    public R2kRMLikeMapInfoBackend() {

    }

    @Override
    public void swapOrders(int orderA, int orderB) {
        IRIOFixnum b = (IRIOFixnum) mapTreeOrders.arrVal[orderA];
        mapTreeOrders.arrVal[orderA] = mapTreeOrders.arrVal[orderB];
        mapTreeOrders.arrVal[orderB] = b;
    }

    @Override
    public int getLastOrder() {
        // Note the - 1 to make Order 0 disappear.
        return mapTreeOrders.arrVal.length - 1;
    }

    public static String sNameFromInt(int i) {
        String m = Integer.toString(i);
        while (m.length() < 4)
            m = "0" + m;
        return "Map" + m + ".lmu";
    }

    public static String sTranslateToGUM(long k) {
        final IRIO map = AppMain.objectDB.getObject("RPG_RT.lmt").getObject().getIVar("@map_infos").getHashVal(new RubyIO().setFX(k));
        String pfx = map.getIVar("@type").getFX() == 2 ? "Area." : "Map.";
        return pfx + k;
    }

    @Override
    public void registerModificationHandler(IConsumer<SchemaPath> onMapInfoChange) {
        modHandler = onMapInfoChange;
        AppMain.objectDB.registerModificationHandler(mapTree.getObject(), onMapInfoChange);
    }

    @Override
    public boolean wouldRelocatingInOrderFail(int orderFrom, int orderTo) {
        return MapInfoReparentUtil.wouldRelocatingInOrderFail(orderFrom, orderTo, this);
    }

    @Override
    public int relocateInOrder(int orderFrom, int orderTo) {
        return MapInfoReparentUtil.relocateInOrder(orderFrom, orderTo, this);
    }

    @Override
    public void triggerEditInfoOf(long k) {
        AppMain.launchNonRootSchema(mapTree, "RPG::MapTree", new RubyIO().setFX(k), getHashBID(k), "RPG::MapInfo", "M" + k, null);
    }

    @Override
    public void removeMap(long k) {
        // Prepare...
        MapInfoReparentUtil.removeMapHelperSALT(k, this);
        // Remove last from array
        IRIOFixnum[] resArray = new IRIOFixnum[mapTreeOrders.getALen() - 1];
        System.arraycopy(mapTreeOrders.arrVal, 0, resArray, 0, resArray.length);
        mapTreeOrders.arrVal = resArray;
        // Eliminate from hash
        mapTreeHash.removeHashVal(new RubyIO().setFX(k));
    }

    @Override
    public int createNewMap(long k) {
        int targetOrder = mapTreeOrders.arrVal.length - 1;
        long l = getMapOfOrder(targetOrder);
        if (l == -1)
            l = 0;

        IRIOFixnum[] resArray = new IRIOFixnum[mapTreeOrders.arrVal.length + 1];
        System.arraycopy(mapTreeOrders.arrVal, 0, resArray, 0, mapTreeOrders.arrVal.length);

        IRIOFixnum key = new IRIOFixnum(k);

        resArray[resArray.length - 1] = key;

        MapInfo mi = mapTreeHash.addHashVal(key);
        SchemaPath.setDefaultValue(mi, AppMain.schemas.getSDBEntry("RPG::MapInfo"), new RubyIO().setFX(k));
        mi.parent.i = (int) l;

        mapTreeOrders.arrVal = resArray;
        return resArray.length - 1;
    }

    @Override
    public void complete() {
        // Need to update indent...
        getHashBID(0).getIVar("@indent").setFX(0);
        LinkedList<Long> intList = new LinkedList<Long>(getHashKeys());
        Collections.sort(intList, new Comparator<Long>() {
            @Override
            public int compare(Long p0, Long p1) {
                int t0 = getOrderOfMap(p0);
                int t1 = getOrderOfMap(p1);
                if (t0 > t1)
                    return 1;
                if (t0 < t1)
                    return -1;
                return 0;
            }
        });
        LinkedList<Long> parentStack = new LinkedList<Long>();
        int lastOrder = 0;
        for (final Long k : intList) {
            final IRIO map = getHashBID(k);
            final int order = getOrderOfMap(k);
            if (lastOrder < order)
                lastOrder = order;
            final long parent = map.getIVar("@parent_id").getFX();
            if (parent == 0) {
                parentStack.clear();
            } else {
                if (parentStack.lastIndexOf(parent) != -1)
                    while (parentStack.getLast() != parent)
                        parentStack.removeLast();
            }
            parentStack.add(k);
            map.getIVar("@indent").setFX(parentStack.size());
        }
        // and done!
        SchemaPath fakePath = new SchemaPath(AppMain.schemas.getSDBEntry("File.RPG_RT.lmt"), mapTree);
        AppMain.objectDB.objectRootModified(mapTree, fakePath);
        modHandler.accept(fakePath);
    }

    @Override
    public Art.Symbol getIconForMap(long k) {
        final IRIO map = getHashBID(k);
        return map.getIVar("@type").getFX() == 2 ? Art.Symbol.Area : Art.Symbol.Map;
    }

    @Override
    public String translateToGUM(long k) {
        return sTranslateToGUM(k);
    }

    @Override
    public Set<Long> getHashKeys() {
        // The job of this is to hide that there *ever was* a Map 0.
        // Map 0 is reserved.
        HashSet<Long> hs = new HashSet<Long>();
        for (Integer i : mapTreeHash.hashVal.keySet())
            if (i != 0)
                hs.add((long) i);
        return hs;
    }

    @Override
    public IRIO getHashBID(long k) {
        return mapTreeHash.getHashVal(new RubyIO().setFX(k));
    }

    @Override
    public int getOrderOfMap(long k) {
        for (int i = 0; i < mapTreeOrders.arrVal.length; i++)
            if (mapTreeOrders.arrVal[i].getFX() == k)
                return i;
        throw new NullPointerException("No such map " + k);
    }

    @Override
    public long getMapOfOrder(int order) {
        return mapTreeOrders.arrVal[order].getFX();
    }
}
