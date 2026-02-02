/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48;

import r48.dbs.ObjectRootHandle;
import r48.dbs.SDB;
import r48.dbs.ValueSyntax;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.schema.EnumSchemaElement;
import r48.schema.OpaqueSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;
import r48.ui.dialog.UIEnumChoice;
import r48.ui.dialog.UIEnumChoice.EntryMode;

import java.util.Collections;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Used to build convenient dictionaries for selecting things.
 * Created on 1/3/17.
 */
public class DictionaryUpdaterRunnable extends R48.Svc implements SDB.DynamicSchemaUpdater {
    // act soon after init.
    private boolean actNow = true;
    public final SDB.DynamicSchemaElement dict;
    public final String targ;
    // Responsible for removing any wrapping
    // fieldA gets the root's wrapping, iVar gets the inner wrapping
    public final Function<IRIO, IRIO> fieldA, iVar;
    public final boolean hash;
    public final int defaultVal;
    public final String interpret;
    private String lastTarget = null;
    private Consumer<SchemaPath> kickMe;
    public final SchemaElement dataSchema;

    // NOTE: targetDictionary must always be referenced by proxy to ensure setSDBEntry works later.
    public DictionaryUpdaterRunnable(R48 app, SDB.DynamicSchemaElement targetDictionary, String target, Function<IRIO, IRIO> iFunction, boolean b, Function<IRIO, IRIO> ivar, int def, String ip, SchemaElement ds) {
        super(app);
        dict = targetDictionary;
        targ = target;
        fieldA = iFunction;
        hash = b;
        iVar = ivar;
        defaultVal = def;
        interpret = ip;
        kickMe = (path) -> actNow = true;
        dataSchema = ds;
    }

    public boolean actIfRequired(ObjectRootHandle map) {
        if (actNow) {
            actNow = false;

            // actually update
            LinkedList<UIEnumChoice.Option> finalMap = new LinkedList<UIEnumChoice.Option>();

            final ObjectRootHandle targetILO;
            String targetName;

            if (targ.equals("__MAP__")) {
                if (map != null) {
                    targetILO = map;
                    targetName = app.odb.getIdByObject(map);
                    if (targetName == null)
                        targetName = "__MAPANONOBJECT-ML-FAIL__";
                } else {
                    targetILO = null;
                    targetName = "__MAPFAIL__";
                }
            } else {
                targetILO = app.odb.getObject(targ);
                targetName = targ;
            }

            if (targetILO != null) {
                IRIO target = targetILO.getObject();

                boolean reregister = true;
                if (lastTarget != null) {
                    if (!lastTarget.equals(targetName)) {
                        app.odb.deregisterModificationHandler(lastTarget, kickMe);
                    } else {
                        reregister = false;
                    }
                }
                if (reregister)
                    app.odb.registerModificationHandler(targetName, kickMe);

                lastTarget = targetName;

                if (fieldA != null)
                    target = fieldA.apply(target);

                if (target == null)
                    return true; // :(

                try {
                    coreLogic(app, finalMap, iVar, targetILO, dataSchema, target, hash, interpret);
                } catch (Exception e) {
                    throw new RuntimeException("During DUR " + dict + " target " + targetName, e);
                }
            }
            finalizeVals(finalMap);
            return true;
        }
        return false;
    }

    public static void coreLogic(R48 app, LinkedList<UIEnumChoice.Option> finalMap, Function<IRIO, IRIO> innerMap, final @Nullable ObjectRootHandle targetILO, @Nullable SchemaElement dataSchema, IRIO target, boolean hash, String interpret) {
        if (hash) {
            for (DMKey key : target.getHashKeys())
                handleVal(app, finalMap, innerMap, targetILO, dataSchema, target.getHashVal(key), key, interpret);
        } else {
            int alen = target.getALen();
            for (int i = 0; i < alen; i++) {
                IRIO rio = target.getAElem(i);
                handleVal(app, finalMap, innerMap, targetILO, dataSchema, rio, DMKey.of(i), interpret);
            }
        }
    }

    private void finalizeVals(LinkedList<UIEnumChoice.Option> finalMap) {
        Collections.sort(finalMap, UIEnumChoice.COMPARATOR_OPTION);
        SchemaElement ise = new EnumSchemaElement(app, finalMap, DMKey.of(defaultVal), EntryMode.INT, () -> T.s.enum_id);
        dict.setEntry(ise);
    }

    private static void handleVal(R48 app, LinkedList<UIEnumChoice.Option> finalMap, Function<IRIO, IRIO> iVar, final @Nullable ObjectRootHandle targetILO, final @Nullable SchemaElement dataSchema, IRIO rio, DMKey k, String interpret) {
        int type = rio.getType();
        if (type != '0') {
            // Key details
            String p = ValueSyntax.encode(k);
            DMKey kc = ValueSyntax.decode(p);
            if (p == null)
                return;
            // Actual found name
            final IRIO mappedRIO = (iVar != null) ? iVar.apply(rio) : rio;
            // Data schema path
            final SchemaPath rootSchemaPath = targetILO == null ? null : new SchemaPath(new OpaqueSchemaElement(app), targetILO);
            final SchemaPath dataSchemaPath = ((rootSchemaPath == null) || (dataSchema == null)) ? null : rootSchemaPath.arrayHashIndex(kc, p).newWindow(dataSchema, rio);
            // Details
            String text;
            Consumer<String> editor = null;
            if (mappedRIO.getType() == '\"') {
                text = mappedRIO.decString();
                if (rootSchemaPath != null) {
                    editor = (t) -> {
                        mappedRIO.setString(t);
                        if (targetILO != null)
                            targetILO.objectRootModified(rootSchemaPath);
                    };
                }
            } else {
                text = app.format(rio, interpret, EnumSchemaElement.Prefix.NoPrefix);
            }
            finalMap.add(EnumSchemaElement.makeStandardOption(kc, () -> text, editor, dataSchemaPath));
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
