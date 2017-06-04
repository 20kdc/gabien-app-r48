/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.ui.UIElement;
import r48.RubyIO;
import r48.dbs.ProxySchemaElement;
import r48.schema.specialized.RubyTableSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.LinkedList;

/**
 * Ensures some fields required to be an object or userdata are in place.
 * Notably,
 * this also performs some sanity checks to make sure the definition is "full",
 * and will report warnings to console if IVars are not dealt with.
 * Created on 12/29/16.
 */
public class ObjectClassSchemaElement extends SchemaElement {
    public SchemaElement backing;
    public String symbol;
    public char type;

    public ObjectClassSchemaElement(String clsSym, SchemaElement back, char typ) {
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

        LinkedList<String> iVars = new LinkedList<String>();
        boolean enableIVarCheck = findAndAddIVars(backing, target, iVars);
        if (enableIVarCheck) {
            for (String s : target.iVars.keySet()) {
                if (!iVars.contains(s)) {
                    System.out.println("WARNING: iVar " + s + " of " + symbol + " wasn't handled.");
                    System.out.println("This usually means your schema is incomplete.");
                }
            }
        }

        return backing.buildHoldingEditor(target, launcher, path);
    }

    private boolean findAndAddIVars(SchemaElement ise, RubyIO target, LinkedList<String> iVars) {
        // Deal with proxies
        boolean proxyHandling = true;
        while (proxyHandling) {
            proxyHandling = false;
            if (ise instanceof ProxySchemaElement) {
                ise = ((ProxySchemaElement) ise).getEntry();
                proxyHandling = true;
            }
            if (ise instanceof SubwindowSchemaElement) {
                ise = ((SubwindowSchemaElement) ise).heldElement;
                proxyHandling = true;
            }
            if (ise instanceof GenericDisambiguationSchemaElement) {
                ise = ((GenericDisambiguationSchemaElement) ise).getDisambiguation(target);
                proxyHandling = true;
            }
        }
        // Final type disambiguation
        if (ise instanceof IVarSchemaElement) {
            iVars.add(((IVarSchemaElement) ise).iVar);
            return true;
        }
        if (ise instanceof RubyTableSchemaElement) {
            iVars.add(((RubyTableSchemaElement) ise).iVar);
            return true;
        }
        if (ise instanceof AggregateSchemaElement) {
            boolean r = false;
            for (SchemaElement se : ((AggregateSchemaElement) ise).aggregate) {
                if (findAndAddIVars(se, target, iVars))
                    r = true;
            }
            return r;
        }
        return false;
    }

    @Override
    public int maxHoldingHeight() {
        return backing.maxHoldingHeight();
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        boolean modified = false;
        if (target.type != type) {
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
