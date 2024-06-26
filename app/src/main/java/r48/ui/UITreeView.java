/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import gabien.render.IGrDriver;
import gabien.ui.*;
import gabien.ui.elements.UIBorderedElement;
import gabien.uslx.append.*;
import gabien.wsi.IDesktopPeripherals;
import gabien.wsi.IPeripherals;
import gabien.wsi.IPointer;
import r48.App;

import java.util.HashSet;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

/**
 * NOTE: This must be recreated every time it needs to be reloaded, and needs to be contained in a UIScrollViewLayout to work properly.
 * Created on 11/08/17.
 */
public class UITreeView extends App.Pan implements OldMouseEmulator.IOldMouseReceiver {
    private TreeElement[] elements = new TreeElement[0];
    private OldMouseEmulator mouseEmulator = new OldMouseEmulator(this);
    private final int nodeWidth;
    private int dragBaseX = 0, dragBaseY = 0;
    private boolean dragCursorEnable = false;
    private Art.Symbol dragCursorSymbol = null;
    private int dragBase = -1;

    public UITreeView(App app, int nw) {
        super(app);
        nodeWidth = nw;
    }

    public void setElements(TreeElement[] e) {
        elements = e;
        // This has to be done in advance, because if we do it later, the layout changes go weird.
        for (UIElement uie : layoutGetElements())
            layoutRemoveElement(uie);
        int invisibleAboveIndent = 0x7FFFFFFF; // to hide unexpanded nodes
        TreeElement lastElement = null; // to set hasChildren
        for (TreeElement te : e) {
            te.visible = te.indent <= invisibleAboveIndent;
            if (lastElement != null)
                if (te.indent > lastElement.indent)
                    lastElement.hasChildren = true;
            lastElement = te;
            layoutAddElement(te.innerElement);
            // If we're visible, set invisibleAboveIndent.
            // If expanded, then set it to "infinite"
            // Otherwise, set it to current indent, so this code will only run again on a indent <= this one
            if (te.visible)
                invisibleAboveIndent = te.expanded ? 0x7FFFFFFF : te.indent;
        }
        layoutRecalculateMetrics();
    }

    @Override
    protected void layoutRunImpl() {
        Size r = getSize();
        int y = 0;
        for (TreeElement te : elements) {
            if (!te.visible) {
                layoutSetElementVis(te.innerElement, false);
                continue;
            }
            int x = nodeWidth * (te.indent + 1);
            int h = te.innerElement.layoutGetHForW(r.width - x);
            te.innerElement.setForcedBounds(this, new Rect(x, y, r.width - x, h));
            y += te.h = h;
            layoutSetElementVis(te.innerElement, true);
        }
    }

    @Override
    protected @Nullable Size layoutRecalculateMetricsImpl() {
        int y = 0;
        // If not handled carefully, this will almost certainly be a vicious cycle.
        // 'tis the cost of buttons that are allowed to specify new sizes.
        // Could easily override said buttons, but that would lose the point.
        // Keeping this in mind, nodeWidth is now final, and for most symbols direct w/h is used.
        int width = 0;
        for (TreeElement te : elements) {
            if (!te.visible)
                continue;
            Size wantedSize = te.innerElement.getWantedSize();
            width = Math.max(width, wantedSize.width);
            y += wantedSize.height;
        }
        return new Size(width, y);
    }

    @Override
    public int layoutGetHForW(int width) {
        int y = 0;
        for (TreeElement te : elements) {
            if (!te.visible)
                continue;
            int x = nodeWidth * (te.indent + 1);
            y += te.innerElement.layoutGetHForW(width - x);
        }
        return y;
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
    public void renderLayer(IGrDriver igd, UILayer layer) {
        super.renderLayer(igd, layer);
        if (layer != UILayer.Content)
            return;
        boolean blackText = UIBorderedElement.getBlackTextFlagWindowRoot(getTheme());
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
                    app.a.drawSymbol(igd, pico, j * nodeWidth, y, nodeWidth, te.h, true, blackText);
                } else {
                    if (continuingLines.contains(j))
                        app.a.drawSymbol(igd, Art.Symbol.BarV, j * nodeWidth, y, nodeWidth, te.h, true, blackText);
                }
            }
            // the actual item icon
            int nwMargin = (te.h - nodeWidth) / 2;
            if (te.hasChildren && (!te.expanded))
                app.a.drawSymbol(igd, Art.Symbol.Expandable, te.indent * nodeWidth, y + nwMargin, nodeWidth, true, blackText);
            app.a.drawSymbol(igd, te.icon, te.indent * nodeWidth, y + nwMargin, nodeWidth, true, blackText);
            y += te.h;
        }
        if (dragCursorEnable)
            app.a.drawSymbol(igd, dragCursorSymbol, mouseEmulator.mouseX - (nodeWidth / 2), mouseEmulator.mouseY - (nodeWidth / 2), nodeWidth, false, blackText);
    }

    @Override
    public IPointerReceiver handleNewPointer(IPointer state) {
        final IPointerReceiver ipr = super.handleNewPointer(state);
        if (ipr != null)
            return new IPointerReceiver() {
                @Override
                public void handlePointerBegin(IPointer state) {
                    mouseEmulator.handlePointerBegin(state);
                    ipr.handlePointerBegin(state);
                }

                @Override
                public void handlePointerUpdate(IPointer state) {
                    mouseEmulator.handlePointerUpdate(state);
                    ipr.handlePointerEnd(state);
                }

                @Override
                public void handlePointerEnd(IPointer state) {
                    mouseEmulator.handlePointerEnd(state);
                    ipr.handlePointerEnd(state);
                }
            };
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
            if ((y >= cy) && (y < cy + elements[i].h))
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
        public final Consumer<Integer> elementDraggedHere;
        public final Runnable expandToggle;

        public TreeElement(int i, Art.Symbol ico, UIElement pineapple, Consumer<Integer> o, boolean expand, Runnable ext) {
            indent = i;
            icon = ico;
            innerElement = pineapple;
            if (o == null) {
                elementDraggedHere = new Consumer<Integer>() {
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
