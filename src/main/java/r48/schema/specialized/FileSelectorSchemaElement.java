/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.IImage;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UIScrollLayout;
import gabien.ui.UITextButton;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.io.PathUtils;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

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
        // TODO: UPDATE TO IMAGEEDITOR-FULL FLUSH
        AppMain.stuffRendererIndependent.imageLoader.flushCache();
        final UIScrollLayout uiSVL = AggregateSchemaElement.createScrollSavingSVL(path, launcher, this, target);
        String[] strs = GaBIEn.listEntries(PathUtils.autoDetectWindows(AppMain.rootPath + pathExtender));
        if (strs == null)
            return new UILabel("The folder does not exist or was not accessible.", FontSizes.schemaButtonTextHeight);
        HashSet<String> hitStrs = new HashSet<String>();
        for (String s : strs) {
            final String sStripped = stripExt(s);
            if (mustBeImage != null) {
                IImage im = AppMain.stuffRendererIndependent.imageLoader.getImage(mustBeImage + sStripped, false);
                if (im == GaBIEn.getErrorImage())
                    continue;
            }
            if (hitStrs.contains(sStripped))
                continue;
            hitStrs.add(sStripped);
            uiSVL.panelsAdd(new UITextButton(sStripped, FontSizes.schemaButtonTextHeight, new Runnable() {
                @Override
                public void run() {
                    target.encString(sStripped, false);
                    path.changeOccurred(false);
                    launcher.switchObject(path.findBack());
                }
            }));
        }
        AppMain.stuffRendererIndependent.imageLoader.flushCache();
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
