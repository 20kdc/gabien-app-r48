/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UIPanel;
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
    public String rP, gP, bP, sP;
    public int base;

    public TonePickerSchemaElement(String rPath, String gPath, String bPath, String sPath, int b) {
        rP = rPath;
        gP = gPath;
        bP = bPath;
        sP = sPath;
        base = b;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        int nr = (int) PathSyntax.parse(target, rP).fixnumVal;
        int ng = (int) PathSyntax.parse(target, gP).fixnumVal;
        int nb = (int) PathSyntax.parse(target, bP).fixnumVal;
        int ns = (int) PathSyntax.parse(target, sP).fixnumVal;
        return createTotem(new ToneImageEffect(nr, ng, nb, ns, base));
    }

    public static UIPanel createTotem(IImageEffect cfg) {
        UIPanel panel = new UIPanel();
        // The tone picker text height is typically 6, which should equal 64, as a base.
        // How do I make this work? Like this:
        int imageUnit = (FontSizes.tonePickerTextHeight * 64) / 6;
        IGrDriver finalComposite = GaBIEn.makeOffscreenBuffer(imageUnit * 2, imageUnit, false);
        IImage totem = GaBIEn.getImage("tonetotm.png");
        finalComposite.blitScaledImage(0, 0, 256, 256, 0, 0, imageUnit, imageUnit, totem);
        finalComposite.blitImage(0, 0, imageUnit, imageUnit, imageUnit, 0, AppMain.imageFXCache.process(finalComposite, cfg));
        UILabel.drawString(finalComposite, 0, (imageUnit + 1) - FontSizes.tonePickerTextHeight, TXDB.get("TotemSrc."), false, FontSizes.tonePickerTextHeight);
        UILabel.drawString(finalComposite, imageUnit, (imageUnit + 1) - FontSizes.tonePickerTextHeight, TXDB.get("Composite"), false, FontSizes.tonePickerTextHeight);
        panel.baseImage = GaBIEn.createImage(finalComposite.getPixels(), imageUnit * 2, imageUnit);
        panel.setBounds(new Rect(0, 0, imageUnit * 2, imageUnit));
        finalComposite.shutdown();
        return panel;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {

    }
}
