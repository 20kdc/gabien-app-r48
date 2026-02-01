/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.LinkedList;
import java.util.List;

import datum.DatumSrcLoc;
import r48.App;
import r48.dbs.SDBOldParser;
import r48.gameinfo.ATDB;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMType;
import r48.minivm.MVMU;
import r48.minivm.harvester.Defun;
import r48.minivm.harvester.Help;
import r48.schema.SchemaElement;
import r48.search.CommandTag;
import r48.toolsets.utils.IDChangerEntry;
import r48.tr.TrNames;
import r48.tr.TrPage.FF0;

/**
 * MiniVM standard library.
 * Created 10th March 2023.
 */
public class MVMSDBLibrary extends App.Svc {
    public MVMSDBLibrary(App app) {
        super(app);
    }

    public void add(MVMEnv ctx) {
        ctx.defLib("sdb-load-old", MVMType.ANY, MVMType.STR, (a0) -> {
            SDBOldParser.readFile(app, (String) a0);
            return null;
        }, "(sdb-load-old FILE) : Read old-format SDB file.");

        ctx.defLib("cmdb-add-tag", MVMType.ANY, MVMType.ANY, MVMType.ANY, (a0, a1) -> {
            String id = MVMU.coerceToString(a0);
            FF0 ax = app.dTr(DatumSrcLoc.NONE, TrNames.cmdbCommandTag(id), MVMU.coerceToString(a1));
            CommandTag tag = new CommandTag(id, ax);
            app.commandTags.put(id, tag);
            app.cmdClassifiers.add(tag);
            return null;
        }, "(cmdb-add-tag ID NAME) : Adds a command tag.");

        ctx.defLib("cmdb-init", MVMType.ANY, MVMType.ANY, (a0) -> {
            app.cmdbs.newCMDB(MVMU.coerceToString(a0));
            return null;
        }, "(cmdb-init ID) : Setup a CMDB.");
        ctx.defLib("cmdb-load-old", MVMType.ANY, MVMType.ANY, MVMType.STR, (a0, a1) -> {
            app.cmdbs.loadCMDB(MVMU.coerceToString(a0), (String) a1);
            return null;
        }, "(cmdb-load-old ID FILE) : Read old-format CMDB file.");

        ctx.defLib("sdb-get", MVMEnvR48.SCHEMAELEMENT_TYPE, MVMType.ANY, (a0) -> {
            return app.sdb.getSDBEntry(MVMU.coerceToString(a0));
        }, "(sdb-get ID) : Gets a SchemaElement from SDB.");

        ctx.defLib("sdb-set", MVMType.ANY, MVMType.ANY, MVMEnvR48.SCHEMAELEMENT_TYPE, (a0, a1) -> {
            app.sdb.setSDBEntry(MVMU.coerceToString(a0), (SchemaElement) a1);
            return null;
        }, "(sdb-set ID SE) : Puts a SchemaElement into SDB.");

        ctx.defLib("idchanger-add", MVMType.ANY, MVMType.ANY, MVMType.ANY, (a0, a1) -> {
            List<Object> lo = MVMU.cList(a1);
            LinkedList<SchemaElement> potential = new LinkedList<>();
            for (Object o : lo)
                potential.add(app.sdb.getSDBEntry(MVMU.coerceToString(o)));
            app.idc.add(new IDChangerEntry((FF0) a0, potential.toArray(new SchemaElement[0])));
            return null;
        }, "(idchanger-add TITLE SCHEMAIDS) : Adds an ID changer to the RMTools ID changer menu.");
    }


    @Defun(n = "atdb-load", r = 1)
    @Help("Loads an ATDB. A0: Primary rule file A1: Inverse rule file (or \"$WallATs$\")")
    public ATDB loadATDB(String filename, String inverseRules) {
        ATDB atdb = new ATDB(app.loadProgress, filename);
        if (inverseRules != null)
            atdb.calculateInverseMap(app.loadProgress, inverseRules);
        return atdb;
    }

    @Defun(n = "atdb-bind", r = 1)
    @Help("Binds ATDBs.")
    public void loadATDB(List<ATDB> atdbs) {
        app.autoTiles = atdbs.toArray(new ATDB[0]);
    }

    public static SchemaElement coerceToElement(App app, Object elm) {
        if (elm instanceof SchemaElement)
            return (SchemaElement) elm;
        return app.sdb.getSDBEntry(MVMU.coerceToString(elm));
    }
}
