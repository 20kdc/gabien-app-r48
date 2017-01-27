/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.UIElement;
import r48.dbs.ProxySchemaElement;
import r48.schema.specialized.RubyTableSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.RubyIO;
import r48.schema.util.SchemaPath;

import java.util.LinkedList;

/**
 * Ensures some fields required to be an object or userdata are in place.
 * Notably,
 * this also performs some sanity checks to make sure the definition is "full",
 * and will report warnings to console if IVars are not dealt with.
 * Created on 12/29/16.
 */
public class ObjectClassSchemaElement implements ISchemaElement {
    public ISchemaElement backing;
    public String symbol;
    public char type;
    public ObjectClassSchemaElement(String clsSym, ISchemaElement back, char typ) {
        symbol = clsSym;
        backing = back;
        type = typ;
    }
    @Override
    public UIElement buildHoldingEditor(RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        if (target.type != type)
            throw new RuntimeException("Wrong type passed to ObjectClassSchemaElement (should be " + type + " was " + target.type + ")");
        if (!target.symVal.equals(symbol))
            throw new RuntimeException("Classable of type " + target.symVal + " passed to OCSE of type " + symbol);

        if (backing instanceof AggregateSchemaElement) {
            AggregateSchemaElement ace = (AggregateSchemaElement) backing;
            LinkedList<String> iVars = new LinkedList<String>();
            boolean enableIVarCheck = false;
            for (ISchemaElement ise : ace.aggregate) {

                // Deal with all likely proxy sandwiches
                while (ise instanceof ProxySchemaElement)
                    ise = ((ProxySchemaElement) ise).getEntry();
                while (ise instanceof SubwindowSchemaElement)
                    ise = ((SubwindowSchemaElement) ise).heldElement;
                while (ise instanceof ProxySchemaElement)
                    ise = ((ProxySchemaElement) ise).getEntry();

                if (ise instanceof IVarSchemaElement) {
                    enableIVarCheck = true;
                    iVars.add(((IVarSchemaElement) ise).iVar);
                }
                if (ise instanceof RubyTableSchemaElement)
                    iVars.add(((RubyTableSchemaElement) ise).iVar);
            }
            if (enableIVarCheck) {
                for (String s : target.iVars.keySet()) {
                    if (!iVars.contains(s)) {
                        System.out.println("WARNING: iVar " + s + " of " + symbol + " wasn't handled.");
                        System.out.println("This usually means your schema is incomplete.");
                    }
                }
            }
        }

        return backing.buildHoldingEditor(target, launcher, path);
    }

    @Override
    public int maxHoldingHeight() {
        return backing.maxHoldingHeight();
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        boolean modified = false;
        if (target.type == 0) {
            target.type = type;
            modified = true;
        }
        if (target.symVal == null) {
            target.symVal = symbol;
            modified = true;
        } else if (!target.symVal.equals(symbol)) {
            target.symVal = symbol;
            modified = true;
        }
        backing.modifyVal(target, path, setDefault);
        if (modified)
            path.changeOccurred(true);
    }
}
