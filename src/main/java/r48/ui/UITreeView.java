/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.ui;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.AppMain;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * NOTE: This must be recreated every time it needs to be reloaded, and needs to be contained in a UIScrollViewLayout to work properly.
 * Created on 11/08/17.
 */
public class UITreeView extends UIPanel {
    private final TreeElement[] elements;
    private int nodeWidth = 8;
    public UITreeView(TreeElement[] e) {
        elements = e;
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        allElements.clear();
        int y = 0;
        nodeWidth = 0;
        int total = 0;
        for (TreeElement te : elements) {
            // Bad IDE warning! This makes total sense - it's averaging the node heights to get the aspect ratio around about right.
            nodeWidth += te.innerElement.getBounds().height;
            total++;
        }
        nodeWidth /= total;
        for (TreeElement te : elements) {
            int x = nodeWidth * (te.indent + 1);
            int h = te.innerElement.getBounds().height;
            te.innerElement.setBounds(new Rect(x, y, r.width - x, h));
            y += h;
            te.h = h;
            allElements.add(te.innerElement);
        }
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean select, IGrInDriver igd) {
        super.updateAndRender(ox, oy, deltaTime, select, igd);
        int y = 0;
        HashSet<Integer> continuingLines = new HashSet<Integer>();
        for (int i = 0; i < elements.length; i++) {
            TreeElement te = elements[i];

            int ico = 0;
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
                    igd.blitScaledImage(pico * 4, 32, 4, 4, ox + (j * nodeWidth), oy + y, nodeWidth, te.h, AppMain.layerTabs);
                } else {
                    if (continuingLines.contains(j))
                        igd.blitScaledImage(4, 32, 4, 4, ox + (j * nodeWidth), oy + y, nodeWidth, te.h, AppMain.layerTabs);
                }
            }
            igd.blitScaledImage(ico * 4, 32, 4, 4, ox + (te.indent * nodeWidth), oy + y, nodeWidth, te.h, AppMain.layerTabs);
            y += te.h;
        }
    }

    public static class TreeElement {
        public final int indent;
        private int h;
        public final UIElement innerElement;
        public final IConsumer<Integer> elementDraggedHere;

        public TreeElement(int i, UIElement pineapple, IConsumer<Integer> o) {
            indent = i;
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
        }
    }
}
