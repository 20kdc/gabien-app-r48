/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized;

import gabien.ui.IConsumer;
import gabien.ui.IFunction;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.ISpritesheetProvider;
import r48.ui.UISpritesheetChoice;

/**
 * Created on 29/07/17.
 */
public class SpritesheetCoreSchemaElement extends r48.schema.SchemaElement {
    public String text;
    public int defaultVal;

    public IFunction<RubyIO, RubyIO> numberProvider;
    public IFunction<RubyIO, ISpritesheetProvider> provider;

    public SpritesheetCoreSchemaElement(String propTranslated, int def, IFunction<RubyIO, RubyIO> nprov, IFunction<RubyIO, ISpritesheetProvider> core) {
        text = propTranslated;
        defaultVal = def;
        numberProvider = nprov;
        provider = core;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final ISpritesheetProvider localProvider = provider.apply(target);
        final RubyIO actTarg = numberProvider.apply(target);
        return new UITextButton(FontSizes.schemaButtonTextHeight, FormatSyntax.formatExtended(text, actTarg), new Runnable() {
            @Override
            public void run() {
                TempDialogSchemaChoice temp = new TempDialogSchemaChoice(null, null, path);
                final SchemaPath innerPath = path.newWindow(temp, target);
                temp.heldDialog = new UISpritesheetChoice((int) actTarg.fixnumVal, localProvider, new IConsumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        actTarg.fixnumVal = integer;
                        innerPath.changeOccurred(false);
                        launcher.switchObject(innerPath.findBack());
                    }
                });
                launcher.switchObject(innerPath);
            }
        });
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        RubyIO actTarg = numberProvider.apply(target);
        if (IntegerSchemaElement.ensureType(actTarg, 'i', setDefault)) {
            actTarg.fixnumVal = defaultVal;
            path.changeOccurred(true);
        }
    }
}
