/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import r48.RubyIO;
import r48.dbs.CMDB;
import r48.dbs.RPGCommand;
import r48.dbs.SDB;
import r48.schema.ArraySchemaElement;
import r48.schema.ISchemaElement;
import r48.schema.util.SchemaPath;

import java.util.LinkedList;

/**
 * ArraySchemaElement + some eventcommand specific stuff to automatically correct issues.
 * Created on 1/2/17.
 */
public class EventCommandArraySchemaElement extends ArraySchemaElement {
    private final CMDB database;
    public EventCommandArraySchemaElement(ISchemaElement eventCommand, CMDB db) {
        super(eventCommand, 0, false);
        database = db;
    }

    @Override
    public boolean autoCorrectArray(RubyIO array, SchemaPath path) {
        if (!SDB.allowControlOfEventCommandIndent)
            return false;
        boolean needsEndingBlock = false;
        if (array.arrVal.length == 0) {
            needsEndingBlock = true;
        } else {
            if (array.arrVal[array.arrVal.length - 1].getInstVarBySymbol("@code").fixnumVal != 0)
                needsEndingBlock = true;
        }

        LinkedList<RubyIO> arr = new LinkedList<RubyIO>();
        for (RubyIO rio : array.arrVal)
            arr.add(rio);

        if (needsEndingBlock) {
            // 0 so that the code won't combust from lacking an array
            RubyIO c = SchemaPath.createDefaultValue(subelems, new RubyIO().setFX(0));
            c.getInstVarBySymbol("@code").fixnumVal = 0;
            arr.add(c);
        }

        boolean modified = needsEndingBlock;
        // NOTE: This method is deliberately awkward to allow for the concurrent modification...
        // Attempting to 'fix' it will only make it worse.
        boolean lastWasBlockLeave = false;
        int lastCode = -1;
        for (int i = 0; i < arr.size(); i++) {
            int code = (int) arr.get(i).getInstVarBySymbol("@code").fixnumVal;
            RPGCommand rc = database.knownCommands.get(code);
            if (rc != null) {
                if (rc.needsBlockLeavePre) {
                    if (!lastWasBlockLeave) {
                        if (rc.blockLeaveReplacement != lastCode) {
                            RubyIO c = SchemaPath.createDefaultValue(subelems, new RubyIO().setFX(0));
                            c.getInstVarBySymbol("@code").fixnumVal = 0;
                            c.getInstVarBySymbol("@indent").fixnumVal = arr.get(i).getInstVarBySymbol("@indent").fixnumVal + 1;
                            arr.add(i, c);
                            // About to re-handle the same code.
                            lastWasBlockLeave = true;
                            lastCode = 0;
                            modified = true;
                            continue;
                        }
                    }
                }
                lastWasBlockLeave = rc.typeBlockLeave;
            } else {
                lastWasBlockLeave = false;
            }
            lastCode = code;
        }

        if (modified)
            array.arrVal = arr.toArray(new RubyIO[0]);
        return modified;
    }
}
