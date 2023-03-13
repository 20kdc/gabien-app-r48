/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import java.io.IOException;

import gabien.datum.DatumSrcLoc;

/**
 * Called upon by DBLoader to actually implement the database.
 * Created on 12/30/16.
 */
public interface IDatabase {
    void newObj(int objId, String objName) throws IOException;

    default void updateSrcLoc(DatumSrcLoc sl) {
        
    }

    void execCmd(char c, String[] args) throws IOException;
}
