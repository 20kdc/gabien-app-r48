/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized;

import gabien.ui.*;
import gabienapp.Application;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.io.File;

/**
 * Gives a list of items (removing the final extension).
 * Meant to be used in conjunction with an existing instance variable, example:
 * baka { string subwindow[ Browse... ] fileSelector CharSet/ }
 * Created on 15/06/17.
 */
public class FileSelectorSchemaElement extends SchemaElement {
    public final String pathExtender;

    public FileSelectorSchemaElement(String ext) {
        pathExtender = ext;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        UIScrollLayout uiSVL = new UIScrollLayout(true);
        String[] strs = new File(Application.autoDetectWindows(AppMain.rootPath + pathExtender)).list();
        if (strs == null)
            return new UILabel("The folder does not exist or was not accessible.", FontSizes.schemaButtonTextHeight);
        for (String s : strs) {
            final String sStripped = stripExt(s);
            uiSVL.panels.add(new UITextButton(FontSizes.schemaButtonTextHeight, sStripped, new Runnable() {
                @Override
                public void run() {
                    target.encString(sStripped);
                    path.changeOccurred(false);
                    launcher.switchObject(path.findBack());
                }
            }));
        }
        uiSVL.setBounds(new Rect(0, 0, 128, 128));
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
