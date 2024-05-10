/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import r48.app.AppCore;
import r48.io.IObjectBackend;
import r48.io.IObjectBackend.ILoadedObject;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

/**
 * Created on 13th August, 2022.
 */
public class ObjectInfo extends AppCore.Csv {
    /**
     * Object ID
     */
    public final @NonNull String idName;
    /**
     * Object Schema
     */
    public final @NonNull String schemaName;

    /**
     * Object Schema
     */
    public final @Nullable SchemaElement schema;

    public ObjectInfo(@NonNull AppCore app, @NonNull String iN, @NonNull String sN) {
        super(app);
        idName = iN;
        schemaName = sN;
        schema = app.sdb.hasSDBEntry(sN) ? app.sdb.getSDBEntry(sN) : null;
    }

    /**
     * Localized name. Beware: This gets overridden with dynamically (data-dependent!) changing variants.
     * So if refreshing seems absurd: It isn't.
     */
    @Override
    public String toString() {
        return idName;
    }

    public @Nullable IObjectBackend.ILoadedObject getILO(boolean create) {
        return app.odb.getObject(idName, create ? schemaName : null);
    }

    public @Nullable SchemaPath makePath(boolean create) {
        SchemaElement se = schema;
        ILoadedObject ilo = getILO(create);
        if (se == null || ilo == null)
            return null;
        return new SchemaPath(se, ilo);
    }
}