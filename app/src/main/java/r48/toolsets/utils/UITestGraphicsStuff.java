/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.toolsets.utils;

import gabien.IPeripherals;
import r48.map.events.RMEventGraphicRenderer;
import r48.schema.displays.TonePickerSchemaElement;
import gabien.IGrDriver;
import gabien.IImage;
import gabien.ui.UIElement;

/**
 * Created 17th February 2023
 */
public class UITestGraphicsStuff extends UIElement {
    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
    }

    @Override
    public void render(IGrDriver igd) {
        for (int j = 0; j < 3; j++) {
            int angle = j * 22;
            IImage tst = TonePickerSchemaElement.getOneTrueTotem();
            for (int i = 0; i < 3; i++)
                RMEventGraphicRenderer.flexibleSpriteDraw(0, 0, tst.getWidth(), tst.getHeight(), j * 128, i * 64, tst.getWidth(), tst.getHeight(), angle, tst, i, igd);
        }
    }
}
