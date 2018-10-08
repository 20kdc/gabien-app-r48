/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import r48.RubyIO;
import r48.dbs.IProxySchemaElement;
import r48.dbs.PathSyntax;
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
            if (target.iVarKeys != null) {
                for (String s : target.iVarKeys) {
                    if (!iVars.contains(s)) {
                        System.out.println("WARNING: iVar " + s + " of " + symbol + " wasn't handled.");
                        System.out.println("This usually means the schema is incomplete.");
                    }
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
            if (ise instanceof IProxySchemaElement) {
                ise = ((IProxySchemaElement) ise).getEntry();
                proxyHandling = true;
            }
            if (ise instanceof DisambiguatorSchemaElement) {
                ise = ((DisambiguatorSchemaElement) ise).getDisambiguation(target);
                proxyHandling = true;
            }
        }
        // Final type disambiguation
        if (ise instanceof PathSchemaElement) {
            // This catches normal iVars, though could cause some harmless spillage
            String n = PathSyntax.getAbsoluteIVar(((PathSchemaElement) ise).pStr);
            if (n != null)
                iVars.add(n);
            return true;
        }
        if (ise instanceof RubyTableSchemaElement) {
            if (((RubyTableSchemaElement) ise).iVar.equals("."))
                return false;
            iVars.add(((RubyTableSchemaElement) ise).iVar);
            return true;
        }
        if (ise instanceof HalfsplitSchemaElement)
            return findAndAddIVars(((HalfsplitSchemaElement) ise).a, target, iVars) || findAndAddIVars(((HalfsplitSchemaElement) ise).b, target, iVars);
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
