/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.mapinfos;

import r48.AppMain;
import r48.RubyIO;
import r48.schema.util.SchemaPath;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Going to have to move it over here
 * Created on 02/06/17.
 */
public class RXPRMLikeMapInfoBackend implements IRMLikeMapInfoBackendWPub, IRMLikeMapInfoBackendWPriv {
    public Runnable modHandler;
    public RubyIO mapInfos = AppMain.objectDB.getObject("MapInfos");

    public static String sNameFromInt(int key) {
        String mapStr = Integer.toString(key);
        while (mapStr.length() < 3)
            mapStr = "0" + mapStr;
        return "Map" + mapStr;
    }

    @Override
    public String nameFromInt(int key) {
        return sNameFromInt(key);
    }

    @Override
    public void registerModificationHandler(Runnable onMapInfoChange) {
        modHandler = onMapInfoChange;
        AppMain.objectDB.registerModificationHandler(mapInfos, onMapInfoChange);
    }

    @Override
    public Set<Integer> getHashKeys() {
        HashSet<Integer> hs = new HashSet<Integer>();
        for (RubyIO rio : mapInfos.hashVal.keySet())
            hs.add((int) rio.fixnumVal);
        return hs;
    }

    @Override
    public RubyIO getHashBID(int k) {
        return mapInfos.getHashVal(new RubyIO().setFX(k));
    }

    @Override
    public int getOrderOfMap(int k) {
        return (int) mapInfos.getHashVal(new RubyIO().setFX(k)).getInstVarBySymbol("@order").fixnumVal;
    }

    @Override
    public int getMapOfOrder(int order) {
        for (Map.Entry<RubyIO, RubyIO> rio : mapInfos.hashVal.entrySet())
            if (rio.getValue().getInstVarBySymbol("@order").fixnumVal == order)
                return (int) rio.getKey().fixnumVal;
        return -1;
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
        AppMain.launchNonRootSchema(mapInfos, "File.MapInfos", mapInfos, "File.MapInfos", new RubyIO().setFX(k), getHashBID(k), "RPG::MapInfo", "M" + k);
    }

    @Override
    public void swapOrders(int orderA, int orderB) {
        int a = getMapOfOrder(orderA);
        int b = getMapOfOrder(orderB);
        RubyIO ao = getHashBID(a).getInstVarBySymbol("@order");
        RubyIO bo = getHashBID(b).getInstVarBySymbol("@order");
        long t = bo.fixnumVal;
        bo.fixnumVal = ao.fixnumVal;
        ao.fixnumVal = t;
    }

    @Override
    public int getLastOrder() {
        int targetOrder = 0;
        for (int m : getHashKeys()) {
            int o = getOrderOfMap(m);
            if (o > targetOrder)
                targetOrder = o;
        }
        return targetOrder;
    }

    @Override
    public void removeMap(int k) {
        MapInfoReparentUtil.removeMapHelperSALT(k, this);
        mapInfos.removeHashVal(new RubyIO().setFX(k));
    }

    @Override
    public int createNewMap(int k) {
        RubyIO mi = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::MapInfo"), new RubyIO().setFX(k));
        int targetOrder = getLastOrder() + 1;
        mi.getInstVarBySymbol("@order").fixnumVal = targetOrder;
        mapInfos.hashVal.put(new RubyIO().setFX(k), mi);
        return targetOrder;
    }

    @Override
    public void complete() {
        AppMain.objectDB.objectRootModified(mapInfos);
        modHandler.run();
    }
}
