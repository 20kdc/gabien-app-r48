/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import r48.RubyIO;
import r48.dbs.CMDB;
import r48.dbs.RPGCommand;
import r48.dbs.SDB;
import r48.schema.SchemaElement;
import r48.schema.arrays.StandardArraySchemaElement;
import r48.schema.util.SchemaPath;

import java.util.Collections;
import java.util.LinkedList;

/**
 * ArraySchemaElement + some eventcommand specific stuff to automatically correct issues.
 * (On top of it's previous behaviors, this is now responsible for indent. It monitors subelements *anyway*,
 * and moving the logic here allows me to cut off some of SchemaPath's rube-goldberg-iness.
 * Anything to simplify that thing. Jun 2, 2017.)
 * Created on 1/2/17.
 */
public class EventCommandArraySchemaElement extends StandardArraySchemaElement {
    private final CMDB database;

    public EventCommandArraySchemaElement(SchemaElement eventCommand, CMDB db) {
        super(eventCommand, 0, false);
        database = db;
    }

    @Override
    public boolean monitorsSubelements() {
        return true;
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
        Collections.addAll(arr, array.arrVal);

        if (needsEndingBlock) {
            // 0 so that the code won't combust from lacking an array
            RubyIO c = SchemaPath.createDefaultValue(subelems, new RubyIO().setFX(0));
            c.getInstVarBySymbol("@code").fixnumVal = database.listLeaveCmd;
            arr.add(c);
        }

        boolean modified = needsEndingBlock;

        // NOTE: This method is deliberately awkward to allow for the concurrent modification...
        // Attempting to 'fix' it will only make it worse.
        boolean lastWasBlockLeave = false;
        int lastCode = -1;

        // Indent tracking
        int indent = 0;

        // Note that this array can grow as it's being searched.
        for (int i = 0; i < arr.size(); i++) {
            RubyIO commandTarg = arr.get(i);
            int code = (int) commandTarg.getInstVarBySymbol("@code").fixnumVal;
            RPGCommand rc = database.knownCommands.get(code);
            if (rc != null) {
                // Indent stuff
                indent += rc.indentPre;
                if (indent != commandTarg.getInstVarBySymbol("@indent").fixnumVal) {
                    commandTarg.getInstVarBySymbol("@indent").fixnumVal = indent;
                    modified = true;
                }
                indent += rc.indentPost.apply(commandTarg.getInstVarBySymbol("@parameters"));

                if (rc.needsBlockLeavePre) {
                    if (!lastWasBlockLeave) {
                        if (rc.blockLeaveReplacement != lastCode) {
                            RubyIO c = SchemaPath.createDefaultValue(subelems, new RubyIO().setFX(0));
                            c.getInstVarBySymbol("@code").fixnumVal = 0;
                            c.getInstVarBySymbol("@indent").fixnumVal = commandTarg.getInstVarBySymbol("@indent").fixnumVal + 1;
                            arr.add(i, c);
                            // About to re-handle the same code.
                            lastWasBlockLeave = true;
                            // What to do here depends on a few things. They'll be handled in CMDB.
                            lastCode = database.blockLeaveCmd;
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
