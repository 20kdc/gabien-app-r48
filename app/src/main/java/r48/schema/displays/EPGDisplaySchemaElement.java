/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.displays;

import gabien.render.IGrDriver;
import gabien.ui.UIElement;
import gabien.ui.UILayer;
import gabien.uslx.append.Size;
import gabien.wsi.IPeripherals;
import r48.App;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.map.StuffRenderer;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Solely for the express purpose of Event::Page::Graphic schemas
 * Created on 12/29/16.
 */
public class EPGDisplaySchemaElement extends SchemaElement.Leaf {
    public EPGDisplaySchemaElement(App app) {
        super(app);
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, SchemaPath path) {
        StuffRenderer r = launcher.getContextRenderer();
        return buildEditorFromObject(app, r, target);
    }

    /**
     * Used for simulated objects
     */
    public static UIElement buildEditorFromObject(App app, StuffRenderer r, RORIO target) {
        final int sprScale = app.f.getSpriteScale();
        UIElement display = new UIElement(64, 96 * sprScale) {
            @Override
            public void update(double deltaTime, boolean selected, IPeripherals peripherals) {

            }

            @Override
            public void renderLayer(IGrDriver igd, UILayer layer) {
                if (layer != UILayer.Content)
                    return;
                Size bounds = getSize();
                igd.clearRect(255, 0, 255, 0, 0, bounds.width, bounds.height);
                int ofs = r.tileRenderer.tileSize / 2;
                r.eventRenderer.drawEventGraphic(target, (bounds.width / 2) - (ofs * sprScale), (bounds.height / 2) - (ofs * sprScale), igd, sprScale);
            }
        };
        return display;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath index, boolean setDefault) {

    }
}
