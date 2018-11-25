/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.displays;

import gabien.IGrDriver;
import gabien.IPeripherals;
import gabien.ui.Size;
import gabien.ui.UIElement;
import r48.FontSizes;
import r48.io.data.IRIO;
import r48.map.StuffRenderer;
import r48.schema.IRIOAwareSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Solely for the express purpose of Event::Page::Graphic schemas
 * Created on 12/29/16.
 */
public class EPGDisplaySchemaElement extends IRIOAwareSchemaElement {
    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, SchemaPath path) {
        final int sprScale = FontSizes.getSpriteScale();
        UIElement display = new UIElement(64, 96 * sprScale) {
            @Override
            public void update(double deltaTime, boolean selected, IPeripherals peripherals) {

            }

            @Override
            public void render(IGrDriver igd) {
                Size bounds = getSize();
                igd.clearRect(255, 0, 255, 0, 0, bounds.width, bounds.height);
                StuffRenderer r = launcher.getContextRenderer();
                int ofs = r.tileRenderer.getTileSize() / 2;
                r.eventRenderer.drawEventGraphic(target, (bounds.width / 2) - (ofs * sprScale), (bounds.height / 2) - (ofs * sprScale), igd, sprScale);
            }
        };
        return display;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath index, boolean setDefault) {

    }
}
