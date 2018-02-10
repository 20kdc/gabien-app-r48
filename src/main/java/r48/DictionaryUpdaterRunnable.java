/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import gabien.ui.IConsumer;
import gabien.ui.IFunction;
import r48.dbs.TXDB;
import r48.dbs.ValueSyntax;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to build convenient dictionaries for selecting things.
 * Created on 1/3/17.
 */
public class DictionaryUpdaterRunnable implements Runnable {
    // act soon after init.
    private boolean actNow = true;
    public final String dict, targ;
    // Responsible for removing any wrapping
    // fieldA gets the root's wrapping, iVar gets the inner wrapping
    public final IFunction<RubyIO, RubyIO> fieldA, iVar;
    public final boolean hash;
    public final int defaultVal;
    private String lastTarget = null;
    private IConsumer<SchemaPath> kickMe;

    public DictionaryUpdaterRunnable(String targetDictionary, String target, IFunction<RubyIO, RubyIO> iFunction, boolean b, IFunction<RubyIO, RubyIO> ivar, int def) {
        dict = targetDictionary;
        targ = target;
        fieldA = iFunction;
        hash = b;
        iVar = ivar;
        defaultVal = def;
        // Cause a proxy to be generated. (NOTE: This *must* be referenced via nocache proxy!)
        AppMain.schemas.ensureSDBProxy(targetDictionary);
        kickMe = new IConsumer<SchemaPath>() {
            @Override
            public void accept(SchemaPath sp) {
                actNow = true;
            }
        };
    }

    public boolean actIfRequired(RubyIO map) {
        if (actNow) {
            actNow = false;
            // actually update
            HashMap<String, String> finalMap = new HashMap<String, String>();
            RubyIO target;
            String targetName;
            if (targ.equals("__MAP__")) {
                target = map;
                targetName = AppMain.objectDB.getIdByObject(map);
                if (targetName == null)
                    targetName = "__MAPANONOBJECT-ML-FAIL__";
            } else {
                target = AppMain.objectDB.getObject(targ);
                targetName = targ;
            }
            if (target != null) {
                boolean reregister = true;
                if (lastTarget != null) {
                    if (!lastTarget.equals(targetName)) {
                        AppMain.objectDB.deregisterModificationHandler(lastTarget, kickMe);
                    } else {
                        reregister = false;
                    }
                }
                if (reregister)
                    AppMain.objectDB.registerModificationHandler(targetName, kickMe);
                lastTarget = targetName;
                if (fieldA != null)
                    target = fieldA.apply(target);
                if (target == null)
                    return true; // :(
                if (hash) {
                    for (Map.Entry<RubyIO, RubyIO> rio : target.hashVal.entrySet())
                        handleVal(finalMap, rio.getValue(), rio.getKey());
                } else {
                    for (int i = 0; i < target.arrVal.length; i++) {
                        RubyIO rio = target.arrVal[i];
                        handleVal(finalMap, rio, new RubyIO().setFX(i));
                    }
                }
            } else {
                actNow = true;
            }
            finalizeVals(finalMap);
            return true;
        }
        return false;
    }

    private void finalizeVals(HashMap<String, String> finalMap) {
        // Default value of 1 because r2k. if this is ever in conflict then start adding a default parameter value for dictionaries.
        // Do proper dictionary unification at the same time.
        SchemaElement ise = new EnumSchemaElement(finalMap, Integer.toString(defaultVal), "INT:" + TXDB.get("ID."));
        AppMain.schemas.setSDBEntry(dict, ise);
    }

    private void handleVal(HashMap<String, String> finalMap, RubyIO rio, RubyIO k) {
        if (rio.type != '0') {
            String p = ValueSyntax.encode(k);
            if (p == null)
                return;
            if (iVar == null) {
                finalMap.put(p, rio.decString());
            } else {
                finalMap.put(p, iVar.apply(rio).decString());
            }
        }
    }

    @Override
    public void run() {
        actNow = true;
    }

    public void sanitize() {
        finalizeVals(new HashMap<String, String>());
    }
}
