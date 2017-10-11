/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.IGrInDriver;
import gabien.ui.IConsumer;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import r48.AppMain;

import java.util.HashSet;

/**
 * NOTE: This must be recreated every time it needs to be reloaded, and needs to be contained in a UIScrollViewLayout to work properly.
 * Created on 11/08/17.
 */
public class UITreeView extends UIPanel {
    private TreeElement[] elements = new TreeElement[0];
    private int nodeWidth = 8;
    private int dragBaseX = 0, dragBaseY = 0;
    private boolean dragCursorEnable = false;
    private int dragBase = -1;

    public UITreeView() {
    }

    // NOTE: Run UIScrollLayout setBounds(getBounds) after this.
    public void setElements(TreeElement[] e) {
        allElements.clear();
        elements = e;
    }

    @Override
    public void setBounds(Rect r) {
        allElements.clear();
        int y = 0;
        nodeWidth = 0;
        int total = 0;
        for (TreeElement te : elements) {
            // Bad IDE warning! This makes total sense - it's averaging the node heights to get the aspect ratio around about right.
            nodeWidth += te.innerElement.getBounds().height;
            total++;
        }
        if (total != 0)
            nodeWidth /= total;
        TreeElement lastElement = null; // to set hasChildren
        int invisibleAboveIndent = 0x7FFFFFFF; // to hide unexpanded nodes
        for (TreeElement te : elements) {
            te.visible = te.indent <= invisibleAboveIndent;
            if (lastElement != null)
                if (te.indent > lastElement.indent)
                    lastElement.hasChildren = true;
            int x = nodeWidth * (te.indent + 1);
            int h = te.innerElement.getBounds().height;
            te.innerElement.setBounds(new Rect(x, y, r.width - x, h));
            if (!te.visible)
                h = 0;
            y += h;
            te.h = h;
            if (te.visible)
                allElements.add(te.innerElement);
            lastElement = te;
            // If we're visible, set invisibleAboveIndent.
            // If expanded, then set it to "infinite"
            // Otherwise, set it to current indent, so this code will only run again on a indent <= this one
            if (te.visible)
                invisibleAboveIndent = te.expanded ? 0x7FFFFFFF : te.indent;
        }
        super.setBounds(new Rect(r.x, r.y, r.width, y));
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean select, IGrInDriver igd) {
        super.updateAndRender(ox, oy, deltaTime, select, igd);

        int size = 4;
        int base = 32;
        if (nodeWidth >= 8) {
            size = 8;
            base = 52;
        }
        int y = 0;
        HashSet<Integer> continuingLines = new HashSet<Integer>();
        for (int i = 0; i < elements.length; i++) {
            TreeElement te = elements[i];
            if (!te.visible)
                continue;

            int pico = 2;
            boolean lastInSect = true;
            for (int j = i + 1; j < elements.length; j++) {
                if (elements[j].indent == te.indent) {
                    lastInSect = false;
                    break;
                }
                if (elements[j].indent < te.indent)
                    break;
            }
            if (lastInSect) {
                pico = 3;
                continuingLines.remove(te.indent - 1);
            } else {
                continuingLines.add(te.indent - 1);
            }

            for (int j = 0; j < te.indent; j++) {
                if (j == (te.indent - 1)) {
                    igd.blitScaledImage(pico * size, base, size, size, ox + (j * nodeWidth), oy + y, nodeWidth, te.h, AppMain.layerTabs);
                } else {
                    if (continuingLines.contains(j))
                        igd.blitScaledImage(size, base, size, size, ox + (j * nodeWidth), oy + y, nodeWidth, te.h, AppMain.layerTabs);
                }
            }
            // the actual item icon
            Rect iconDisplay = Art.reconcile(new Rect(ox + (te.indent * nodeWidth), oy + y, nodeWidth, te.h), te.icon);
            if (te.hasChildren && (!te.expanded)) {
                Rect ico = Art.hiddenTreeIcon;
                igd.blitScaledImage(ico.x, ico.y, ico.width, ico.height, iconDisplay.x, iconDisplay.y, iconDisplay.width, iconDisplay.height, AppMain.layerTabs);
            }
            igd.blitScaledImage(te.icon.x, te.icon.y, te.icon.width, te.icon.height, iconDisplay.x, iconDisplay.y, iconDisplay.width, iconDisplay.height, AppMain.layerTabs);
            y += te.h;
        }
        if (dragCursorEnable) {
            int dcs = size * 3;
            igd.blitScaledImage(0, base, size, size, igd.getMouseX() - (dcs / 2), igd.getMouseY() - (dcs / 2), dcs, dcs, AppMain.layerTabs);
        }
    }

    @Override
    public void handleClick(int x, int y, int button) {
        super.handleClick(x, y, button);
        if (button == 1) {
            dragBase = getTarget(x, y);
            dragBaseX = x;
            dragBaseY = y;
            dragCursorEnable = false;
        }
    }

    @Override
    public void handleDrag(int x, int y) {
        super.handleDrag(x, y);
        if (dragBase != -1) {
            int ddx = Math.abs(dragBaseX - x);
            int ddy = Math.abs(dragBaseY - y);
            if (Math.max(ddx, ddy) > (elements[dragBase].h / 2))
                dragCursorEnable = true;
        }
    }

    @Override
    public void handleRelease(int x, int y) {
        super.handleRelease(x, y);
        int targ = getTarget(x, y);
        if (dragBase != -1) {
            if (dragCursorEnable) {
                if (targ != -1)
                    elements[targ].elementDraggedHere.accept(dragBase);
            } else if (targ == dragBase) {
                elements[dragBase].expandToggle.run();
            }
        }
        dragBase = -1;
        dragCursorEnable = false;
    }

    private int getTarget(int x, int y) {
        if (x < 0)
            return -1;
        if (x > getBounds().width)
            return -1;
        int cy = 0;
        for (int i = 0; i < elements.length; i++) {
            if ((y > cy) && (y < cy + elements[i].h))
                return i;
            cy += elements[i].h;
        }
        return -1;
    }

    public static class TreeElement {
        // Calculated on setBounds. Set to 0 if invisible.
        private int h;
        // Calculated on setBounds.
        private boolean hasChildren, visible;

        public final boolean expanded;
        public final int indent;
        public final Rect icon;
        public final UIElement innerElement;
        public final IConsumer<Integer> elementDraggedHere;
        public final Runnable expandToggle;

        public TreeElement(int i, Rect ico, UIElement pineapple, IConsumer<Integer> o, boolean expand, Runnable ext) {
            indent = i;
            icon = ico;
            innerElement = pineapple;
            if (o == null) {
                elementDraggedHere = new IConsumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                    }
                };
            } else {
                elementDraggedHere = o;
            }
            expanded = expand;
            if (ext == null) {
                expandToggle = new Runnable() {
                    @Override
                    public void run() {

                    }
                };
            } else {
                expandToggle = ext;
            }
        }
    }
}
