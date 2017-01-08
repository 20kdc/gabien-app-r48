/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package gabienapp;

import gabien.ui.*;
import gabienapp.schema.util.SchemaPath;
import gabienapp.ui.UIAppendButton;
import gabienapp.ui.UIScrollVertLayout;
import gabienapp.ui.UITextPrompt;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Helps jump between maps.
 * Should eventually replace the arrow-keys system.
 * Note that as this is a tab, it will not self-deregister.
 * Created on 1/1/17.
 */
public class UIMapInfos extends UIPanel {
    private UIScrollVertLayout uiSVL;
    private int selectedOrder = 0;
    private boolean deleteConfirmation = false;
    private boolean enableOrderHoleDebug = false;
    private Runnable onMapInfoChange = new Runnable() {
        @Override
        public void run() {
            rebuildList();
        }
    };
    private final RubyIO mapInfos;

    private final ISupplier<IConsumer<UIElement>> windowMakerGetter;

    public UIMapInfos(ISupplier<IConsumer<UIElement>> wmg) {
        mapInfos = Application.objectDB.getObject("MapInfos");
        windowMakerGetter = wmg;
        uiSVL = new UIScrollVertLayout();
        rebuildList();
        allElements.add(uiSVL);
        Application.objectDB.registerModificationHandler(mapInfos, onMapInfoChange);
    }

