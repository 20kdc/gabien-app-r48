/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import gabien.uslx.append.*;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UITextButton;
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
            return app.fmt.interpretParameter(rubyIO, heldElement, true);
        }
    };

    public SubwindowSchemaElement(@NonNull SchemaElement encap) {
        super(encap.app);
        heldElement = encap;
    }

    public SubwindowSchemaElement(@NonNull SchemaElement encap, IFunction<IRIO, String> naming) {
        super(encap.app);
        heldElement = encap;
        nameGetter = naming;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        // This is never meant to *actually* scroll.
        String text = nameGetter.apply(target);
        String[] lines = text.split("\n");
        UIElement r = new UITextButton(lines[0], app.f.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                launcher.pushObject(path.newWindow(heldElement, target));
            }
        });
        for (int i = 1; i < lines.length; i++)
            r = new UINSVertLayout(r, new UILabel(lines[i], app.f.schemaFieldTextHeight));
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
