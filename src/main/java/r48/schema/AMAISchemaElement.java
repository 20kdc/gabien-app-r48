/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import r48.dbs.TXDB;
import r48.io.data.IRIO;
import r48.schema.util.SchemaPath;

/**
 * Always Matches Array Index
 * (@id on Events)
 * Created on 12/29/16.
 */
public class AMAISchemaElement extends OpaqueSchemaElement {

    @Override
    public String getMessage(IRIO v) {
        return TXDB.get("Parent Index. ") + v;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        if (checkType(target, 'i', null, setDefault || !IRIO.rubyEquals(target, path.lastArrayIndex))) {
            // always must be set to this
            target.setDeepClone(path.lastArrayIndex);
            path.changeOccurred(true);
        }
    }
}
