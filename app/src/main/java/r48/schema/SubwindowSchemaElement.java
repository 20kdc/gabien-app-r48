/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.ui.IFunction;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.dbs.FormatSyntax;
import r48.dbs.IProxySchemaElement;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UINSVertLayout;

/**
 * Created on 12/29/16.
 */
public class SubwindowSchemaElement extends SchemaElement implements IProxySchemaElement {
    public SchemaElement heldElement;
    public IFunction<IRIO, String> nameGetter = new IFunction<IRIO, String>() {
        @Override
        public String apply(IRIO rubyIO) {
            return FormatSyntax.interpretParameter(rubyIO, heldElement, true);
        }
    };

    public SubwindowSchemaElement(SchemaElement encap) {
        heldElement = encap;
    }

    public SubwindowSchemaElement(SchemaElement encap, IFunction<IRIO, String> naming) {
        heldElement = encap;
        nameGetter = naming;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        // This is never meant to *actually* scroll.
        String text = nameGetter.apply(target);
        String[] lines = text.split("\n");
        UIElement r = new UITextButton(lines[0], FontSizes.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                launcher.pushObject(path.newWindow(heldElement, target));
            }
        });
        for (int i = 1; i < lines.length; i++)
            r = new UINSVertLayout(r, new UILabel(lines[i], FontSizes.schemaFieldTextHeight));
        return r;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        heldElement.modifyVal(target, path, setDefault);
    }

    @Override
    public SchemaElement getEntry() {
        return heldElement;
    }
}
