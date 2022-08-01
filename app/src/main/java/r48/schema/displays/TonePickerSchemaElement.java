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
import r48.dbs.PathSyntax;
import r48.dbs.TXDB;
import r48.imagefx.IImageEffect;
import r48.imagefx.ToneImageEffect;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIThumbnail;

/**
 * Shows test images for tone selection.
 * Note this must *follow* elements to ensure the properties exist.
 * Created on 31/07/17.
 */
public class TonePickerSchemaElement extends SchemaElement {
    public final String rP, gP, bP, sP;
    public final int base;

    public TonePickerSchemaElement(String rPath, String gPath, String bPath, String sPath, int b) {
        rP = rPath;
        gP = gPath;
        bP = bPath;
        sP = sPath;
        base = b;
    }

    @Override
    public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
        int nr = (int) PathSyntax.parse(target, rP).getFX();
        int ng = (int) PathSyntax.parse(target, gP).getFX();
        int nb = (int) PathSyntax.parse(target, bP).getFX();
        int ns = (int) PathSyntax.parse(target, sP).getFX();
        return createTotem(target, new ToneImageEffect(nr, ng, nb, ns, base));
    }

    public static IImage compositeTotem(IImage totem, IImageEffect cfg) {
        // The tone picker text height is typically 6, which should equal 64, as a base.
        // How do I make this work? Like this:

        int imageUnit = (FontSizes.tonePickerTextHeight * 64) / 6;
        if (imageUnit < 1)
            imageUnit = 1;
        IGrDriver finalComposite = GaBIEn.makeOffscreenBuffer(imageUnit * 2, imageUnit, false);

        finalComposite.blitScaledImage(0, 0, 256, 256, 0, 0, imageUnit, imageUnit, totem);
        finalComposite.blitImage(0, 0, imageUnit, imageUnit, imageUnit, 0, AppMain.imageFXCache.process(finalComposite, cfg));

        FontManager.drawString(finalComposite, 0, (imageUnit + 1) - FontSizes.tonePickerTextHeight, TXDB.get("TotemSrc."), false, false, FontSizes.tonePickerTextHeight);
        FontManager.drawString(finalComposite, imageUnit, (imageUnit + 1) - FontSizes.tonePickerTextHeight, TXDB.get("Composite"), false, false, FontSizes.tonePickerTextHeight);

        IImage im = GaBIEn.createImage(finalComposite.getPixels(), imageUnit * 2, imageUnit);
        finalComposite.shutdown();
        return im;
    }

    public static IImage getOneTrueTotem() {
        return GaBIEn.getImage("tonetotm.png");
    }

    public static UIPublicPanel createTotemStandard(IImage totem, IImageEffect cfg) {
        IImage img = compositeTotem(totem, cfg);
        UIPublicPanel panel = new UIPublicPanel(img.getWidth(), img.getHeight());
        panel.baseImage = img;
        return panel;
    }

    public UIElement createTotem(IRIO target, IImageEffect cfg) {
        return createTotemStandard(getOneTrueTotem(), cfg);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {

    }
    
    public static class Thumbnail extends TonePickerSchemaElement {
        public final String iPath, iPrefix;
        public Thumbnail(String rPath, String gPath, String bPath, String sPath, int b, String iPth, String iPfx) {
            super(rPath, gPath, bPath, sPath, b);
            iPath = iPth;
            iPrefix = iPfx;
        }

        @Override
        public UIElement createTotem(IRIO target, IImageEffect cfg) {
            String imagePath = iPrefix + PathSyntax.parse(target, iPath).decString();
            IImage totem = AppMain.stuffRendererIndependent.imageLoader.getImage(imagePath, false);
            IImage img = compositeTotem(totem, cfg);
            UIThumbnail panel = new UIThumbnail(img);
            return panel;
        }
    }
}
