/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.schema.op;

import java.util.function.Consumer;
import java.util.function.Function;

import datum.DatumSymbol;
import r48.App;
import r48.UITest;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.schema.util.SchemaPath;
import r48.toolsets.utils.UIIDChanger;
import r48.tr.TrPage.FF0;

/**
 * Created 19th December, 2025.
 */
public class BaseSchemaOps {
    /**
     * Save/Inspect etc.
     */
    public static final int SORT_SYSCORE = 0x00000000;
    /**
     * LIDC etc.
     */
    public static final int SORT_VISITOR = 0x01000000;
    /**
     * Text tools and such.
     */
    public static final int SORT_RMTOOLS = 0x02000000;

    public static final DatumSymbol BASE_SYSCORE = new DatumSymbol("r48core_");

    // See ISchemaHost.supplyOperatorContext
    /**
     * Parameter from UI: if array selection is non-empty, start index. 
     */
    public static final String CTXPARAM_ARRAYSTART = "@ctx_array_start";
    /**
     * Parameter from UI: if array selection is non-empty, end (exclusive) index.
     */
    public static final String CTXPARAM_ARRAYEND = "@ctx_array_end";

    public static int sortIdx(int part, int index) {
        return part + (index * 0x10000);
    }

    /**
     * Helps with defining the system core operators.
     */
    private static SchemaOp sysOperator(App app, String id, FF0 name, Consumer<SchemaPath> handler, int idx, SchemaOpSite... sites) {
        return new SchemaOp(app, BASE_SYSCORE, id, sortIdx(SORT_SYSCORE, idx), sites) {
            @Override
            public String shouldDisplay(SchemaOp.ExpandedCtx context) {
                return name.r();
            }
            @Override
            public String invoke(SchemaOp.ExpandedCtx parameters) {
                SchemaPath path = parameters.path;
                handler.accept(path);
                return null;
            }
        };
    }

    /**
     * Makes parameters a bit easier to handle.
     */
    public static RORIO getParamOrDMNull(Function<String, DMKey> parameters, String iv) {
        RORIO res = parameters.apply(iv);
        if (res == null)
            return DMKey.NULL;
        return res;
    }

    public static long getParamLong(Function<String, DMKey> parameters, String iv, long def) {
        RORIO res = parameters.apply(iv);
        if (res == null || res.getType() != 'i')
            return def;
        return res.getFX();
    }