    private void rebuildList() {
        uiSVL.panels.clear();
        LinkedList<Integer> intList = new LinkedList<Integer>();
        // Only used when the key is explicitly needed
        final HashMap<Integer, RubyIO> intToKey = new HashMap<Integer, RubyIO>();
        for (RubyIO i : mapInfos.hashVal.keySet()) {
            intList.add((int) i.fixnumVal);
            intToKey.put((int) i.fixnumVal, i);
        }
        intList.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer t0, Integer t1) {
                RubyIO a = mapInfos.getHashVal(new RubyIO().setFX(t0));
                RubyIO b = mapInfos.getHashVal(new RubyIO().setFX(t1));
                t0 = (int) a.getInstVarBySymbol("@order").fixnumVal;
                t1 = (int) b.getInstVarBySymbol("@order").fixnumVal;
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
            final RubyIO map = mapInfos.getHashVal(new RubyIO().setFX(k));
            final int order = (int) map.getInstVarBySymbol("@order").fixnumVal;
            if (lastOrder < order)
                lastOrder = order;
            final int parent = (int) map.getInstVarBySymbol("@parent_id").fixnumVal;

            String name = map.getInstVarBySymbol("@name").decString();
            String spc = "";

            if (parent == 0) {
                parentStack.clear();
            } else {
                if (parentStack.lastIndexOf(parent) != -1) {
                    while (parentStack.getLast() != parent)
                        parentStack.removeLast();
                } else {
                    windowMakerGetter.get().accept(new UILabel("Parent Inconsistency Warning @ " + k + " o " + order, false));
                    enableOrderHoleDebug = true;
                }
            }
            parentStack.add(k);
            for (int i = 0; i < (parentStack.size() - 1); i++)
                spc += " ";
            if (selectedOrder == order) {
                spc = ">" + spc;
            } else {
                spc = " " + spc;
            }
            if (enableOrderHoleDebug)
                spc = order + spc;
            UIElement elm = new UITextButton(false, spc + k + ":" + name + " P" + parent, new Runnable() {
                @Override
                public void run() {
                    selectedOrder = order;
                    Application.loadMap(k);
                    rebuildList();
                }
            });
            if (selectedOrder != order) {
                if (selectedOrder != 0)
                    if (!wouldRelocatingInOrderFail(selectedOrder, order + 1)) {
                        elm = new UIAppendButton(">>", elm, new Runnable() {
                            @Override
                            public void run() {
                                selectedOrder = relocateInOrder(selectedOrder, order + 1);
                                Application.objectDB.objectRootModified(mapInfos);
                                rebuildList();
                            }
                        }, false);
                    }
            } else {
                if (parent != 0) {
                    // This used to be two operations, but, eh.
                    elm = new UIAppendButton("Move Out ", elm, new Runnable() {
                        @Override
                        public void run() {
                            final int parentLastOrder = findChildrenLastOrder(parent, getMapByOrder(parent).getValue());
                            // Does it need to be moved to the bottom
                            if (!mapInPath(k, (int) getMapByOrder(parentLastOrder).getKey().fixnumVal))
                                selectedOrder = relocateInOrder(selectedOrder, parentLastOrder + 1);
                            map.getInstVarBySymbol("@parent_id").fixnumVal = mapInfos.getHashVal(new RubyIO().setFX(parent)).getInstVarBySymbol("@parent_id").fixnumVal;
                            Application.objectDB.objectRootModified(mapInfos);
                            rebuildList();
                        }
                    }, false);
                }
                elm = new UIAppendButton("Edit Info. ", elm, new Runnable() {
                    @Override
                    public void run() {
                        Application.launchNonRootSchema(mapInfos, "File.MapInfos", mapInfos, "File.MapInfos", new RubyIO().setFX(k), map, "RPG::MapInfo", "M" + k);
                    }
                }, false);
                if (deleteConfirmation) {
                    elm = new UIAppendButton("Delete!", elm, new Runnable() {
                        @Override
                        public void run() {
                            // Deal with child nodes first so that last order WILL equal first order
                            for (RubyIO rio : mapInfos.hashVal.values())
                                if (rio.getInstVarBySymbol("@parent_id").fixnumVal == k)
                                    rio.getInstVarBySymbol("@parent_id").fixnumVal = parent;
                            // Remove this map
                            mapInfos.hashVal.remove(intToKey.get(k));
                            // And shift the orders.
                            performOrderShift(order, -1, -1);
                            Application.objectDB.objectRootModified(mapInfos);
                            rebuildList();
                        }
                    }, false);
                } else {
                    elm = new UIAppendButton("Delete?", elm, new Runnable() {
                        @Override
                        public void run() {
                            deleteConfirmation = true;
                            rebuildList();
                        }
                    }, false);
                }
            }
            uiSVL.panels.add(elm);
        }
        final int fLastOrder = lastOrder;
        uiSVL.panels.add(new UITextButton(false, "<Insert New Map>", new Runnable() {
            @Override
            public void run() {
                windowMakerGetter.get().accept(new UITextPrompt("Map ID?", new IConsumer<String>() {
                    @Override
                    public void accept(String s) {
                        int i = Integer.parseInt(s);
                        RubyIO key = new RubyIO().setFX(i);
                        if (mapInfos.getHashVal(key) != null) {
                            windowMakerGetter.get().accept(new UILabel("That ID is already in use.", false));
                            return;
                        }
                        RubyIO mi = SchemaPath.createDefaultValue(Application.schemas.getSDBEntry("RPG::MapInfo"), key);
                        mi.getInstVarBySymbol("@order").fixnumVal = fLastOrder + 1;
                        mapInfos.hashVal.put(key, mi);
                        Application.objectDB.objectRootModified(mapInfos);
                        rebuildList();
                    }
                }));
            }
        }));
        uiSVL.panels.add(new UITextButton(false, "<Test Sequence Consistency>", new Runnable() {
            @Override
            public void run() {
                LinkedList<Integer> orders = new LinkedList<Integer>();
                for (RubyIO map : mapInfos.hashVal.values())
                    orders.add((int) map.getInstVarBySymbol("@order").fixnumVal);
                orders.sort(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer a, Integer b) {
                        // Really? I have to implement this myself?
                        if (a < b)
                            return -1;
                        if (a > b)
                            return 1;
                        return 0;
                    }
                });
                String message = "The MapInfos database is sequential.";
                int lastOrder = 0;
                for (int i : orders) {
                    if (i != (lastOrder + 1)) {
                        if (i <= lastOrder) {
                            message = "The entries in the MapInfos database contain duplicates. (@" + i + ")";
                            enableOrderHoleDebug = true;
                            break;
                        } else {
                            message = "The entries in the MapInfos database contain holes. (@" + i + ")";
                            enableOrderHoleDebug = true;
                            break;
                        }
                    }
                    lastOrder = i;
                }
                windowMakerGetter.get().accept(new UILabel(message, false));
                rebuildList();
            }
        }));
        deleteConfirmation = false;
    }

    private boolean mapInPath(int mapId, int pathInnermostId) {
        while (pathInnermostId != 0) {
            if (pathInnermostId == mapId)
                return true;
            RubyIO path = mapInfos.getHashVal(new RubyIO().setFX(pathInnermostId));
            pathInnermostId = (int) path.getInstVarBySymbol("@parent_id").fixnumVal;
        }
        return false;
    }

    // Note: shiftStart is the first order to be moved,
    //       so order >= shiftStart.
    // Also note that this doesn't perform the rebuild notification.
    // Make sure to do that.
    private void performOrderShift(int shiftStart, int shiftAmount, int shiftDir) {
        for (RubyIO v : mapInfos.hashVal.values()) {
            RubyIO order = v.getInstVarBySymbol("@order");
            if (order.fixnumVal >= shiftStart)
                if ((shiftAmount == -1) || (order.fixnumVal < (shiftStart + shiftAmount)))
                order.fixnumVal += shiftDir;
        }
    }

    private Map.Entry<RubyIO, RubyIO> getMapByOrder(int order) {
        for (Map.Entry<RubyIO, RubyIO> v : mapInfos.hashVal.entrySet())
            if (v.getValue().getInstVarBySymbol("@order").fixnumVal == order)
                return v;
        return null;
    }

    // upper logic of the next function cut out.
    boolean wouldRelocatingInOrderFail(int orderA, int orderB) {
        // This is one of those methods where not documenting it would be more of a pain
        //  than documenting it.
        Map.Entry<RubyIO, RubyIO> map = getMapByOrder(orderA);
        if (map == null)
            return true;
        // When moving a tree about,
        //  the tree can (thankfully) be considered as one contiguous section.
        // However, the tree has to be known first.
        int lastOrder = findChildrenLastOrder(map.getKey().fixnumVal, map.getValue());
        // Essentially, what needs to happen here is:
        // 1. Detect if the place we want to go is inside where we are,
        //     or in exactly the same place,
        //     in which case just do nothing.
        if (orderB > orderA)
            if (orderB <= (lastOrder + 1))
                return true;
        return false;
    }

    // Relocates a map in the order, giving it the parent of the map directly above it (or -1).
    // This guarantees consistency - further edits can be performed afterwards.
    // Note that orderA is the map-in-order to target, and orderB is the place it needs to go.
    // Testing of this operation is best performed by hitting the buttons until something breaks.
    private int relocateInOrder(int orderA, int orderB) {
        // This is one of those methods where not documenting it would be more of a pain
        //  than documenting it.
        Map.Entry<RubyIO, RubyIO> map = getMapByOrder(orderA);
        if (map == null)
            return orderA;
        // When moving a tree about,
        //  the tree can (thankfully) be considered as one contiguous section.
        // However, the tree has to be known first.
        int lastOrder = findChildrenLastOrder(map.getKey().fixnumVal, map.getValue());
        // Essentially, what needs to happen here is:
        // 1. Detect if the place we want to go is inside where we are,
        //     or in exactly the same place,
        //     in which case just do nothing.
        if (orderB > orderA)
            if (orderB <= (lastOrder + 1))
                return orderA; // Do nothing

        // 2. A pocket of space needs to be created of the right size where the map should go.
        //    (making sure to adjust orderA as required)
        int shiftAmount = (lastOrder - orderA) + 1;
        performOrderShift(orderB, -1, shiftAmount);
        if (orderB < orderA)
            orderA += shiftAmount;

        // 3. Specifically shift the orders into place, leaving a hole in the order table
        performOrderShift(orderA, shiftAmount, orderB - orderA);
        // 4. Correct that hole
        performOrderShift(orderA + shiftAmount, -1, -shiftAmount);
        // 5. Fix parenting. Orders start from 1.
        long newParent = 0;
        int newOrder = (int) map.getValue().getInstVarBySymbol("@order").fixnumVal;
        if (newOrder > 1) {
            Map.Entry<RubyIO, RubyIO> prevMap = getMapByOrder(newOrder - 1);
            if (prevMap != null)
                newParent = prevMap.getKey().fixnumVal;
        }
        map.getValue().getInstVarBySymbol("@parent_id").fixnumVal = newParent;
        return newOrder;
    }

    private int findChildrenLastOrder(long mapId, RubyIO map) {
        int order = (int) (map.getInstVarBySymbol("@order").fixnumVal);

        // Recursively find the highest order, which ensures the whole tree will be moved.
        for (Map.Entry<RubyIO, RubyIO> v : mapInfos.hashVal.entrySet())
            if (v.getValue().getInstVarBySymbol("@parent_id").fixnumVal == mapId)
                order = Math.max(order, findChildrenLastOrder(v.getKey().fixnumVal));
        return order;
    }

    private int findChildrenLastOrder(long mapId) {
        return findChildrenLastOrder(mapId, mapInfos.getHashVal(new RubyIO().setFX(mapId)));
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        uiSVL.setBounds(new Rect(0, 0, r.width, r.height));
    }
}
