/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

import gabien.ui.UIElement;
import r48.AppMain;
import r48.RubyIO;
import r48.schema.ISchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Has to be here because of protections.
 * Has to be a separate class so RPGCommand stuff can 'see through it'.
 * Created on 1/3/17.
 */
public class ProxySchemaElement implements ISchemaElement {
    private final String tx;
    ISchemaElement cache = null;
    public ProxySchemaElement(String text) {
        tx = text;
    }
    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        return getEntry().buildHoldingEditor(target, launcher, path);
    }

    public ISchemaElement getEntry() {
        if (cache != null)
            return cache;
        ISchemaElement r = AppMain.schemas.schemaTrueDatabase.get(tx);
        if (r == null)
            throw new RuntimeException("Schema used " + tx + ", but it didn't exist when invoked.");
        cache = r;
        return r;
    }

    @Override
    public int maxHoldingHeight() {
        return getEntry().maxHoldingHeight();
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        getEntry().modifyVal(target, path, setDefault);
    }
}
