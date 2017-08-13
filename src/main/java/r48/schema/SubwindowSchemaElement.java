/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.IFunction;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.IProxySchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UINSVertLayout;

/**
 * Created on 12/29/16.
 */
public class SubwindowSchemaElement extends SchemaElement implements IProxySchemaElement {
    public SchemaElement heldElement;
    public IFunction<RubyIO, String> nameGetter = new IFunction<RubyIO, String>() {
        @Override
        public String apply(RubyIO rubyIO) {
            return FormatSyntax.interpretParameter(rubyIO, heldElement, true);
        }
    };

    public SubwindowSchemaElement(SchemaElement encap) {
        heldElement = encap;
    }

    public SubwindowSchemaElement(SchemaElement encap, IFunction<RubyIO, String> naming) {
        heldElement = encap;
        nameGetter = naming;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        // This is never meant to *actually* scroll.
        String text = nameGetter.apply(target);
        String[] lines = text.split("\n");
        UIElement r = new UITextButton(FontSizes.schemaButtonTextHeight, lines[0], new Runnable() {
            @Override
            public void run() {
                launcher.switchObject(path.newWindow(heldElement, target));
            }
        });
        for (int i = 1; i < lines.length; i++)
            r = new UINSVertLayout(r, new UILabel(lines[i], FontSizes.schemaFieldTextHeight));
        return r;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        heldElement.modifyVal(target, path, setDefault);
    }

    @Override
    public SchemaElement getEntry() {
        return heldElement;
    }
}
