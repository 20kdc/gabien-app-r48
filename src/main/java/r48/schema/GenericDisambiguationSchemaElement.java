/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema;

import gabien.ui.UIElement;
import r48.RubyIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.HashMap;

/**
 * Hopefully It's Generic Enough This Time (tm)
 * Created on 2/16/17.
 */
public class GenericDisambiguationSchemaElement extends SchemaElement {
    public String iVar;
    public HashMap<Integer, SchemaElement> mapping;

    public GenericDisambiguationSchemaElement(String disambiguationIVar, HashMap<Integer, SchemaElement> baseSE) {
        iVar = disambiguationIVar;
        mapping = baseSE;
    }

    public SchemaElement getDisambiguation(RubyIO target) {
        SchemaElement ise = mapping.get((int) (target.getInstVarBySymbol(iVar).fixnumVal));
        if (ise == null)
            return mapping.get(-1);
        return ise;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
        return getDisambiguation(target).buildHoldingEditor(target, launcher, path);
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        getDisambiguation(target).modifyVal(target, path, setDefault);
    }
}
