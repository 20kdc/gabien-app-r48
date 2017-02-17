/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.schema.ISchemaElement;
import r48.schema.specialized.rmanim.RMAnimRootPanel;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * This starts a UI-framework-based special editing package,
 *  which calls back to this for edit notifications.
 * Created on 2/17/17.
 */
public class RMAnimSchemaElement implements ISchemaElement {
    private String a, b;
    public RMAnimSchemaElement(String arg, String arg1) {
        a = arg;
        b = arg1;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        return new UITextButton(FontSizes.schemaButtonTextHeight, "Start RMAnimEditor", new Runnable() {
            @Override
            public void run() {
                RMAnimRootPanel rmarp = new RMAnimRootPanel(target, new Runnable() {
                    @Override
                    public void run() {
                        path.changeOccurred(false);
                    }
                }, a, b);
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
