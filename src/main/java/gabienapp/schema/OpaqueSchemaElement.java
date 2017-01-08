/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.schema;

import gabien.ui.*;
import gabienapp.Application;
import gabienapp.UITest;
import gabienapp.schema.util.ISchemaHost;
import gabienapp.RubyIO;
import gabienapp.schema.util.SchemaPath;

/**
 * Created on 12/28/16.
 */
public class OpaqueSchemaElement implements ISchemaElement {

    public String getMessage() {
        return "Can't edit:";
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        return new UITextButton(false, getMessage() + target, new Runnable() {
            @Override
            public void run() {
                launcher.launchOther(new UITest(target));
            }
        });
    }

    @Override
    public int maxHoldingHeight() {
        return 9;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        // Not a clue, so re-initialize if all else fails.
        if ((target.type == 0) || setDefault) {
            target.setNull();
            path.changeOccurred(true);
        }
    }
}
