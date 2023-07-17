/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.LinkedList;
import java.util.List;

import r48.App;
import r48.dbs.SDBOldParser;
import r48.minivm.MVMEnv;
import r48.minivm.MVMU;
import r48.schema.SchemaElement;
import r48.toolsets.utils.IDChangerEntry;
import r48.tr.TrPage.FF0;

/**
 * MiniVM standard library.
 * Created 10th March 2023.
 */
public class MVMSDBLibrary {
    public static void add(MVMEnv ctx, App app) {
        ctx.defLib("sdb-load-old", (a0) -> {
            SDBOldParser.readFile(app, (String) a0);
            return null;
        }).attachHelp("(sdb-load-old FILE) : Read old-format SDB file.");

        ctx.defLib("cmdb-init", (a0) -> {
            app.sdb.newCMDB(MVMU.coerceToString(a0));
            return null;
        }).attachHelp("(cmdb-init ID) : Setup a CMDB.");
        ctx.defLib("cmdb-load-old", (a0, a1) -> {
            app.sdb.loadCMDB(MVMU.coerceToString(a0), (String) a1);
            return null;
        }).attachHelp("(cmdb-load-old ID FILE) : Read old-format CMDB file.");

        ctx.defLib("sdb-get", (a0) -> {
            return app.sdb.getSDBEntry(MVMU.coerceToString(a0));
        }).attachHelp("(sdb-get ID) : Gets a SchemaElement from SDB.");

        ctx.defLib("sdb-set", (a0, a1) -> {
            app.sdb.setSDBEntry(MVMU.coerceToString(a0), (SchemaElement) a1);
            return null;
        }).attachHelp("(sdb-set ID SE) : Puts a SchemaElement into SDB.");

        ctx.defLib("idchanger-add", (a0, a1) -> {
            List<Object> lo = MVMU.cList(a1);
            LinkedList<SchemaElement> potential = new LinkedList<>();
            for (Object o : lo)
                potential.add(app.sdb.getSDBEntry(MVMU.coerceToString(o)));
            app.idc.add(new IDChangerEntry((FF0) a0, potential.toArray(new SchemaElement[0])));
            return null;
        }).attachHelp("(idchanger-add TITLE SCHEMAIDS) : Adds an ID changer to the RMTools ID changer menu.");
    }
}
