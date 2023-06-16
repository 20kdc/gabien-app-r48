/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import org.eclipse.jdt.annotation.NonNull;

import gabien.uslx.append.*;
import gabien.wsi.IPeripherals;
import gabien.render.IGrDriver;
import gabien.ui.UIElement;
import r48.dbs.IProxySchemaElement;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Allows for things to disappear & appear as needed.
 * Created on 04/08/17.
 */
public class HiddenSchemaElement extends SchemaElement implements IProxySchemaElement {
    public final SchemaElement content;
    public final IFunction<IRIO, Boolean> show;

    public HiddenSchemaElement(@NonNull SchemaElement hide, IFunction<IRIO, Boolean> shouldShow) {
        super(hide.app);
        content = hide;
        show = shouldShow;
    }

    @Override
    public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
        if (show.apply(target))
            return content.buildHoldingEditor(target, launcher, path);
        return makeHiddenElement();
    }

    public static UIElement makeHiddenElement() {
        return new UIElement(0, 0) {
            @Override
            public void update(double deltaTime, boolean selected, IPeripherals peripherals) {

            }

            @Override
            public void render(IGrDriver igd) {

            }
        };
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        content.modifyVal(target, path, setDefault);
    }

    @Override
    public SchemaElement getEntry() {
        return content;
    }
}
