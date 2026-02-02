/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.mapinfos;

import r48.R48;
import r48.dbs.ObjectRootHandle;
import r48.io.data.DMKey;
import r48.io.data.DMPath;
import r48.io.data.IRIO;
import r48.schema.util.SchemaPath;
import r48.tr.pages.TrRoot;
import r48.ui.AppUI;
import r48.ui.Art;

import java.util.*;
import java.util.function.Consumer;

/**
 * Going to have to move it over here
 * Created on 02/06/17.
 */
public class RXPRMLikeMapInfoBackend extends R48.Svc implements IRMLikeMapInfoBackendWPub, IRMLikeMapInfoBackendWPriv {
    public Consumer<SchemaPath> modHandler;
    public ObjectRootHandle mapInfos;
    public RXPRMLikeMapInfoBackend(R48 app) {
        super(app);
        mapInfos = app.odb.getObject("MapInfos");
    }

    public static String sNameFromInt(long key) {
        String mapStr = Long.toString(key);
        while (mapStr.length() < 3)
            mapStr = "0" + mapStr;
        return "Map" + mapStr;
    }

    @Override
    public void registerModificationHandler(Consumer<SchemaPath> onMapInfoChange) {
        modHandler = onMapInfoChange;
        mapInfos.registerModificationHandler(onMapInfoChange);
    }

    @Override
    public Set<Long> getHashKeys() {
        HashSet<Long> hs = new HashSet<Long>();
        for (DMKey rio : mapInfos.getObject().getHashKeys())
            hs.add(rio.getFX());
        return hs;
    }

    @Override
    public IRIO getHashBID(long k) {
        return mapInfos.getObject().getHashVal(DMKey.of(k));
    }

    @Override
    public int getOrderOfMap(long k) {
        return (int) mapInfos.getObject().getHashVal(DMKey.of(k)).getIVar("@order").getFX();
    }

    @Override
    public long getMapOfOrder(int order) {
        IRIO obj = mapInfos.getObject();
        for (DMKey rio : obj.getHashKeys())
            if (obj.getHashVal(rio).getIVar("@order").getFX() == order)
                return rio.getFX();
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
    public void triggerEditInfoOf(AppUI U, long k) {
        U.launchSchemaTrace(mapInfos, null, new DMPath.Hash(DMKey.of(k), false));
    }

    @Override
    public void swapOrders(int orderA, int orderB) {
        long a = getMapOfOrder(orderA);
        long b = getMapOfOrder(orderB);
        IRIO ao = getHashBID(a).getIVar("@order");
        IRIO bo = getHashBID(b).getIVar("@order");
        long t = bo.getFX();
        bo.setFX(ao.getFX());
        ao.setFX(t);
    }

    @Override
    public int getLastOrder() {
        int targetOrder = 0;
        for (long m : getHashKeys()) {
            int o = getOrderOfMap(m);
            if (o > targetOrder)
                targetOrder = o;
        }
        return targetOrder;
    }

    @Override
    public void removeMap(long k) {
        MapInfoReparentUtil.removeMapHelperSALT(k, this);
        mapInfos.getObject().removeHashVal(DMKey.of(k));
    }

    @Override
    public int createNewMap(long k) {
        int targetOrder = getLastOrder();
        long l = getMapOfOrder(targetOrder);
        if (l == -1)
            l = 0;
        IRIO mi = mapInfos.getObject().addHashVal(DMKey.of(k));
        SchemaPath.setDefaultValue(mi, app.sdb.getSDBEntry("RPG::MapInfo"), DMKey.of(k));
        mi.getIVar("@parent_id").setFX(l);
        mi.getIVar("@order").setFX(targetOrder + 1);
        return targetOrder;
    }

    @Override
    public void complete() {
        SchemaPath fakePath = new SchemaPath(app.sdb.getSDBEntry("File.MapInfos"), mapInfos);
        mapInfos.objectRootModified(fakePath);
        modHandler.accept(fakePath);
    }

    @Override
    public Art.Symbol getIconForMap(long k) {
        return Art.Symbol.Map;
    }

    @Override
    public String translateToGUM(long k) {
        return sNameFromInt(k);
    }

    @Override
    public String calculateIndentsAndGetErrors(HashMap<Long, Integer> id) {
        StringBuilder errors = new StringBuilder();

        standardCalculateIndentsAndGetErrors(app, this, id, errors, 0);

        return errorsToStringOrNull(app, errors);
    }

    public static void standardCalculateIndentsAndGetErrors(R48 app, final IRMLikeMapInfoBackend backend, HashMap<Long, Integer> id, StringBuilder errors, int standardIndentOffset) {
        LinkedList<Long> maps = new LinkedList<Long>();
        maps.addAll(backend.getHashKeys());

        Collections.sort(maps, new Comparator<Long>() {
            @Override
            public int compare(Long aLong, Long t1) {
                int a = backend.getOrderOfMap(aLong);
                int b = backend.getOrderOfMap(t1);
                if (a < b)
                    return -1;
                if (a > b)
                    return 1;
                return 0;
            }
        });

        final TrRoot T = app.t;
        LinkedList<Long> parentStack = new LinkedList<Long>();
        int lastOrder = 0;
        for (Long map : maps) {
            IRIO data = backend.getHashBID(map);
            final long parent = data.getIVar("@parent_id").getFX();

            if (parent == 0) {
                parentStack.clear();
            } else {
                while ((!parentStack.isEmpty()) && (parentStack.getLast() != parent))
                    parentStack.removeLast();
                if (parentStack.size() == 0) {
                    // Not valid!
                    errors.append(T.m.parentOrderInconsistency).append(" (@").append(map).append(")\n");
                }
            }

            id.put(map, parentStack.size());

            IRIO indent = data.getIVar("@indent");
            // For R2k
            if (indent != null)
                if (indent.getFX() != (parentStack.size() + standardIndentOffset))
                    errors.append(T.m.inconsistentMapIndent).append(map).append('=').append(indent.getFX()).append(" !").append(parentStack.size() + standardIndentOffset).append('\n');

            parentStack.add(map);

            int order = backend.getOrderOfMap(map);
            if (order != (lastOrder + 1))
                errors.append(T.m.inconsistentOrder).append(map).append('\n');
            lastOrder = order;
        }
    }

    public static String errorsToStringOrNull(R48 app, StringBuilder errors) {
        if (errors.length() == 0)
            return null;
        errors.append(app.t.m.resolveManually);
        return errors.toString();
    }
}
