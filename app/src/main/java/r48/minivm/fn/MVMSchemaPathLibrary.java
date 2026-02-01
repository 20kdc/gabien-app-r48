/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import org.eclipse.jdt.annotation.Nullable;

import r48.App;
import r48.dbs.ObjectRootHandle;
import r48.io.data.DMPath;
import r48.io.data.IRIO;
import r48.minivm.MVMEnv;
import r48.minivm.harvester.Defun;
import r48.minivm.harvester.Help;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

/**
 * Schema Path library.
 * Created 19th December, 2025.
 */
public class MVMSchemaPathLibrary extends App.Svc {
    public MVMSchemaPathLibrary(App app) {
        super(app);
    }

    public void add(MVMEnv ctx) {
    }

    @Defun(n = "sp-new", r = 1)
    @Help("Create a new SchemaPath from either a root or a root and a DMPath. The root will be auto-created - use odb-get to explicitly avoid this. Null root passes through; failure-to-create throws.")
    public SchemaPath spNew(@Nullable Object root, @Nullable DMPath dmPath) {
        if (root == null)
            return null;
        ObjectRootHandle root2 = MVMDMAppLibrary.assertObjectRoot(app, root, true);
        if (root2 == null)
            throw new RuntimeException("Unable to create " + root);
        SchemaPath pathRoot = new SchemaPath(root2);
        if (dmPath != null)
            return pathRoot.tracePathRoute(dmPath);
        return pathRoot;
    }

    @Defun(n = "sp-parent", r = 1)
    @Help("Get the parent SchemaPath. Null passes through.")
    public SchemaPath spParent(@Nullable SchemaPath sp) {
        if (sp == null)
            return null;
        return sp.parent;
    }

    @Defun(n = "sp-editor", r = 1)
    @Help("Get the SchemaElement. sp-editor and sp-target are either both null or neither are. Null passes through.")
    public SchemaElement spEditor(@Nullable SchemaPath sp) {
        if (sp == null)
            return null;
        return SchemaElement.cast(sp.editor);
    }

    @Defun(n = "sp-target", r = 1)
    @Help("Get the target IRIO. sp-editor and sp-target are either both null or neither are. Null passes through.")
    public IRIO spTarget(@Nullable SchemaPath sp) {
        if (sp == null)
            return null;
        return sp.targetElement;
    }
}
