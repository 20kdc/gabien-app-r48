/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import r48.App;
import r48.dbs.ObjectRootHandle;
import r48.io.data.DMPath;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMType;
import r48.minivm.MVMU;
import r48.minivm.harvester.Defun;
import r48.minivm.harvester.Help;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.schema.util.UISchemaHostWindow;

/**
 * The scope creep begins here.
 * Created 21st August 2024, but only just.
 */
public class MVMAppUILibrary extends App.Svc {
    public MVMAppUILibrary(App app) {
        super(app);
    }

    public void add(MVMEnv ctx) {
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

        // (define my-root (root-new "int" "Example")) (ui-view my-root dp-empty)
        ctx.defLib("ui-view", MVMType.ANY, MVMType.ANY, MVMEnvR48.DMPATH_TYPE, (text, path) -> {
            ObjectRootHandle ilo = MVMDMAppLibrary.assertObjectRoot(app, text, true);
            if (ilo == null)
                throw new RuntimeException("Unable to create " + text);
            return app.ui.launchSchemaTrace(ilo, null, (DMPath) path);
        }, "(ui-view OID PATH): Opens a view to the given path of the given object (as if via odb-get). R48 will 'auto-route' to make this path work. Object will be created if necessary. Returned value is the schema host handle (target may not exactly match what you wanted!).");
    }

    @Defun(n = "ui-view-sp", r = 1)
    @Help("Opens a view to a schema path. Returned value is the schema host handle. Nulls pass through.")
    public ISchemaHost uiViewSP(SchemaPath schemaPath) {
        UISchemaHostWindow watcher = new UISchemaHostWindow(app, null);
        watcher.pushPathTree(schemaPath);
        return watcher;
    }
}
