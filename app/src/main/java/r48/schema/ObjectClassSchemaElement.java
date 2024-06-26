/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import r48.dbs.PathSyntax;
import r48.io.data.IRIO;
import r48.schema.specialized.BaseRubyTableSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;

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

    public ObjectClassSchemaElement(String clsSym, @NonNull SchemaElement back, char typ) {
        super(back.app);
        symbol = clsSym;
        backing = back;
        type = typ;
    }

    @Override
    public UIElement buildHoldingEditorImpl(IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        if (checkType(target, type, symbol, false))
            return objectHasBecomeInvalidScreen(path);

        LinkedList<String> iVars = new LinkedList<String>();
        boolean enableIVarCheck = findAndAddIVars(backing, target, iVars);
        if (enableIVarCheck) {
            for (String s : target.getIVars()) {
                if (!iVars.contains(s)) {
                    System.out.println("WARNING: iVar " + s + " of " + symbol + " wasn't handled.");
                    System.out.println("This usually means the schema is incomplete.");
                }
            }
        }

        return backing.buildHoldingEditor(target, launcher, path);
    }

    private boolean findAndAddIVars(SchemaElement ise, IRIO target, LinkedList<String> iVars) {
        ise = AggregateSchemaElement.extractField(ise, target);
        // Final type disambiguation
        if (ise instanceof PathSchemaElement) {
            // This catches normal iVars, though could cause some harmless spillage
            String n = PathSyntax.getAbsoluteIVar(((PathSchemaElement) ise).pStr);
            if (n != null)
                iVars.add(n);
            return true;
        }
        if (ise instanceof BaseRubyTableSchemaElement) {
            String n = PathSyntax.getAbsoluteIVar(((BaseRubyTableSchemaElement) ise).iVar);
            if (n != null)
                iVars.add(n);
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
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        setDefault = SchemaElement.checkType(target, type, symbol, setDefault);
        if (setDefault)
            target.setObject(symbol);
        backing.modifyVal(target, path, setDefault);
        if (setDefault)
            path.changeOccurred(true);
    }

    @Override
    public void visitChildren(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths) {
        backing.visit(target, path, v, detailedPaths);
    }

    @Override
    public String windowTitleSuffix(SchemaPath path) {
        return backing.windowTitleSuffix(path);
    }
}
