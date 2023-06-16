/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.displays;

import gabien.FontManager;
import gabien.GaBIEn;
import gabien.render.IGrDriver;
import gabien.render.IImage;
import gabien.ui.UIElement;
import gabien.ui.UIPublicPanel;
import r48.App;
import r48.dbs.PathSyntax;
import r48.imagefx.IImageEffect;
import r48.imagefx.ToneImageEffect;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.pages.TrRoot;
import r48.ui.UIThumbnail;

/**
 * Shows test images for tone selection.
 * Note this must *follow* elements to ensure the properties exist.
 * Created on 31/07/17.
 */
public class TonePickerSchemaElement extends SchemaElement {
    public final PathSyntax rP, gP, bP, sP;
    public final int base;

    public TonePickerSchemaElement(App app, PathSyntax rPath, PathSyntax gPath, PathSyntax bPath, PathSyntax sPath, int b) {
        super(app);
        rP = rPath;
        gP = gPath;
        bP = bPath;
        sP = sPath;
        base = b;
    }

    @Override
    public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
        int nr = (int) rP.get(target).getFX();
        int ng = (int) gP.get(target).getFX();
        int nb = (int) bP.get(target).getFX();
        int ns = (int) sP.get(target).getFX();
        return createTotem(target, new ToneImageEffect(nr, ng, nb, ns, base));
    }

    public static IImage compositeTotem(App app, IImage totem, IImageEffect cfg) {
        // The tone picker text height is typically 6, which should equal 64, as a base.
        // How do I make this work? Like this:

        int imageUnit = (app.f.tonePickerTH * 64) / 6;
        if (imageUnit < 1)
            imageUnit = 1;
        IGrDriver finalComposite = GaBIEn.makeOffscreenBuffer(imageUnit * 2, imageUnit);

        finalComposite.blitScaledImage(0, 0, 256, 256, 0, 0, imageUnit, imageUnit, totem);
        finalComposite.blitImage(0, 0, imageUnit, imageUnit, imageUnit, 0, app.ui.imageFXCache.process(finalComposite, cfg));

        final TrRoot T = app.t;
        FontManager.drawString(finalComposite, 0, (imageUnit + 1) - app.f.tonePickerTH, T.z.l102, false, false, app.f.tonePickerTH);
        FontManager.drawString(finalComposite, imageUnit, (imageUnit + 1) - app.f.tonePickerTH, T.z.l103, false, false, app.f.tonePickerTH);

        IImage im = GaBIEn.createImage(finalComposite.getPixels(), imageUnit * 2, imageUnit);
        finalComposite.shutdown();
        return im;
    }

    public static IImage getOneTrueTotem() {
        return GaBIEn.getImage("tonetotm.png");
    }

    public static UIPublicPanel createTotemStandard(App app, IImage totem, IImageEffect cfg) {
        IImage img = compositeTotem(app, totem, cfg);
        UIPublicPanel panel = new UIPublicPanel(img.getWidth(), img.getHeight());
        panel.baseImage = img;
        return panel;
    }

    public UIElement createTotem(IRIO target, IImageEffect cfg) {
        return createTotemStandard(app, getOneTrueTotem(), cfg);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {

    }
    
    public static class Thumbnail extends TonePickerSchemaElement {
        public final PathSyntax iPath;
        public final String iPrefix;
        public Thumbnail(App app, PathSyntax rPath, PathSyntax gPath, PathSyntax bPath, PathSyntax sPath, int b, PathSyntax iPth, String iPfx) {
            super(app, rPath, gPath, bPath, sPath, b);
            iPath = iPth;
            iPrefix = iPfx;
        }

        @Override
        public UIElement createTotem(IRIO target, IImageEffect cfg) {
            String imagePath = iPrefix + iPath.get(target).decString();
            IImage totem = app.stuffRendererIndependent.imageLoader.getImage(imagePath, false);
            IImage img = compositeTotem(app, totem, cfg);
            UIThumbnail panel = new UIThumbnail(img);
            return panel;
        }
    }
}
