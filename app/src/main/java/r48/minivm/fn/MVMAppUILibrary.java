/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import r48.App;
import r48.dbs.ObjectInfo;
import r48.dbs.ObjectRootHandle;
import r48.dbs.PathSyntax;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMType;
import r48.minivm.MVMU;

/**
 * The scope creep begins here.
 * Created 21st August 2024, but only just.
 */
public class MVMAppUILibrary {
    public static void add(MVMEnv ctx, App app) {
        ctx.defLib("ui-prompt", MVMType.ANY, MVMType.ANY, MVMType.Fn.simple(MVMType.ANY, MVMType.STR), (text, handler) -> {
            MVMFn function = (MVMFn) handler;
            app.ui.launchPrompt(MVMU.coerceToString(text), (res) -> {
                function.callDirect(res);
            });
            return null;
        }, "(ui-prompt TEXT HANDLER): Displays a prompt to the user. If the dialog is simply closed, #nil is passed, otherwise the text is passed.");

        ctx.defLib("ui-message", MVMType.ANY, MVMType.ANY, (text) -> {
            app.ui.launchDialog(MVMU.coerceToString(text));
            return text;
        }, "(ui-message VALUE): Displays a message to the user, returns the value you passed in.");

        ctx.defLib("ui-view", MVMType.ANY, MVMType.STR, MVMEnvR48.PATHSYNTAX_TYPE, (text, path) -> {
            ObjectInfo oi = MVMDMAppLibrary.assertObjectInfo(app, text);
            ObjectRootHandle ilo = oi.getILO(true);
            if (ilo == null)
                throw new RuntimeException("Unable to create " + text);
            return app.ui.launchSchemaTrace(ilo, null, (PathSyntax) path);
        }, "(ui-view OID PATH): Opens a view to the given path of the given object. R48 will 'auto-route' to make this path work. The object must be in the objects info table or an error occurs. Object will be created if necessary. Returned value is the schema host handle (target may not exactly match what you wanted!).");
    }
}
