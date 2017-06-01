/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.displays;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import r48.RubyIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Solely for the express purpose of Event::Page::Graphic schemas
 * Created on 12/29/16.
 */
public class EPGDisplaySchemaElement extends SchemaElement {
    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, SchemaPath path) {
        UIElement display = new UIElement() {
            @Override
            public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
                Rect bounds = getBounds();
                igd.clearRect(255, 0, 255, ox, oy, bounds.width, bounds.height);
                launcher.getContextRenderer().eventRenderer.drawEventGraphic(target, ox + (bounds.width / 2) - 16, oy + (bounds.height / 2) - 16, igd);
            }

            @Override
            public void handleClick(int x, int y, int button) {

            }
        };
        display.setBounds(new Rect(0, 0, 64, 96));
        return display;
    }

    @Override
    public int maxHoldingHeight() {
        return 96;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath index, boolean setDefault) {

    }
}
