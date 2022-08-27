/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import gabien.uslx.append.*;
import gabien.uslx.append.*;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.dbs.ValueSyntax;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.schema.EnumSchemaElement;
import r48.schema.OpaqueSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;
import r48.ui.dialog.UIEnumChoice;
import r48.ui.dialog.UIEnumChoice.EntryMode;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

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
            LinkedList<UIEnumChoice.Option> finalMap = new LinkedList<UIEnumChoice.Option>();

            final IObjectBackend.ILoadedObject targetILO;
            String targetName;

            if (targ.equals("__MAP__")) {
                if (map != null) {
                    targetILO = map;
                    targetName = AppMain.objectDB.getIdByObject(map);
                    if (targetName == null)
                        targetName = "__MAPANONOBJECT-ML-FAIL__";
                } else {
                    targetILO = null;
                    targetName = "__MAPFAIL__";
                }
            } else {
                targetILO = AppMain.objectDB.getObject(targ);
                targetName = targ;
            }

            if (targetILO != null) {
                IRIO target = targetILO.getObject();

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
                    coreLogic(finalMap, iVar, new Runnable() {
                        @Override
                        public void run() {
                            AppMain.objectDB.objectRootModified(targetILO, new SchemaPath(new OpaqueSchemaElement(), targetILO));
                        }
                    }, target, hash, interpret);
                } catch (Exception e) {
                    throw new RuntimeException("During DUR " + dict, e);
                }
            }
            finalizeVals(finalMap);
            return true;
        }
        return false;
    }

    public static void coreLogic(LinkedList<UIEnumChoice.Option> finalMap, IFunction<IRIO, IRIO> innerMap, @Nullable Runnable editMade, IRIO target, boolean hash, String interpret) {
        if (hash) {
            for (IRIO key : target.getHashKeys())
                handleVal(finalMap, innerMap, editMade, target.getHashVal(key), key, interpret);
        } else {
            int alen = target.getALen();
            for (int i = 0; i < alen; i++) {
                IRIO rio = target.getAElem(i);
                handleVal(finalMap, innerMap, editMade, rio, new RubyIO().setFX(i), interpret);
            }
        }
    }

    private void finalizeVals(LinkedList<UIEnumChoice.Option> finalMap) {
        Collections.sort(finalMap, UIEnumChoice.COMPARATOR_OPTION);
        SchemaElement ise = new EnumSchemaElement(finalMap, new RubyIO().setFX(defaultVal), EntryMode.INT, TXDB.get("ID."));
        AppMain.schemas.setSDBEntry(dict, ise);
    }

    private static void handleVal(LinkedList<UIEnumChoice.Option> finalMap, IFunction<IRIO, IRIO> iVar, final @Nullable Runnable editMade, IRIO rio, IRIO k, String interpret) {
        int type = rio.getType();
        if (type != '0') {
            String p = ValueSyntax.encode(k);
            RubyIO kc = ValueSyntax.decode(p);
            if (p == null)
                return;
            final IRIO mappedRIO = (iVar != null) ? iVar.apply(rio) : rio;
            String text;
            IConsumer<String> editor = null;
            if (mappedRIO.getType() == '\"') {
                text = mappedRIO.decString();
                if (editMade != null) {
                    editor = new IConsumer<String>() {
                        @Override
                        public void accept(String t) {
                            mappedRIO.setString(t);
                            editMade.run();
                        }
                    };
                }
            } else {
                text = FormatSyntax.interpretParameter(rio, interpret, false);
            }
            finalMap.add(EnumSchemaElement.makeStandardOption(kc, text, editor));
        }
    }

    @Override
    public void run() {
        actNow = true;
    }

    public void sanitize() {
        finalizeVals(new LinkedList<UIEnumChoice.Option>());
    }
}
