/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.toolsets.utils;

import r48.App;
import r48.schema.displays.TonePickerSchemaElement;
import gabien.GaBIEn;
import gabien.natives.BadGPU;
import gabien.render.IGrDriver;
import gabien.render.IImage;
import gabien.ui.UIElement;
import gabien.wsi.IPeripherals;

/**
 * Created 17th February 2023
 */
public class UITestGraphicsStuff extends UIElement {
    public final App app;
    private static final int[] blendModes = {
        IGrDriver.BLEND_NORMAL,
        IGrDriver.BLEND_ADD,
        IGrDriver.BLEND_SUB
    };
    public UITestGraphicsStuff(App app) {
        this.app = app;
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
    }

    @Override
    public void render(IGrDriver igd) {
        IImage tst = TonePickerSchemaElement.getOneTrueTotem();
        for (int j = 0; j < 3; j++) {
            int angle = j * 22;
            for (int i = 0; i < blendModes.length; i++)
                igd.drawRotatedScaled(j * 128, i * 64, tst.getWidth(), tst.getHeight(), angle, tst, blendModes[i], 0);
        }
        float ofx = (float) (96 + (GaBIEn.getTime() % 16));
        igd.blitScaledImage(ofx, ofx, 16, 16, 512, 0, 128, 128, tst, IGrDriver.BLEND_NORMAL, BadGPU.DrawFlags.MagLinear);
        igd.drawXYSTRGBA(IGrDriver.BLEND_NORMAL, 0, null,
                  0, 512, 0, 0, 1, 0, 0, 1,
                256, 512, 0, 0, 0, 1, 0, 1,
                  0, 768, 0, 0, 0, 0, 1, 1
        );
    }
}
