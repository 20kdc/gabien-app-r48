/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.mapinfos;

import gabien.ui.IConsumer;
import r48.AppMain;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.schema.util.SchemaPath;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Just another one of those classes.
 * Created on 02/06/17.
 */
public class R2kRMLikeMapInfoBackend implements IRMLikeMapInfoBackendWPub, IRMLikeMapInfoBackendWPriv {
    public IConsumer<SchemaPath> modHandler;
    public RubyIO mapTree = AppMain.objectDB.getObject("RPG_RT.lmt");
    // Note: The orders table is [order] = map.
    // So swapping orders is probably the easiest operation here.
    public RubyIO mapTreeHash = mapTree.getInstVarBySymbol("@map_infos");
    public RubyIO mapTreeOrders = mapTree.getInstVarBySymbol("@map_order");

    @Override
    public void swapOrders(int orderA, int orderB) {
        RubyIO b = mapTreeOrders.arrVal[orderA];
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

    @Override
    public String nameFromInt(int i) {
        return sNameFromInt(i);
    }

    @Override
    public void registerModificationHandler(IConsumer<SchemaPath> onMapInfoChange) {
        modHandler = onMapInfoChange;
        AppMain.objectDB.registerModificationHandler(mapTree, onMapInfoChange);
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
    public void triggerEditInfoOf(int k) {
        AppMain.launchNonRootSchema(mapTree, "RPG::MapTree", new RubyIO().setFX(k), getHashBID(k), "RPG::MapInfo", "M" + k, null);
    }

    @Override
    public void removeMap(int k) {
        // Prepare...
        MapInfoReparentUtil.removeMapHelperSALT(k, this);
        // Remove last from array
        RubyIO[] resArray = new RubyIO[mapTreeOrders.arrVal.length - 1];
        System.arraycopy(mapTreeOrders.arrVal, 0, resArray, 0, resArray.length);
        mapTreeOrders.arrVal = resArray;
        // Eliminate from hash
        mapTreeHash.removeHashVal(new RubyIO().setFX(k));
    }

    @Override
    public int createNewMap(int k) {
        int targetOrder = mapTreeOrders.arrVal.length - 1;
        int l = getMapOfOrder(targetOrder);
        if (l == -1)
            l = 0;
        RubyIO mi = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::MapInfo"), new RubyIO().setFX(k));
        mi.getInstVarBySymbol("@parent_id").fixnumVal = l;

        RubyIO[] resArray = new RubyIO[mapTreeOrders.arrVal.length + 1];
        System.arraycopy(mapTreeOrders.arrVal, 0, resArray, 0, mapTreeOrders.arrVal.length);
        resArray[resArray.length - 1] = new RubyIO().setFX(k);
        mapTreeHash.hashVal.put(new RubyIO().setFX(k), mi);
        mapTreeOrders.arrVal = resArray;
        return resArray.length - 1;
    }

    @Override
    public void complete() {
        // Need to update indent...
        getHashBID(0).getInstVarBySymbol("@indent").fixnumVal = 0;
        LinkedList<Integer> intList = new LinkedList<Integer>(getHashKeys());
        intList.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer t0, Integer t1) {
                t0 = getOrderOfMap(t0);
                t1 = getOrderOfMap(t1);
                if (t0 > t1)
                    return 1;
                if (t0 < t1)
                    return -1;
                return 0;
            }
        });
        LinkedList<Integer> parentStack = new LinkedList<Integer>();
        int lastOrder = 0;
        for (final Integer k : intList) {
            final RubyIO map = getHashBID(k);
            final int order = getOrderOfMap(k);
            if (lastOrder < order)
                lastOrder = order;
            final int parent = (int) map.getInstVarBySymbol("@parent_id").fixnumVal;
            if (parent == 0) {
                parentStack.clear();
            } else {
                if (parentStack.lastIndexOf(parent) != -1)
                    while (parentStack.getLast() != parent)
                        parentStack.removeLast();
            }
            parentStack.add(k);
            map.getInstVarBySymbol("@indent").fixnumVal = parentStack.size();
        }
        // and done!
        SchemaPath fakePath = new SchemaPath(AppMain.schemas.getSDBEntry("File.RPG_RT.lmt"), mapTree, null);
        AppMain.objectDB.objectRootModified(mapTree, fakePath);
        modHandler.accept(fakePath);
    }

    @Override
    public Set<Integer> getHashKeys() {
        // The job of this is to hide that there *ever was* a Map 0.
        // Map 0 is reserved.
        HashSet<Integer> hs = new HashSet<Integer>();
        for (RubyIO i : mapTreeHash.hashVal.keySet())
            if (i.fixnumVal != 0)
                hs.add((int) i.fixnumVal);
        return hs;
    }

    @Override
    public RubyIO getHashBID(int k) {
        return mapTreeHash.getHashVal(new RubyIO().setFX(k));
    }

    @Override
    public int getOrderOfMap(int k) {
        for (int i = 0; i < mapTreeOrders.arrVal.length; i++)
            if (mapTreeOrders.arrVal[i].fixnumVal == k)
                return i;
        throw new NullPointerException("No such map " + k);
    }

    @Override
    public int getMapOfOrder(int order) {
        return (int) mapTreeOrders.arrVal[order].fixnumVal;
    }
}
