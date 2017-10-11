/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.mapinfos;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.IMapContext;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;
import r48.ui.UINSVertLayout;
import r48.ui.UITreeView;

import java.util.Collections;
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
    private UIScrollLayout uiSVL = new UIScrollLayout(true, FontSizes.generalScrollersize);
    private UITreeView utv = new UITreeView();
    private int selectedOrder = 0;
    private boolean deleteConfirmation = false;
    private boolean enableOrderHoleDebug = false;
    private IMapContext mapContext;

    private IConsumer<SchemaPath> onMapInfoChange = new IConsumer<SchemaPath>() {
        @Override
        public void accept(SchemaPath sp) {
            rebuildList();
        }
    };

    public UIGRMMapInfos(ISupplier<IConsumer<UIElement>> wmg, final IRMLikeMapInfoBackendWPub b, IMapContext context) {
        operators = b;
        mapContext = context;
        b.registerModificationHandler(onMapInfoChange);
        windowMakerGetter = wmg;
        rebuildList();
        allElements.add(uiSVL);
    }

    private void rebuildList() {
        uiSVL.panels.clear();
        LinkedList<Integer> intList = new LinkedList<Integer>(operators.getHashKeys());
        Collections.sort(intList, new Comparator<Integer>() {
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
        LinkedList<UITreeView.TreeElement> tree = new LinkedList<UITreeView.TreeElement>();
        for (final Integer k : intList) {
            final RubyIO map = operators.getHashBID(k);
            final int order = operators.getOrderOfMap(k);
            if (lastOrder < order)
                lastOrder = order;
            final int parent = (int) map.getInstVarBySymbol("@parent_id").fixnumVal;

            String name = map.getInstVarBySymbol("@name").decString();

            if (parent == 0) {
                parentStack.clear();
            } else {
                if (parentStack.lastIndexOf(parent) != -1) {
                    while (parentStack.getLast() != parent)
                        parentStack.removeLast();
                } else {
                    AppMain.launchDialog(FormatSyntax.formatExtended(TXDB.get("Parent Inconsistency Warning @ #A o #B"), new RubyIO().setFX(k), new RubyIO().setFX(order)));
                    enableOrderHoleDebug = true;
                }
            }
            parentStack.add(k);
            String spc = " ";
            if (selectedOrder == order)
                spc = ">";
            if (enableOrderHoleDebug)
                spc = order + spc;
            UIElement elm = new UITextButton(FontSizes.mapInfosTextHeight, spc + k + ":" + name + " P" + parent, new Runnable() {
                @Override
                public void run() {
                    selectedOrder = order;
                    deleteConfirmation = false;
                    mapContext.loadMap(new RubyIO().setFX(k));
                    rebuildList();
                }
            });

            if (selectedOrder == order) {
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
                            rebuildList();
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
            tree.add(new UITreeView.TreeElement(parentStack.size(), operators.getIconForMap(k), elm, new IConsumer<Integer>() {
                @Override
                public void accept(Integer integer) {
                    int orderFrom = integer + 1;
                    if (orderFrom > 0)
                        if (!operators.wouldRelocatingInOrderFail(orderFrom, order + 1)) {
                            selectedOrder = operators.relocateInOrder(orderFrom, order + 1);
                            operators.complete();
                            mapContext.loadMap(new RubyIO().setFX(operators.getMapOfOrder(selectedOrder)));
                            rebuildList();
                        }
                }
            }));
        }
        utv.setElements(tree.toArray(new UITreeView.TreeElement[0]));
        uiSVL.panels.add(utv);
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
                        mapContext.loadMap(new RubyIO().setFX(i));
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
                Collections.sort(orders);
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
        uiSVL.setBounds(uiSVL.getBounds());
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
