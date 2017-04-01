/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIPanel;
import r48.RubyIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.HashMap;

/**
 * This is used for things like conditional branches, which are so complicated it's ridiculous.
 * Note that the element MUST be an enum - enums trigger a UI rebuild when they're set.
 * Created on 12/31/16.
 */
public class ArrayDisambiguatorSchemaElement implements ISchemaElement {
    // If -1, then this is just a container for an array structure
    // #hastilyAddedFeatures
    public int dIndex;
    public ISchemaElement dType;
    public ISchemaElement defaultType;
    public HashMap<Integer, ISchemaElement> dTable;

    public ArrayDisambiguatorSchemaElement(int disambiguatorIndex, ISchemaElement disambiguatorType, ISchemaElement backup, HashMap<Integer, ISchemaElement> disambiguations) {
        dIndex = disambiguatorIndex;
        dType = disambiguatorType;
        defaultType = backup;
        dTable = disambiguations;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, SchemaPath path2) {
        final SchemaPath path = path2.arrayEntry(target, this);
        UIPanel p = new UIPanel() {
            public UIElement subElem = rebuildSubElem();

            public UIElement rebuildSubElem() {
                int iv = getDisambigIndex(target);
                try {
                    ISchemaElement ise = getSchemaElement(iv);
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

    private ISchemaElement getSchemaElement(int dVal) {
        ISchemaElement r = dTable.get(dVal);
        if (r == null)
            r = defaultType;
        return r;
    }

    @Override
    public int maxHoldingHeight() {
        int height = defaultType.maxHoldingHeight();
        for (ISchemaElement possible : dTable.values())
            height = Math.max(possible.maxHoldingHeight(), height);
        return height;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        path = path.arrayEntry(target, this);

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
            ISchemaElement ise = getSchemaElement(iv);
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
