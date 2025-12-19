/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.op;

import org.eclipse.jdt.annotation.Nullable;

import r48.dbs.CMDB;
import r48.io.data.DMKey;
import r48.io.data.RORIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.arrays.ArraySchemaElement;
import r48.schema.specialized.cmgb.EventCommandArraySchemaElement;
import r48.schema.util.SchemaPath;

import static r48.schema.op.BaseSchemaOps.*;

import java.util.function.Function;

/**
 * For operators that run on a command list, this verifies that command list and returns it.
 * Created 19th December, 2025.
 */
public class CommandListSelection {
    /**
     * CMDB
     */
    public final CMDB cmdb;

    /**
     * Start/end indices. That is to say, before we started messing with things.
     */
    public final int startIndex, endIndex;

    public CommandListSelection(CMDB cmdb, int start, int end) {
        this.cmdb = cmdb;
        startIndex = start;
        endIndex = end;
    }

    /**
     * Extracts a command list selection.
     */
    public static @Nullable CommandListSelection extractSelection(SchemaPath path, Function<String, DMKey> parameters) {
        // Must always be an array.
        if (path.targetElement.getType() != '[')
            return null;
        // Firstly, figure out the CMDB. This also confirms the array we have is what it's supposed to be.
        CMDB cmdb = null;
        SchemaElement se = AggregateSchemaElement.extractField(path.editor, path.targetElement);
        // We may be in the tracking SE of a command; break out if so.
        if (se instanceof ArraySchemaElement.TrackingSE)
            se = ((ArraySchemaElement.TrackingSE) se).parentArraySE;
        // Is this an event command array?
        if (se instanceof EventCommandArraySchemaElement)
            cmdb = ((EventCommandArraySchemaElement) se).database;
        if (cmdb == null)
            return null;
        // Now for the selection stuff.
        RORIO arrayStartK = getParamOrDMNull(parameters, CTXPARAM_ARRAYSTART);
        RORIO arrayEndK = getParamOrDMNull(parameters, CTXPARAM_ARRAYEND);
        int startIndex = 0;
        int endIndex = path.targetElement.getALen();
        if (arrayStartK.getType() == 'i' && arrayEndK.getType() == 'i') {
            startIndex = (int) arrayStartK.getFX();
            endIndex = (int) arrayEndK.getFX();
        }
        if (endIndex < startIndex || startIndex < 0 || endIndex < 0)
            return null;
        return new CommandListSelection(cmdb, startIndex, endIndex);
    }
}
