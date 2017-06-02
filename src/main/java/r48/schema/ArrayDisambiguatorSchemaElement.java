/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import r48.RubyIO;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.HashMap;

/**
 * This is used for things like conditional branches, which are so complicated it's ridiculous.
 * Note that the element MUST be an enum - enums trigger a UI rebuild when they're set.
 * Created on 12/31/16.
 */
public class ArrayDisambiguatorSchemaElement extends SchemaElement {
    // Special values:
    // -1: There is no disambiguator (value is assumed to be 0). You should use a disambiguatorType of nil here.
    // #hastilyAddedFeatures
    public int dIndex;
    public SchemaElement dType;
    public SchemaElement defaultType;
    public HashMap<Integer, SchemaElement> dTable;

    public ArrayDisambiguatorSchemaElement(int disambiguatorIndex, SchemaElement disambiguatorType, SchemaElement backup, HashMap<Integer, SchemaElement> disambiguations) {
        dIndex = disambiguatorIndex;
        dType = disambiguatorType;
        defaultType = backup;
        dTable = disambiguations;
    }

    @Override
    public boolean monitorsSubelements() {
        return true;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        UIPanel p = new UIPanel() {
            public UIElement subElem = rebuildSubElem();

            public UIElement rebuildSubElem() {
                int iv = getDisambigIndex(target);
                try {
                    SchemaElement ise = getSchemaElement(iv);
                    UIElement se = ise.buildHoldingEditor(target, launcher, path);
                    allElements.add(se);
                    return se;
                } catch (RuntimeException e) {
                    e.printStackTrace(System.out);
                    System.out.println("ArrayDisambiguator Debug: " + iv);
                    throw e;
                }
            }

            @Override
            public void setBounds(Rect r) {
                super.setBounds(r);
                subElem.setBounds(new Rect(0, 0, r.width, r.height));
            }
        };
        p.setBounds(new Rect(0, 0, 320, maxHoldingHeight()));
        return p;
    }

    private int getDisambigIndex(RubyIO target) {
        if (dIndex == -1)
            return 0;
        if (target.arrVal.length <= dIndex)
            return 0x7FFFFFFF;
        int dVal = (int) target.arrVal[dIndex].fixnumVal;
        return dVal;
    }

    private SchemaElement getSchemaElement(int dVal) {
        SchemaElement r = dTable.get(dVal);
        if (r == null)
            r = defaultType;
        return r;
    }

    @Override
    public int maxHoldingHeight() {
        int height = defaultType.maxHoldingHeight();
        for (SchemaElement possible : dTable.values())
            height = Math.max(possible.maxHoldingHeight(), height);
        return height;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        // ensure target is an array (and that's about it, since this is defined by array elements)
        setDefault = IntegerSchemaElement.ensureType(target, '[', setDefault);
        boolean modified = false;
        if (setDefault || (target.arrVal == null)) {
            target.arrVal = new RubyIO[dIndex + 1];
            for (int i = 0; i < target.arrVal.length; i++)
                target.arrVal[i] = new RubyIO().setNull();
            modified = true;
        }

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
        if (modified)
            path.changeOccurred(true);
    }
}
