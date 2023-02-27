/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48;

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
import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Used to build convenient dictionaries for selecting things.
 * Created on 1/3/17.
 */
public class DictionaryUpdaterRunnable extends App.Svc implements Runnable {
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
    public final SchemaElement dataSchema;

    // NOTE: targetDictionary must always be referenced by proxy to ensure setSDBEntry works later.
    public DictionaryUpdaterRunnable(App app, String targetDictionary, String target, IFunction<IRIO, IRIO> iFunction, boolean b, IFunction<IRIO, IRIO> ivar, int def, String ip, SchemaElement ds) {
        super(app);
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
        dataSchema = ds;
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
                    coreLogic(app, finalMap, iVar, targetILO, dataSchema, target, hash, interpret);
                } catch (Exception e) {
                    throw new RuntimeException("During DUR " + dict, e);
                }
            }
            finalizeVals(finalMap);
            return true;
        }
        return false;
    }

    public static void coreLogic(App app, LinkedList<UIEnumChoice.Option> finalMap, IFunction<IRIO, IRIO> innerMap, final @Nullable IObjectBackend.ILoadedObject targetILO, @Nullable SchemaElement dataSchema, IRIO target, boolean hash, String interpret) {
        if (hash) {
            for (IRIO key : target.getHashKeys())
                handleVal(app, finalMap, innerMap, targetILO, dataSchema, target.getHashVal(key), key, interpret);
        } else {
            int alen = target.getALen();
            for (int i = 0; i < alen; i++) {
                IRIO rio = target.getAElem(i);
                handleVal(app, finalMap, innerMap, targetILO, dataSchema, rio, new RubyIO().setFX(i), interpret);
            }
        }
    }

    private void finalizeVals(LinkedList<UIEnumChoice.Option> finalMap) {
        Collections.sort(finalMap, UIEnumChoice.COMPARATOR_OPTION);
        SchemaElement ise = new EnumSchemaElement(app, finalMap, new RubyIO().setFX(defaultVal), EntryMode.INT, TXDB.get("ID."));
        AppMain.schemas.setSDBEntry(dict, ise);
    }

    private static void handleVal(App app, LinkedList<UIEnumChoice.Option> finalMap, IFunction<IRIO, IRIO> iVar, final @Nullable IObjectBackend.ILoadedObject targetILO, final @Nullable SchemaElement dataSchema, IRIO rio, IRIO k, String interpret) {
        int type = rio.getType();
        if (type != '0') {
            // Key details
            String p = ValueSyntax.encode(k);
            RubyIO kc = ValueSyntax.decode(p);
            if (p == null)
                return;
            // Actual found name
            final IRIO mappedRIO = (iVar != null) ? iVar.apply(rio) : rio;
            // Data schema path
            final SchemaPath rootSchemaPath = targetILO == null ? null : new SchemaPath(new OpaqueSchemaElement(app), targetILO);
            final SchemaPath dataSchemaPath = ((rootSchemaPath == null) || (dataSchema == null)) ? null : rootSchemaPath.arrayHashIndex(kc, p).newWindow(dataSchema, rio);
            // Details
            String text;
            IConsumer<String> editor = null;
            if (mappedRIO.getType() == '\"') {
                text = mappedRIO.decString();
                if (rootSchemaPath != null) {
                    editor = new IConsumer<String>() {
                        @Override
                        public void accept(String t) {
                            mappedRIO.setString(t);
                            AppMain.objectDB.objectRootModified(targetILO, rootSchemaPath);
                        }
                    };
                }
            } else {
                text = FormatSyntax.interpretParameter(rio, interpret, false);
            }
            finalMap.add(EnumSchemaElement.makeStandardOption(kc, text, editor, dataSchemaPath));
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
