/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.IGrDriver;
import gabien.IPeripherals;
import gabien.ui.IFunction;
import gabien.ui.UIElement;
import r48.RubyIO;
import r48.dbs.IProxySchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Allows for things to disappear & appear as needed.
 * Created on 04/08/17.
 */
public class HiddenSchemaElement extends SchemaElement implements IProxySchemaElement {
    public final SchemaElement content;
    public final IFunction<RubyIO, Boolean> show;

    public HiddenSchemaElement(SchemaElement hide, IFunction<RubyIO, Boolean> shouldShow) {
        content = hide;
        show = shouldShow;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
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
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        content.modifyVal(target, path, setDefault);
    }

    @Override
    public SchemaElement getEntry() {
        return content;
    }
}
