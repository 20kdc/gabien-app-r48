/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.dbs;

import r48.AppMain;
import r48.schema.SchemaElement;
import r48.schema.specialized.cmgb.EventCommandArraySchemaElement;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on April 19, 2019.
 */
public class TestDBUtils {
    public static Set<EventCommandArraySchemaElement> getLoadedCSLs() {
        HashSet<EventCommandArraySchemaElement> ecase = new HashSet<EventCommandArraySchemaElement>();
        for (SchemaElement se : AppMain.schemas.schemaTrueDatabase.values())
            if (se instanceof EventCommandArraySchemaElement)
                ecase.add((EventCommandArraySchemaElement) se);
        return ecase;
    }
}
