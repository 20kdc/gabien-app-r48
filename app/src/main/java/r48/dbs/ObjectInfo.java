/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import r48.App;
import r48.io.IObjectBackend;

/**
 * Created on 13th August, 2022.
 */
public class ObjectInfo extends App.Svc {
    /**
     * Object ID
     */
    public final String idName;
    /**
     * Object Schema
     */
    public final String schemaName;

    public ObjectInfo(App app, String iN, String sN) {
        super(app);
        idName = iN;
        schemaName = sN;
    }

    public String toString() {
        return idName;
    }

    public IObjectBackend.ILoadedObject getILO(boolean create) {
        return app.odb.getObject(idName, create ? schemaName : null);
    }
}