    /**
     * This is the central list of all built-in (Java-side) operators.
     */
    public static void defJavasideOperators(App app) {
        int catIdx = 0;
        app.opCopy = new SchemaOp(app, BASE_SYSCORE, "copy", sortIdx(SORT_SYSCORE, catIdx++), app.opSites.ARRAY_SEL) {
            @Override
            public String shouldDisplay(SchemaOp.ExpandedCtx context) {
                return app.t.g.bCopy;
            }
            @Override
            public String invoke(SchemaOp.ExpandedCtx parameters) {
                SchemaPath path = parameters.path;
                RORIO arrayStartK = getParamOrDMNull(parameters, CTXPARAM_ARRAYSTART);
                RORIO arrayEndK = getParamOrDMNull(parameters, CTXPARAM_ARRAYEND);
                if (path.targetElement.getType() == '[' && arrayStartK.getType() == 'i' && arrayEndK.getType() == 'i') {
                    // the clipboard is very lenient...
                    IRIOGeneric rio = new IRIOGeneric(app.ctxClipboardAppEncoding);
                    rio.setArray();
                    int arrayStart = (int) arrayStartK.getFX();
                    int arrayEnd = (int) arrayEndK.getFX();
                    for (int i = arrayStart; i < arrayEnd; i++)
                        rio.addAElem(rio.getALen()).setDeepClone(path.targetElement.getAElem(i));
                    app.theClipboard = rio;
                } else {
                    app.setClipboardFrom(path.targetElement);
                }
                return null;
            }
        };
        app.opPaste = new SchemaOp(app, BASE_SYSCORE, "paste", sortIdx(SORT_SYSCORE, catIdx++), app.opSites.ARRAY_SEL) {
            @Override
            public String shouldDisplay(SchemaOp.ExpandedCtx parameters) {
                SchemaPath path = parameters.path;
                if (app.theClipboard == null)
                    return null;

                RORIO arrayStartK = getParamOrDMNull(parameters, CTXPARAM_ARRAYSTART);
                RORIO arrayEndK = getParamOrDMNull(parameters, CTXPARAM_ARRAYEND);
                if (app.theClipboard.getType() == '[' && path.targetElement.getType() == '[' && arrayStartK.getType() == 'i' && arrayEndK.getType() == 'i')
                    return app.t.s.op_pasteOverwrite;
                return app.t.g.bPaste;
            }
            @Override
            public String invoke(SchemaOp.ExpandedCtx parameters) {
                SchemaPath path = parameters.path;
                if (app.theClipboard == null)
                    return T.u.shcEmpty;

                RORIO arrayStartK = getParamOrDMNull(parameters, CTXPARAM_ARRAYSTART);
                RORIO arrayEndK = getParamOrDMNull(parameters, CTXPARAM_ARRAYEND);
                if (app.theClipboard.getType() == '[' && path.targetElement.getType() == '[' && arrayStartK.getType() == 'i' && arrayEndK.getType() == 'i') {
                    int arrayStart = (int) arrayStartK.getFX();
                    int arrayEnd = (int) arrayEndK.getFX();
                    int targLen = app.theClipboard.getALen();
                    // This is horrible no-good fudge that will backfire at some point, but what can be done?
                    // With commands being groups inside arrays, we need to get a little clever with selections.
                    while (true) {
                        int resLen = arrayEnd - arrayStart;
                        if (resLen > targLen) {
                            path.targetElement.rmAElem(arrayEnd - 1);
                            arrayEnd--;
                        } else if (resLen < targLen) {
                            path.targetElement.addAElem(arrayEnd);
                            arrayEnd++;
                        } else {
                            break;
                        }
                    }
                    for (int i = arrayStart; i < arrayEnd; i++)
                        path.targetElement.getAElem(i).setDeepClone(app.theClipboard.getAElem(i - arrayStart));
                    path.changeOccurred(false);
                } else {
                    if (IRIO.rubyTypeEquals(path.targetElement, app.theClipboard)) {
                        try {
                            path.targetElement.setDeepClone(app.theClipboard);
                        } catch (Exception e) {
                            app.ui.launchDialog(T.u.shcIncompatible, e);
                        }
                        path.changeOccurred(false);
                    } else {
                        return T.u.shcIncompatible;
                    }
                }
                return null;
            }
        };
        sysOperator(app, "save", () -> app.t.g.wordSave, (innerElem) -> {
            SchemaPath root = innerElem.findRoot();
            // perform a final verification of the file, just in case? (NOPE: Causes long save times on, say, LDBs)
            // root.editor.modifyVal(root.targetElement, root, false);
            root.root.ensureSaved();
        }, catIdx++, app.opSites.SCHEMA_HEADER);
        sysOperator(app, "inspect", () -> app.t.u.shInspect, (innerElem) -> {
            app.ui.wm.createWindow(new UITest(app, innerElem.targetElement, innerElem.root));
        }, catIdx++, app.opSites.SCHEMA_HEADER);
        sysOperator(app, "localidchanger", () -> app.t.u.shLIDC, (innerElem) -> {
            // innerElem.editor and innerElem.targetElement must exist because SchemaHostImpl uses them.
            app.ui.wm.createWindow(new UIIDChanger(app, innerElem));
        }, catIdx++, app.opSites.SCHEMA_HEADER);

        new SchemaOp(app, BASE_SYSCORE, "test_operator", sortIdx(SORT_SYSCORE, catIdx++), app.opSites.SCHEMA_HEADER, app.opSites.ARRAY_SEL) {
            @Override
            public String shouldDisplay(SchemaOp.ExpandedCtx context) {
                return "TEST OPERATOR";
            }
            @Override
            public String invoke(SchemaOp.ExpandedCtx parameters) {
                return "TEST OPERATOR PLEASE IGNORE";
            }
        };

        // other command sets
        RMToolsSchemaOps.defJavasideOperators(app);
    }

}
