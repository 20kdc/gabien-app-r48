/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.IImage;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.io.PathUtils;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIThumbnail;
import r48.ui.spacing.UIBorderedSubpanel;

import java.util.HashSet;

/**
 * Gives a list of items (removing the final extension).
 * Meant to be used in conjunction with an existing instance variable, example:
 * baka { string subwindow[ Browse... ] fileSelector CharSet/ }
 * Created on 15/06/17.
 */
public class FileSelectorSchemaElement extends SchemaElement {
    public final String pathExtender;
    public final String mustBeImage;

    public FileSelectorSchemaElement(String ext, String img) {
        pathExtender = ext;
        mustBeImage = img;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        AppMain.performFullImageFlush();
        final UIScrollLayout uiSVL = AggregateSchemaElement.createScrollSavingSVL(launcher, this, target);
        String[] strs = GaBIEn.listEntries(PathUtils.autoDetectWindows(AppMain.rootPath + pathExtender));
        if (strs == null)
            return new UILabel("The folder does not exist or was not accessible.", FontSizes.schemaFieldTextHeight);
        HashSet<String> hitStrs = new HashSet<String>();
        UIElement waitingLeft = null;
        for (String s : strs) {
            final String sStripped = stripExt(s);
            if (hitStrs.contains(sStripped))
                continue;
            UITextButton selectButton = new UITextButton(sStripped, FontSizes.schemaFieldTextHeight, new Runnable() {
                @Override
                public void run() {
                    target.encString(sStripped, false);
                    path.changeOccurred(false);
                    launcher.popObject();
                }
            });
            UIElement res = null;
            if (mustBeImage != null) {
                IImage im = AppMain.stuffRendererIndependent.imageLoader.getImage(mustBeImage + sStripped, false);
                if (im == GaBIEn.getErrorImage())
                    continue;
                hitStrs.add(sStripped);
                int bw = UIBorderedElement.getRecommendedBorderWidth(FontSizes.schemaFieldTextHeight) * 4;
                res = new UIBorderedSubpanel(new UISplitterLayout(new UIThumbnail(im), selectButton, true, 1), bw);
            } else {
                hitStrs.add(sStripped);
                res = selectButton;
            }
            if (waitingLeft != null) {
                uiSVL.panelsAdd(new UISplitterLayout(waitingLeft, res, false, 0.5d));
                waitingLeft = null;
            } else {
                waitingLeft = res;
            }
        }
        if (waitingLeft != null)
            uiSVL.panelsAdd(new UISplitterLayout(waitingLeft, new UIPublicPanel(1, 1), false, 0.5d));
        AppMain.performFullImageFlush();
        return uiSVL;
    }

    private String stripExt(String s) {
        int idx = s.lastIndexOf('.');
        if (idx != -1)
            return s.substring(0, idx);
        return s;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        // Does nothing - this is solely an editor helper element.
    }
}
