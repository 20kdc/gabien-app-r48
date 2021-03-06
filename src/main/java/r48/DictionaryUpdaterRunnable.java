/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import gabien.ui.IConsumer;
import gabien.ui.IFunction;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.dbs.ValueSyntax;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

import java.util.HashMap;

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
    public final IFunction<IRIO, IRIO> fieldA, iVar;
    public final boolean hash;
    public final int defaultVal;
    public final String interpret;
    private String lastTarget = null;
    private IConsumer<SchemaPath> kickMe;

    // NOTE: targetDictionary must always be referenced by proxy to ensure setSDBEntry works later.
    public DictionaryUpdaterRunnable(String targetDictionary, String target, IFunction<IRIO, IRIO> iFunction, boolean b, IFunction<IRIO, IRIO> ivar, int def, String ip) {
        dict = targetDictionary;
        targ = target;
        fieldA = iFunction;
        hash = b;
        iVar = ivar;
        defaultVal = def;
        interpret = ip;
        kickMe = new IConsumer<SchemaPath>() {
            @Override
            public void accept(SchemaPath sp) {
                actNow = true;
            }
        };
    }

    public boolean actIfRequired(IObjectBackend.ILoadedObject map) {
        if (actNow) {
            actNow = false;
            // actually update
            HashMap<String, String> finalMap = new HashMap<String, String>();
            IRIO target;
            String targetName;
            if (targ.equals("__MAP__")) {
                if (map != null) {
                    target = map.getObject();
                    targetName = AppMain.objectDB.getIdByObject(map);
                    if (targetName == null)
                        targetName = "__MAPANONOBJECT-ML-FAIL__";
                } else {
                    target = null;
                    targetName = "__MAPFAIL__";
                }
            } else {
                IObjectBackend.ILoadedObject ilo = AppMain.objectDB.getObject(targ);
                if (ilo != null) {
                    target = ilo.getObject();
                } else {
                    target = null;
                }
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
                try {
                    coreLogic(finalMap, iVar, target, hash, interpret);
                } catch (Exception e) {
                    throw new RuntimeException("During DUR " + dict, e);
                }
            }
            finalizeVals(finalMap);
            return true;
        }
        return false;
    }

    public static void coreLogic(HashMap<String, String> finalMap, IFunction<IRIO, IRIO> innerMap, IRIO target, boolean hash, String interpret) {
        if (hash) {
            for (IRIO key : target.getHashKeys())
                handleVal(finalMap, innerMap, target.getHashVal(key), key, interpret);
        } else {
            int alen = target.getALen();
            for (int i = 0; i < alen; i++) {
                IRIO rio = target.getAElem(i);
                handleVal(finalMap, innerMap, rio, new RubyIO().setFX(i), interpret);
            }
        }
    }

    private void finalizeVals(HashMap<String, String> finalMap) {
        SchemaElement ise = new EnumSchemaElement(finalMap, new RubyIO().setFX(defaultVal), "INT:" + TXDB.get("ID."));
        AppMain.schemas.setSDBEntry(dict, ise);
    }

    private static void handleVal(HashMap<String, String> finalMap, IFunction<IRIO, IRIO> iVar, IRIO rio, IRIO k, String interpret) {
        int type = rio.getType();
        if (type != '0') {
            String p = ValueSyntax.encode(k);
            if (p == null)
                return;
            if (iVar != null)
                rio = iVar.apply(rio);
            if (type == '\"') {
                finalMap.put(p, rio.decString());
            } else {
                finalMap.put(p, FormatSyntax.interpretParameter(rio, interpret, false));
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
