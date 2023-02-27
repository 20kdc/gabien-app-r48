/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import org.eclipse.jdt.annotation.Nullable;

import r48.App;
import r48.RubyTable;
import r48.dbs.PathSyntax;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

/**
 * Contains just the boilerplate bits of RubyTableSchemaElement.
 * Pulled out on 3/3/2020.
 */
public abstract class BaseRubyTableSchemaElement extends SchemaElement {

    public final int defW;
    public final int defH;
    public final int planes;
    public final int dimensions;
    public final @Nullable PathSyntax iVar;
    public final int[] defVals;

    public BaseRubyTableSchemaElement(App app, int dw, int dh, int p, int d, @Nullable PathSyntax iV, int[] defaults) {
        super(app);
        defW = dw;
        defH = dh;
        planes = p;
        dimensions = d;
        iVar = iV;
        defVals = defaults;
    }
    
    public IRIO extractTarget(IRIO target) {
        return iVar == null ? target : iVar.get(target);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath index, boolean setDefault) {
        boolean needChange = setDefault;
    
        if (iVar != null) {
            IRIO st = iVar.get(target);
            if (st == null) {
                st = iVar.add(target);
                needChange = true;
            }
            target = st;
        }
    
        if (target.getType() != 'u') {
            needChange = true;
        } else if (!target.getSymbol().equals("Table")) {
            needChange = true;
        }
    
        // Re-initialize if all else fails.
        // (This will definitely trigger if the iVar was missing or if setDefault was on)
    
        boolean changeOccurred = false;
        if (needChange) {
            target.setUser("Table", new RubyTable(dimensions, defW, defH, planes, defVals).innerBytes);
            changeOccurred = true;
        }
    
        // Fix up pre v1.0-2 tables (would have existed from the start if I knew about it, but...)
        RubyTable rt = new RubyTable(target.getBuffer());
        if (rt.dimensionCount != dimensions) {
            rt.innerTable.putInt(0, dimensions);
            changeOccurred = true;
        }
        if (changeOccurred)
            index.changeOccurred(true);
    }

}