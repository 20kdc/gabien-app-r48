/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.mapinfos;

import gabien.GaBIEnUI;
import gabien.ui.*;
import r48.App;
import r48.IMapContext;
import r48.io.data.IRIO;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;
import r48.ui.UINSVertLayout;
import r48.ui.UITreeView;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Helps jump between maps.
 * Should eventually replace the arrow-keys system.
 * Note that as this is a tab, it will not self-deregister.
 * <p/>
 * Created on 1/1/17. Copied for Generic RM Map Infos on Jun 2 2017.
 */
public class UIGRMMapInfos extends App.Prx {
    private final IRMLikeMapInfoBackendWPub operators;
    private final UIScrollLayout uiSVL = new UIScrollLayout(true, app.f.generalS);
    private final UITreeView utv;
    private final UITextBox searchBarBox = new UITextBox("", app.f.mapInfosTH);
    private final UISplitterLayout searchBar = new UISplitterLayout(new UILabel("Search: ", app.f.mapInfosTH), searchBarBox, false, 0); 
    private String lastSearchTerm = "";
    private int selectedOrder = 0;
    private IMapContext mapContext;
    private HashSet<Long> notExpanded = new HashSet<Long>();
    private String toStringRes;

    // Cannot actually be converted to local variable due to reference issues
    private Consumer<SchemaPath> onMapInfoChange = new Consumer<SchemaPath>() {
        @Override
        public void accept(SchemaPath sp) {
            rebuildList();
        }
    };

    public UIGRMMapInfos(final IRMLikeMapInfoBackendWPub b, IMapContext context, String mapInfos) {
        super(context.getApp());
        utv = new UITreeView(UIBorderedElement.getBorderedTextHeight(GaBIEnUI.sysThemeRoot.getTheme(), app.f.mapInfosTH));
        operators = b;
        mapContext = context;
        toStringRes = mapInfos;
        b.registerModificationHandler(onMapInfoChange);
        searchBarBox.onEdit = new Runnable() {
            @Override
            public void run() {
                lastSearchTerm = searchBarBox.text;
                rebuildList();
            }
        };
        rebuildList();
        proxySetElement(uiSVL, true);
    }

