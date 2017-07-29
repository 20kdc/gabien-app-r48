/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.mapinfos;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.map.UIMapViewContainer;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;
import gabien.ui.UIScrollLayout;
import r48.ui.UINSVertLayout;
import r48.ui.UITextPrompt;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * Helps jump between maps.
 * Should eventually replace the arrow-keys system.
 * Note that as this is a tab, it will not self-deregister.
 * <p/>
 * Created on 1/1/17. Copied for Generic RM Map Infos on Jun 2 2017.
 */
public class UIGRMMapInfos extends UIPanel {
    private final IRMLikeMapInfoBackendWPub operators;
    private final ISupplier<IConsumer<UIElement>> windowMakerGetter;
    private final IConsumer<Integer> mapLoader;
    private UIScrollLayout uiSVL;
    private int selectedOrder = 0;
    private boolean deleteConfirmation = false;
    private boolean enableOrderHoleDebug = false;
    private IConsumer<SchemaPath> onMapInfoChange = new IConsumer<SchemaPath>() {
        @Override
        public void accept(SchemaPath sp) {
            rebuildList();
        }
    };

    public UIGRMMapInfos(ISupplier<IConsumer<UIElement>> wmg, final UIMapViewContainer mapBox, final IRMLikeMapInfoBackendWPub b) {
        operators = b;
        b.registerModificationHandler(onMapInfoChange);
        windowMakerGetter = wmg;
        mapLoader = new IConsumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                mapBox.loadMap(operators.nameFromInt(integer));
            }
        };
        uiSVL = new UIScrollLayout(true);
        rebuildList();
        allElements.add(uiSVL);
    }

    private void rebuildList() {
        uiSVL.panels.clear();
        LinkedList<Integer> intList = new LinkedList<Integer>(operators.getHashKeys());
        intList.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer t0, Integer t1) {
                t0 = operators.getOrderOfMap(t0);
                t1 = operators.getOrderOfMap(t1);
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
            final RubyIO map = operators.getHashBID(k);
            final int order = operators.getOrderOfMap(k);
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
                    AppMain.launchDialog(FormatSyntax.formatExtended(TXDB.get("Parent Inconsistency Warning @ #A o #B"), new RubyIO[] {new RubyIO().setFX(k), new RubyIO().setFX(order)}));
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
            UIElement elm = new UITextButton(FontSizes.mapInfosTextHeight, spc + k + ":" + name + " P" + parent, new Runnable() {
                @Override
                public void run() {
                    selectedOrder = order;
                    deleteConfirmation = false;
                    mapLoader.accept(k);
                    rebuildList();
                }
            });

            if (selectedOrder != order) {
                if (selectedOrder != 0)
                    if (!operators.wouldRelocatingInOrderFail(selectedOrder, order + 1)) {
                        elm = new UIAppendButton(TXDB.get("Parent Here"), elm, new Runnable() {
                            @Override
                            public void run() {
                                selectedOrder = operators.relocateInOrder(selectedOrder, order + 1);
                                operators.complete();
                            }
                        }, FontSizes.mapInfosTextHeight);
                    }
            } else {
                if (parent != 0) {
                    // This used to be two operations, but, eh.
                    elm = new UIAppendButton(TXDB.get("Move Out "), elm, new Runnable() {
                        @Override
                        public void run() {
                            final int parentLastOrder = MapInfoReparentUtil.findChildrenLastOrder(parent, operators);
                            // Firstly, keep in mind relocateInOrder orders are relative to the CURRENT STATE.
                            // This is what keeps them relatively consistent to use.
                            // So, anyway. If *the selected order* is on the path to the parent's last order,
                            // that is, a case such as:
                            // 1
                            //  2
                            //  3 <selected!>
                            //   4
                            // then relocating is going to act weird because relative to the current state we're trying to make a reference loop.
                            // But that's fine, since we're at the end anyway.
                            // 1
                            //  2
                            // 3 <selected!>
                            //  4
                            // Just nudge the parent_id.
                            // Meanwhile for this:
                            // 1
                            //  2 <selected!>
                            //  3
                            //   4
                            // We need to move to the bottom...
                            // 1
                            //  3
                            //   4
                            //    2 <selected!>
                            // And then fix the parent_id.
                            // 1
                            //  3
                            //   4
                            // 2 <selected!>
                            // relocateInOrder will handle the case of 2 having any children.
                            // (In the previous case of 3, which did have children, it was just a parent_id nudge anyway)

                            if (!mapInPath(k, operators.getMapOfOrder(parentLastOrder)))
                                selectedOrder = operators.relocateInOrder(selectedOrder, parentLastOrder + 1);
                            map.getInstVarBySymbol("@parent_id").fixnumVal = operators.getHashBID(parent).getInstVarBySymbol("@parent_id").fixnumVal;
                            operators.complete();
                        }
                    }, FontSizes.mapInfosTextHeight);
                }
                elm = new UIAppendButton(TXDB.get("Edit Info. "), elm, new Runnable() {
                    @Override
                    public void run() {
                        operators.triggerEditInfoOf(k);
                    }
                }, FontSizes.mapInfosTextHeight);
                if (deleteConfirmation) {
                    elm = new UIAppendButton(TXDB.get("Delete!"), elm, new Runnable() {
                        @Override
                        public void run() {
                            // Orphan/move up child nodes first
                            for (Integer rk : operators.getHashKeys()) {
                                RubyIO rio = operators.getHashBID(rk);
                                if (rio.getInstVarBySymbol("@parent_id").fixnumVal == k)
                                    rio.getInstVarBySymbol("@parent_id").fixnumVal = parent;
                            }
                            operators.removeMap(k);
                            operators.complete();
                        }
                    }, FontSizes.mapInfosTextHeight);
                } else {
                    elm = new UIAppendButton(TXDB.get("Delete?"), elm, new Runnable() {
                        @Override
                        public void run() {
                            deleteConfirmation = true;
                            rebuildList();
                        }
                    }, FontSizes.mapInfosTextHeight);
                }
            }
            uiSVL.panels.add(elm);
        }

        uiSVL.panels.add(new UITextButton(FontSizes.mapInfosTextHeight, TXDB.get("<Insert New Map>"), new Runnable() {
            @Override
            public void run() {
                final UINumberBox num = new UINumberBox(FontSizes.textDialogFieldTextHeight);
                UIAppendButton prompt = new UIAppendButton(TXDB.get("Confirm"), num, new Runnable() {
                    @Override
                    public void run() {
                        int i = num.number;
                        if (operators.getHashBID(i) != null) {
                            AppMain.launchDialog(TXDB.get("That ID is already in use."));
                            return;
                        }
                        selectedOrder = operators.createNewMap(i);
                        operators.complete();
                        mapLoader.accept(i);
                        rebuildList();
                    }
                }, FontSizes.textDialogFieldTextHeight);
                UINSVertLayout dialog = new UINSVertLayout(prompt, new UITextButton(FontSizes.textDialogFieldTextHeight, TXDB.get("Find unused ID."), new Runnable() {
                    @Override
                    public void run() {
                        int i = 1;
                        while (operators.getHashBID(i) != null)
                            i++;
                        num.number = i;
                    }
                })) {
                    @Override
                    public String toString() {
                        return TXDB.get("Map ID?");
                    }
                };
                windowMakerGetter.get().accept(dialog);
            }
        }));
        uiSVL.panels.add(new UITextButton(FontSizes.mapInfosTextHeight, TXDB.get("<Test Sequence Consistency>"), new Runnable() {
            @Override
            public void run() {
                LinkedList<Integer> orders = new LinkedList<Integer>();
                for (Integer map : operators.getHashKeys())
                    orders.add(operators.getOrderOfMap(map));
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
                String message = TXDB.get("The MapInfos database is sequential.");
                int lastOrder = 0;
                for (int i : orders) {
                    if (i != (lastOrder + 1)) {
                        if (i <= lastOrder) {
                            message = TXDB.get("The entries in the MapInfos database contain duplicates.") + " (@" + i + ")";
                            enableOrderHoleDebug = true;
                            break;
                        } else {
                            message = TXDB.get("The entries in the MapInfos database contain holes.") + " (@" + i + ")";
                            enableOrderHoleDebug = true;
                            break;
                        }
                    }
                    lastOrder = i;
                }
                AppMain.launchDialog(message);
                rebuildList();
            }
        }));
        deleteConfirmation = false;
    }

    // Is a map part of the chain from pathInnermostId to root?
    private boolean mapInPath(int mapId, int pathInnermostId) {
        while (pathInnermostId != 0) {
            if (pathInnermostId == mapId)
                return true;
            RubyIO path = operators.getHashBID(pathInnermostId);
            pathInnermostId = (int) path.getInstVarBySymbol("@parent_id").fixnumVal;
        }
        return false;
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        uiSVL.setBounds(new Rect(0, 0, r.width, r.height));
    }
}
