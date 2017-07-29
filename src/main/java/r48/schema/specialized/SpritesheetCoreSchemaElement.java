/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized;

import gabien.ui.IConsumer;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.ISpritesheetProvider;
import r48.ui.UIEnumChoice;
import r48.ui.UISpritesheetChoice;

import java.util.HashMap;

/**
 * Created on 29/07/17.
 */
public class SpritesheetCoreSchemaElement extends r48.schema.SchemaElement {
    public int defaultVal;
    public ISpritesheetProvider provider;

    public SpritesheetCoreSchemaElement(int def, ISpritesheetProvider core) {
        defaultVal = def;
        provider = core;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        return new UITextButton(FontSizes.schemaButtonTextHeight, Long.toString(target.fixnumVal), new Runnable() {
            @Override
            public void run() {
                launcher.switchObject(path.newWindow(new TempDialogSchemaChoice(new UISpritesheetChoice((int) target.fixnumVal, provider, new IConsumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        target.fixnumVal = integer;
                        path.changeOccurred(false);
                    }
                }), null, path), target, launcher));
            }
        });
    }

    @Override
    public int maxHoldingHeight() {
        return UITextButton.getRecommendedSize("", FontSizes.schemaButtonTextHeight).height;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (IntegerSchemaElement.ensureType(target, 'i', setDefault)) {
            target.fixnumVal = defaultVal;
            path.changeOccurred(true);
        }
    }
}
