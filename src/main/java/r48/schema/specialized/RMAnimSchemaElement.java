/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.schema.SchemaElement;
import r48.schema.specialized.rmanim.RMAnimRootPanel;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * This starts a UI-framework-based special editing package,
 * which calls back to this for edit notifications.
 * Created on 2/17/17.
 */
public class RMAnimSchemaElement extends SchemaElement {
    private String a, b;
    private int framerate;

    public RMAnimSchemaElement(String arg, String arg1, int fps) {
        a = arg;
        b = arg1;
        framerate = fps;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        return new UITextButton(FontSizes.schemaButtonTextHeight, TXDB.get("Start RMAnimEditor"), new Runnable() {
            @Override
            public void run() {
                RMAnimRootPanel rmarp = new RMAnimRootPanel(target, new Runnable() {
                    @Override
                    public void run() {
                        path.changeOccurred(false);
                    }
                }, a, b, framerate);
                rmarp.setBounds(new Rect(0, 0, 320, 200));
                launcher.launchOther(rmarp);
            }
        });
    }

    @Override
    public int maxHoldingHeight() {
        return UITextButton.getRecommendedSize("", FontSizes.schemaButtonTextHeight).height;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        // "How should I know?" *commences shrugging*
    }
}
