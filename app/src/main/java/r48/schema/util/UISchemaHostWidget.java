/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.schema.util;

import gabien.ui.UIElement;
import gabien.uslx.append.Rect;
import gabien.uslx.append.Size;
import r48.App;
import r48.map.UIMapView;

/**
 * Created 21st August, 2024
 */
public class UISchemaHostWidget extends SchemaHostBase {
    private UIElement innerElemEditor;

    public UISchemaHostWidget(App app, UIMapView rendererSource) {
        super(app, rendererSource);
    }

    @Override
    public void pushObject(SchemaPath nextObject) {
        if (innerElem == null) {
            switchObject(nextObject);
        } else {
            newBlank().pushObject(nextObject);
        }
    }

    @Override
    public void popObject(boolean canClose) {
        // do absolutely nothing
    }

    @Override
    protected void refreshDisplay() {
        // just swap this hastily implemented dynamic proxy
        for (UIElement uie : layoutGetElements())
            layoutRemoveElement(uie);
        innerElemEditor = innerElem.editor.buildHoldingEditor(innerElem.targetElement, this, innerElem);
        layoutAddElement(innerElemEditor);
        layoutRecalculateMetrics();
    }

    @Override
    protected Size layoutRecalculateMetricsImpl() {
        if (innerElemEditor == null)
            return null;
        return innerElemEditor.getWantedSize();
    }

    @Override
    public int layoutGetHForW(int width) {
        if (innerElemEditor == null)
            return 0;
        return innerElemEditor.layoutGetHForW(width);
    }

    @Override
    public int layoutGetWForH(int height) {
        if (innerElemEditor == null)
            return 0;
        return innerElemEditor.layoutGetWForH(height);
    }

    @Override
    protected void layoutRunImpl() {
        if (innerElemEditor != null)
            innerElemEditor.setForcedBounds(this, new Rect(getSize()));
    }

}
