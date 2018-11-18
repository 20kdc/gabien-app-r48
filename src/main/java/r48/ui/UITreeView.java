/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.IDesktopPeripherals;
import gabien.IGrDriver;
import gabien.IPeripherals;
import gabien.ui.*;

import java.util.HashSet;

/**
 * NOTE: This must be recreated every time it needs to be reloaded, and needs to be contained in a UIScrollViewLayout to work properly.
 * Created on 11/08/17.
 */
public class UITreeView extends UIElement.UIPanel implements OldMouseEmulator.IOldMouseReceiver {
    private TreeElement[] elements = new TreeElement[0];
    private OldMouseEmulator mouseEmulator = new OldMouseEmulator(this);
    private final int nodeWidth;
    private int dragBaseX = 0, dragBaseY = 0;
    private boolean dragCursorEnable = false;
    private Art.Symbol dragCursorSymbol = null;
    private int dragBase = -1;

    public UITreeView(int nw) {
        nodeWidth = nw;
    }

    public void setElements(TreeElement[] e) {
        elements = e;
        runLayout();
    }

    @Override
    public void runLayout() {
        Size r = getSize();
        for (UIElement uie : layoutGetElements())
            layoutRemoveElement(uie);
        int y = 0;
        // If not handled carefully, this will almost certainly be a vicious cycle.
        // 'tis the cost of buttons that are allowed to specify new sizes.
        // Could easily override said buttons, but that would lose the point.
        // Keeping this in mind, nodeWidth is now final, and for most symbols direct w/h is used.
        TreeElement lastElement = null; // to set hasChildren
        int invisibleAboveIndent = 0x7FFFFFFF; // to hide unexpanded nodes
        int width = 0;
        for (TreeElement te : elements) {
            te.visible = te.indent <= invisibleAboveIndent;
            if (lastElement != null)
                if (te.indent > lastElement.indent)
                    lastElement.hasChildren = true;
            int x = nodeWidth * (te.indent + 1);
            int h = te.innerElement.getWantedSize().height;
            te.innerElement.setForcedBounds(null, new Rect(x, y, r.width - x, h));
            h = te.innerElement.getWantedSize().height;
            te.innerElement.setForcedBounds(null, new Rect(x, y, r.width - x, h));
            width = Math.max(width, te.innerElement.getWantedSize().width);
            if (!te.visible)
                h = 0;
            y += h;
            te.h = h;
            if (te.visible)
                layoutAddElement(te.innerElement);
            lastElement = te;
            // If we're visible, set invisibleAboveIndent.
            // If expanded, then set it to "infinite"
            // Otherwise, set it to current indent, so this code will only run again on a indent <= this one
            if (te.visible)
                invisibleAboveIndent = te.expanded ? 0x7FFFFFFF : te.indent;
        }
        setWantedSize(new Size(width, y));
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        super.update(deltaTime, selected, peripherals);
        if (peripherals instanceof IDesktopPeripherals) {
            mouseEmulator.mouseX = ((IDesktopPeripherals) peripherals).getMouseX();
            mouseEmulator.mouseY = ((IDesktopPeripherals) peripherals).getMouseY();
        }
    }

    @Override
    public void render(IGrDriver igd) {
        super.render(igd);
        boolean blackText = UIBorderedElement.getBlackTextFlagWindowRoot();
        int y = 0;
        HashSet<Integer> continuingLines = new HashSet<Integer>();
        for (int i = 0; i < elements.length; i++) {
            TreeElement te = elements[i];
            if (!te.visible)
                continue;

            Art.Symbol pico = Art.Symbol.BarVBranchR;
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
                pico = Art.Symbol.BarCornerUR;
                continuingLines.remove(te.indent - 1);
            } else {
                continuingLines.add(te.indent - 1);
            }

            for (int j = 0; j < te.indent; j++) {
                if (j == (te.indent - 1)) {
                    Art.drawSymbol(igd, pico, j * nodeWidth, y, nodeWidth, te.h, true, blackText);
                } else {
                    if (continuingLines.contains(j))
                        Art.drawSymbol(igd, Art.Symbol.BarV, j * nodeWidth, y, nodeWidth, te.h, true, blackText);
                }
            }
            // the actual item icon
            int nwMargin = (te.h - nodeWidth) / 2;
            if (te.hasChildren && (!te.expanded))
                Art.drawSymbol(igd, Art.Symbol.Expandable, te.indent * nodeWidth, y + nwMargin, nodeWidth, true, blackText);
            Art.drawSymbol(igd, te.icon, te.indent * nodeWidth, y + nwMargin, nodeWidth, true, blackText);
            y += te.h;
        }
        if (dragCursorEnable)
            Art.drawSymbol(igd, dragCursorSymbol, mouseEmulator.mouseX - (nodeWidth / 2), mouseEmulator.mouseY - (nodeWidth / 2), nodeWidth, false, blackText);
    }

    @Override
    public IPointerReceiver handleNewPointer(IPointer state) {
        return mouseEmulator;
    }

    @Override
    public void handleClick(int x, int y, int button) {
        if (button == 1) {
            dragBase = getTarget(x, y);
            dragBaseX = x;
            dragBaseY = y;
            dragCursorEnable = false;
        }
    }

    @Override
    public void handleDrag(int x, int y) {
        if (dragBase != -1) {
            int ddx = Math.abs(dragBaseX - x);
            int ddy = Math.abs(dragBaseY - y);
            if (Math.max(ddx, ddy) > (elements[dragBase].h / 2)) {
                dragCursorEnable = true;
                dragCursorSymbol = elements[dragBase].icon;
            }
        }
    }

    @Override
    public void handleRelease(int x, int y) {
        int targ = getTarget(x, y);
        if (dragBase != -1) {
            if (dragCursorEnable) {
                if (targ != -1)
                    elements[targ].elementDraggedHere.accept(dragBase);
            } else if (targ == dragBase) {
                if (x < ((elements[dragBase].indent + 1) * elements[dragBase].h))
                    elements[dragBase].expandToggle.run();
            }
        }
        dragBase = -1;
        dragCursorEnable = false;
    }

    private int getTarget(int x, int y) {
        if (x < 0)
            return -1;
        if (x > getSize().width)
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
        public final Art.Symbol icon;
        public final UIElement innerElement;
        public final IConsumer<Integer> elementDraggedHere;
        public final Runnable expandToggle;

        public TreeElement(int i, Art.Symbol ico, UIElement pineapple, IConsumer<Integer> o, boolean expand, Runnable ext) {
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