    private void rebuildList() {
        uiSVL.panelsClear();

        HashMap<Long, Integer> indent = new HashMap<Long, Integer>();
        String errors = operators.calculateIndentsAndGetErrors(indent);
        if (errors != null) {
            uiSVL.panelsAdd(new UILabel(errors, app.f.mapInfosTH));
            return;
        }

        LinkedList<Long> intList = new LinkedList<Long>(operators.getHashKeys());
        Collections.sort(intList, new Comparator<Long>() {
            @Override
            public int compare(Long p0, Long p1) {
                int t0 = operators.getOrderOfMap(p0);
                int t1 = operators.getOrderOfMap(p1);
                if (t0 > t1)
                    return 1;
                if (t0 < t1)
                    return -1;
                return 0;
            }
        });
        int lastOrder = 0;
        LinkedList<UITreeView.TreeElement> tree = new LinkedList<UITreeView.TreeElement>();
        final boolean searching = !lastSearchTerm.equals("");
        for (final Long k : intList) {
            final IRIO map = operators.getHashBID(k);
            final int order = operators.getOrderOfMap(k);
            if (lastOrder < order)
                lastOrder = order;
            final long parent = map.getIVar("@parent_id").getFX();

            String name = map.getIVar("@name").decString();

            if (searching && !name.contains(lastSearchTerm))
                continue;
            
            UIElement elm = extractedElement(k, map, order, parent, name);
            tree.add(new UITreeView.TreeElement(indent.get(k), operators.getIconForMap(k), elm, new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) {
                    if (searching)
                        return;
                    int orderFrom = integer + 1;
                    if (orderFrom > 0)
                        if (!operators.wouldRelocatingInOrderFail(orderFrom, order + 1)) {
                            selectedOrder = operators.relocateInOrder(orderFrom, order + 1);
                            operators.complete();
                            mapContext.loadMap(operators.translateToGUM(operators.getMapOfOrder(selectedOrder)));
                            rebuildList();
                        }
                }
            }, searching || !notExpanded.contains(k), new Runnable() {
                @Override
                public void run() {
                    if (searching)
                        return;
                    if (notExpanded.contains(k)) {
                        notExpanded.remove(k);
                    } else {
                        notExpanded.add(k);
                    }
                    rebuildList();
                }
            }));
        }
        utv.setElements(tree.toArray(new UITreeView.TreeElement[0]));
        uiSVL.panelsAdd(searchBar);
        uiSVL.panelsAdd(utv);
        uiSVL.panelsAdd(new UITextButton(T.m.bNewMap, app.f.mapInfosTH, new Runnable() {
            @Override
            public void run() {
                final UINumberBox num = new UINumberBox(0, app.f.textDialogFieldTH);
                final Runnable unusedID = new Runnable() {
                    @Override
                    public void run() {
                        int i = 1;
                        while (operators.getHashBID(i) != null)
                            i++;
                        num.number = i;
                    }
                };
                unusedID.run();
                final AtomicBoolean close = new AtomicBoolean(false);
                UIAppendButton prompt = new UIAppendButton(T.g.bConfirm, num, new Runnable() {
                    @Override
                    public void run() {
                        long i = num.number;
                        if (operators.getHashBID(i) != null) {
                            app.ui.launchDialog(T.m.dIDInUse);
                            return;
                        }
                        selectedOrder = operators.createNewMap(i);
                        operators.complete();
                        mapContext.loadMap(operators.translateToGUM(i));
                        rebuildList();
                        close.set(true);
                    }
                }, app.f.textDialogFieldTH);
                UINSVertLayout dialog = new UINSVertLayout(prompt, new UITextButton(T.m.bFindUnusedID, app.f.textDialogFieldTH, unusedID)) {
                    @Override
                    public String toString() {
                        return T.m.dMapID;
                    }

                    @Override
                    public boolean requestsUnparenting() {
                        return close.get();
                    }
                };
                mapContext.getApp().ui.wm.createWindow(dialog);
            }
        }));
    }

    private UIElement extractedElement(final Long k, final IRIO map, final int order, final long parent, String name) {
        UIElement elm = new UITextButton(k + ":" + name + " P" + parent, app.f.mapInfosTH, new Runnable() {
            @Override
            public void run() {
                selectedOrder = order;
                mapContext.loadMap(operators.translateToGUM(k));
                rebuildList();
            }
        }).togglable(selectedOrder == order);

        if (selectedOrder == order) {
            if (parent != 0) {
                // This used to be two operations, but, eh.
                elm = new UIAppendButton(T.m.bMoveOut, elm, new Runnable() {
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
                        map.getIVar("@parent_id").setFX(operators.getHashBID(parent).getIVar("@parent_id").getFX());
                        operators.complete();
                    }
                }, app.f.mapInfosTH);
            }
            elm = new UIAppendButton(T.m.bEditInfo, elm, new Runnable() {
                @Override
                public void run() {
                    operators.triggerEditInfoOf(k);
                }
            }, app.f.mapInfosTH);
            elm = new UIAppendButton(app, T.m.bDelete, elm, null, new String[] {T.g.bConfirm}, new Runnable[] {new Runnable() {
                @Override
                public void run() {
                    // Orphan/move up child nodes first
                    for (Long rk : operators.getHashKeys()) {
                        IRIO rio = operators.getHashBID(rk);
                        if (rio.getIVar("@parent_id").getFX() == k)
                            rio.getIVar("@parent_id").setFX(parent);
                    }
                    operators.removeMap(k);
                    operators.complete();
                    rebuildList();
                }
            }}, app.f.mapInfosTH);
        }
        return elm;
    }

    @Override
    public String toString() {
        return toStringRes;
    }

    // Is a map part of the chain from pathInnermostId to root?
    private boolean mapInPath(long mapId, long pathInnermostId) {
        while (pathInnermostId != 0) {
            if (pathInnermostId == mapId)
                return true;
            IRIO path = operators.getHashBID(pathInnermostId);
            pathInnermostId = path.getIVar("@parent_id").getFX();
        }
        return false;
    }
}
