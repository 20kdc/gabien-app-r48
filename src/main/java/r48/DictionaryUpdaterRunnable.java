/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48;

import gabien.ui.IFunction;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;
import r48.toolsets.MapToolset;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to build convenient dictionaries for selecting things.
 * Created on 1/3/17.
 */
public class DictionaryUpdaterRunnable implements Runnable {
    // act soon after init.
    private boolean actNow = true;
    public final String dict, targ, iVar;
    // Responsible for removing any initial wrapping
    public final IFunction<RubyIO, RubyIO> fieldA;
    public final boolean hash;
    private RubyIO lastTarget = null;
    private Runnable kickMe;

    public DictionaryUpdaterRunnable(String targetDictionary, String target, IFunction<RubyIO, RubyIO> iFunction, boolean b, String ivar) {
        dict = targetDictionary;
        targ = target;
        fieldA = iFunction;
        hash = b;
        iVar = ivar;
        // Cause a proxy to be generated.
        AppMain.schemas.getSDBEntry(targetDictionary);
        kickMe = new Runnable() {
            @Override
            public void run() {
                actNow = true;
            }
        };
    }

    public void actIfRequired(RubyIO map) {
        if (actNow) {
            actNow = false;
            // actually update
            HashMap<Integer, String> finalMap = new HashMap<Integer, String>();
            RubyIO target;
            if (targ.equals("__MAP__")) {
                target = map;
            } else {
                target = AppMain.objectDB.getObject(targ);
            }
            if (target != null) {
                if (lastTarget != target) {
                    if (lastTarget != null)
                        AppMain.objectDB.deregisterModificationHandler(lastTarget, kickMe);
                    AppMain.objectDB.registerModificationHandler(target, kickMe);
                }
                lastTarget = target;
                if (fieldA != null)
                    target = fieldA.apply(target);
                if (target == null)
                    return; // :(
                if (hash) {
                    for (Map.Entry<RubyIO, RubyIO> rio : target.hashVal.entrySet()) {
                        handleVal(finalMap, rio.getValue(), (int) rio.getKey().fixnumVal);
                    }
                } else {
                    for (int i = 0; i < target.arrVal.length; i++) {
                        RubyIO rio = target.arrVal[i];
                        handleVal(finalMap, rio, i);
                    }
                }
            } else {
                actNow = true;
            }
            finalizeVals(finalMap);
        }
    }

    private void finalizeVals(HashMap<Integer, String> finalMap) {
        SchemaElement ise = new EnumSchemaElement(finalMap, 0, "ID.");
        AppMain.schemas.setSDBEntry(dict, ise);
    }

    private void handleVal(HashMap<Integer, String> finalMap, RubyIO rio, int fixnumVal) {
        if (rio.type != '0') {
            if (iVar == null) {
                finalMap.put(fixnumVal, rio.decString());
            } else {
                finalMap.put(fixnumVal, rio.getInstVarBySymbol(iVar).decString());
            }
        }
    }

    @Override
    public void run() {
        actNow = true;
    }

    public void sanitize() {
        finalizeVals(new HashMap<Integer, String>());
    }
}
