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
    public String text;
    public int defaultVal;
    public IFunction<RubyIO, ISpritesheetProvider> provider;

    public SpritesheetCoreSchemaElement(String propTranslated, int def, IFunction<RubyIO, ISpritesheetProvider> core) {
        text = propTranslated;
        defaultVal = def;
        provider = core;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final ISpritesheetProvider localProvider = provider.apply(target);
        final RubyIO actTarg = localProvider.numberHolder();
        return new UITextButton(FontSizes.schemaButtonTextHeight, FormatSyntax.formatExtended(text, actTarg), new Runnable() {
            @Override
            public void run() {
                launcher.switchObject(path.newWindow(new TempDialogSchemaChoice(new UISpritesheetChoice((int) actTarg.fixnumVal, localProvider, new IConsumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        actTarg.fixnumVal = integer;
                        path.changeOccurred(false);
                        launcher.switchObject(path.findBack());
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
        ISpritesheetProvider localProvider = provider.apply(target);
        RubyIO actTarg = localProvider.numberHolder();
        if (IntegerSchemaElement.ensureType(actTarg, 'i', setDefault)) {
            actTarg.fixnumVal = defaultVal;
            path.changeOccurred(true);
        }
    }
}
