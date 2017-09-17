/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.UIElement;
import r48.RubyIO;
import r48.dbs.PathSyntax;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.HashMap;

/**
 * This is used for things like conditional branches, which are so complicated it's ridiculous.
 * Note that the element MUST be an enum - enums trigger a UI rebuild when they're set.
 * Created on 12/31/16.
 */
public class DisambiguatorSchemaElement extends SchemaElement {
    // Special values:
    // "$fail": There is no disambiguator. Implemented via PathSyntax.
    // #hastilyAddedFeatures
    public String dIndex;
    public SchemaElement defaultType;
    public HashMap<Integer, SchemaElement> dTable;

    public DisambiguatorSchemaElement(String disambiguatorIndex, SchemaElement backup, HashMap<Integer, SchemaElement> disambiguations) {
        dIndex = disambiguatorIndex;
        defaultType = backup;
        dTable = disambiguations;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path2) {
        final SchemaPath path = path2.tagSEMonitor(target, this);
        int iv = getDisambigIndex(target);
        SchemaElement ise = getSchemaElement(iv);
        return ise.buildHoldingEditor(target, launcher, path);
    }

    private int getDisambigIndex(RubyIO target) {
        if (dIndex == null)
            return 0;
        target = PathSyntax.parse(target, dIndex);
        if (target == null)
            return 0x7FFFFFFF;
        return (int) target.fixnumVal;
    }

    private SchemaElement getSchemaElement(int dVal) {
        SchemaElement r = dTable.get(dVal);
        if (r == null)
            r = defaultType;
        if (r == null)
            r = new AggregateSchemaElement(new SchemaElement[0]);
        return r;
    }

    // used by OCSE
    public SchemaElement getDisambiguation(RubyIO target) {
        return getSchemaElement(getDisambigIndex(target));
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path2, boolean setDefault) {
        final SchemaPath path = path2.tagSEMonitor(target, this);

        int iv = getDisambigIndex(target);
        if (iv == 0x7FFFFFFF)
            System.out.println("Warning: Disambiguator working off of nothing here, this CANNOT GO WELL");
        try {
            SchemaElement ise = getSchemaElement(iv);
            ise.modifyVal(target, path, setDefault);
        } catch (RuntimeException e) {
            e.printStackTrace(System.out);
            System.out.println("ArrayDisambiguator Debug: " + iv);
            throw e;
        }
    }
}
