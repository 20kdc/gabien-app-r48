/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.dbs;

import gabien.ui.UIElement;
import r48.AppMain;
import r48.RubyIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Has to be here because of protections.
 * Has to be a separate class so RPGCommand stuff can 'see through it'.
 * Created on 1/3/17.
 */
public class NameProxySchemaElement extends SchemaElement implements IProxySchemaElement {
    private final String tx;
    private boolean useCache = true;
    private SchemaElement cache = null;

    public NameProxySchemaElement(String text, boolean useCach) {
        tx = text;
        useCache = useCach;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        return getEntry().buildHoldingEditor(target, launcher, path);
    }

    @Override
    public SchemaElement getEntry() {
        if (cache != null)
            return cache;
        SchemaElement r = AppMain.schemas.schemaTrueDatabase.get(tx);
        if (r == null)
            throw new RuntimeException("Schema used " + tx + ", but it didn't exist when invoked.");
        if (useCache)
            cache = r;
        return r;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        getEntry().modifyVal(target, path, setDefault);
    }
}
