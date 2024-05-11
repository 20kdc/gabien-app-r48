/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.render.IImage;
import gabien.ui.*;
import gabien.ui.elements.UIBorderedElement;
import gabien.ui.elements.UIEmpty;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextButton;
import gabien.ui.elements.UIThumbnail;
import gabien.ui.layouts.UISplitterLayout;
import r48.App;
import r48.io.data.IRIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.EmbedDataKey;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.spacing.UIBorderedSubpanel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Gives a list of items (removing the final extension).
 * Meant to be used in conjunction with an existing instance variable, example:
 * baka { string subwindow[ Browse... ] fileSelector CharSet/ }
 * Created on 15/06/17.
 */
public class FileSelectorSchemaElement extends SchemaElement.Leaf {
    public final String pathExtender;
    public final String mustBeImage;
    public final EmbedDataKey<Double> scrollPointKey = new EmbedDataKey<>();

    public FileSelectorSchemaElement(App app, String ext, String img) {
        super(app);
        pathExtender = ext;
        mustBeImage = img;
    }

    @Override
    public UIElement buildHoldingEditorImpl(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        app.ui.performFullImageFlush();
        String[] strs = GaBIEn.listEntries(app.gameResources.intoPath(pathExtender));
        if (strs == null)
            return new UILabel("The folder does not exist or was not accessible.", app.f.schemaFieldTH);
        Arrays.sort(strs, (a, b) -> a.compareToIgnoreCase(b));
        HashSet<String> hitStrs = new HashSet<String>();
        UIElement waitingLeft = null;
        LinkedList<UIElement> uiSVL = new LinkedList<>();
        for (String s : strs) {
            final String sStripped = stripExt(s);
            if (hitStrs.contains(sStripped))
                continue;
            UITextButton selectButton = new UITextButton(sStripped, app.f.schemaFieldTH, new Runnable() {
                @Override
                public void run() {
                    target.setString(sStripped);
                    path.changeOccurred(false);
                    launcher.popObject();
                }
            });
            UIElement res = null;
            if (mustBeImage != null) {
                IImage im = launcher.getApp().stuffRendererIndependent.imageLoader.getImage(mustBeImage + sStripped, false);
                if (im == GaBIEn.getErrorImage())
                    continue;
                hitStrs.add(sStripped);
                int bw = UIBorderedElement.getRecommendedBorderWidth(app.f.schemaFieldTH) * 4;
                res = new UIBorderedSubpanel(new UISplitterLayout(new UIThumbnail(im), selectButton, true, 1), bw);
            } else {
                hitStrs.add(sStripped);
                res = selectButton;
            }
            if (waitingLeft != null) {
                uiSVL.add(new UISplitterLayout(waitingLeft, res, false, 0.5d));
                waitingLeft = null;
            } else {
                waitingLeft = res;
            }
        }
        if (waitingLeft != null)
            uiSVL.add(new UISplitterLayout(waitingLeft, new UIEmpty(), false, 0.5d));
        app.ui.performFullImageFlush();
        return AggregateSchemaElement.createScrollSavingSVL(launcher, scrollPointKey, target, uiSVL);
    }

    private String stripExt(String s) {
        int idx = s.lastIndexOf('.');
        if (idx != -1)
            return s.substring(0, idx);
        return s;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        // Does nothing - this is solely an editor helper element.
    }
}
