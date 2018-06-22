/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.displays;

import gabien.FontManager;
import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import gabien.ui.UIElement;
import gabien.ui.UIPublicPanel;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.PathSyntax;
import r48.dbs.TXDB;
import r48.imagefx.IImageEffect;
import r48.imagefx.ToneImageEffect;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Shows test images for tone selection.
 * Note this must *follow* elements to ensure the properties exist.
 * Created on 31/07/17.
 */
public class TonePickerSchemaElement extends SchemaElement {
    public final String rP, gP, bP, sP;
    public final int base;
    public final boolean sdb2;

    public TonePickerSchemaElement(String rPath, String gPath, String bPath, String sPath, int b, boolean sdb2x) {
        rP = rPath;
        gP = gPath;
        bP = bPath;
        sP = sPath;
        base = b;
        sdb2 = sdb2x;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        int nr = (int) PathSyntax.parse(target, rP, sdb2).fixnumVal;
        int ng = (int) PathSyntax.parse(target, gP, sdb2).fixnumVal;
        int nb = (int) PathSyntax.parse(target, bP, sdb2).fixnumVal;
        int ns = (int) PathSyntax.parse(target, sP, sdb2).fixnumVal;
        return createTotem(new ToneImageEffect(nr, ng, nb, ns, base));
    }

    public static IImage compositeTotem(IImageEffect cfg) {
        // The tone picker text height is typically 6, which should equal 64, as a base.
        // How do I make this work? Like this:

        int imageUnit = (FontSizes.tonePickerTextHeight * 64) / 6;
        if (imageUnit < 1)
            imageUnit = 1;
        IGrDriver finalComposite = GaBIEn.makeOffscreenBuffer(imageUnit * 2, imageUnit, false);

        IImage totem = GaBIEn.getImage("tonetotm.png");
        finalComposite.blitScaledImage(0, 0, 256, 256, 0, 0, imageUnit, imageUnit, totem);
        finalComposite.blitImage(0, 0, imageUnit, imageUnit, imageUnit, 0, AppMain.imageFXCache.process(finalComposite, cfg));

        FontManager.drawString(finalComposite, 0, (imageUnit + 1) - FontSizes.tonePickerTextHeight, TXDB.get("TotemSrc."), false, false, FontSizes.tonePickerTextHeight);
        FontManager.drawString(finalComposite, imageUnit, (imageUnit + 1) - FontSizes.tonePickerTextHeight, TXDB.get("Composite"), false, false, FontSizes.tonePickerTextHeight);

        IImage im = GaBIEn.createImage(finalComposite.getPixels(), imageUnit * 2, imageUnit);
        finalComposite.shutdown();
        return im;
    }

    public static UIPublicPanel createTotem(IImageEffect cfg) {
        int imageUnit = (FontSizes.tonePickerTextHeight * 64) / 6;
        UIPublicPanel panel = new UIPublicPanel(imageUnit * 2, imageUnit);
        panel.baseImage = compositeTotem(cfg);
        return panel;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {

    }
}
