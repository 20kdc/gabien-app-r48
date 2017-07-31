/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.IOsbDriver;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import r48.AppMain;
import r48.RubyIO;
import r48.dbs.PathSyntax;
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
    public TonePickerSchemaElement(String rPath, String gPath, String bPath, String sPath) {
        rP = rPath;
        gP = gPath;
        bP = bPath;
        sP = sPath;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        int nr = (int) PathSyntax.parse(target, rP).fixnumVal;
        int ng = (int) PathSyntax.parse(target, gP).fixnumVal;
        int nb = (int) PathSyntax.parse(target, bP).fixnumVal;
        int ns = (int) PathSyntax.parse(target, sP).fixnumVal;
        UIPanel panel = new UIPanel();
        IOsbDriver finalComposite = GaBIEn.makeOffscreenBuffer(128, 64, false);
        IGrInDriver.IImage totem = GaBIEn.getImage("tonetotm.png");
        finalComposite.blitScaledImage(0, 0, 256, 256, 0, 0, 64, 64, totem);
        finalComposite.blitImage(0, 0, 64, 64, 64, 0, AppMain.imageFXCache.process(finalComposite, new ToneImageEffect(nr, ng, nb, ns)));
        panel.baseImage = GaBIEn.createImage(finalComposite.getPixels(), 128, 64);
        panel.setBounds(new Rect(0, 0, 128, 64));
        finalComposite.shutdown();
        return panel;
    }

    @Override
    public int maxHoldingHeight() {
        return 64;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {

    }
}
