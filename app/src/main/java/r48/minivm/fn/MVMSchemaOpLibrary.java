/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.HashMap;

import r48.R48;
import r48.dbs.ObjectRootHandle;
import r48.io.data.DMKey;
import r48.minivm.MVMEnv;
import r48.minivm.harvester.Defun;
import r48.minivm.harvester.Help;
import r48.schema.op.SchemaOp;
import r48.schema.util.SchemaPath;

/**
 * Schema Op library.
 * Created 20th December, 2025.
 */
public class MVMSchemaOpLibrary extends R48.Svc {
    public MVMSchemaOpLibrary(R48 app) {
        super(app);
    }

    public void add(MVMEnv ctx) {
    }

    @Defun(n = "sop-init-params-root", r = 1)
    @Help("Initializes a Schema Operator's parameters root. This may be passed to the 'root-' function series.")
    public ObjectRootHandle sopInitParamsRoot(Object op) {
        SchemaOp operator = app.operators.coerce(op);
        return operator.createOperatorConfig(new HashMap<String, DMKey>());
    }

    @Defun(n = "sop-invoke", r = 3)
    @Help("Invokes the given Schema Operator on the given path with the given parameters root. Results are reported via the returned string.")
    public String sopInvoke(Object op, SchemaPath path, ObjectRootHandle parameters) {
        SchemaOp operator = app.operators.coerce(op);
        return operator.invoke(new SchemaOp.ExpandedCtx(path, parameters.getObject(), app, app.uiAttachmentPoint));
    }
}
