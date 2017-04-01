/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.IConsumer;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.schema.specialized.TempDialogSchemaChoice;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIEnumChoice;

import java.util.HashMap;

/**
 * Enum. Note that it is critical to implementation of many things that this explicitly switch into a new view.
 * Specifically, the switch will cause a UI rebuild upon return, which is required to keep data consistency.
 * Created on 12/30/16.
 */
public class SymEnumSchemaElement implements ISchemaElement {
    public String[] options;
    public HashMap<String, Integer> viewOptions;

    public SymEnumSchemaElement(String[] o) {
        options = o;
        viewOptions = new HashMap<String, Integer>();
        for (int i = 0; i < o.length; i++)
            viewOptions.put(o[i], i);
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        return new UITextButton(FontSizes.schemaButtonTextHeight, target.symVal, new Runnable() {
            @Override
            public void run() {
                launcher.switchObject(path.newWindow(new TempDialogSchemaChoice(new UIEnumChoice(new IConsumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        if (integer < 0)
                            return;
                        if (integer >= options.length)
                            return;
                        target.symVal = options[integer];
                        path.changeOccurred(false);
                        // Enums can affect parent format, so deal with that now.
                        launcher.switchObject(path.findBack());
                    }
                }, viewOptions, ""), path), target, launcher));
            }
        });
    }

    @Override
    public int maxHoldingHeight() {
        return UITextButton.getRecommendedSize("", FontSizes.schemaButtonTextHeight).height;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (IntegerSchemaElement.ensureType(target, ':', setDefault)) {
            target.symVal = options[0];
            path.changeOccurred(true);
        }
    }
}
