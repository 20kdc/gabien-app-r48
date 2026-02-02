/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.toolsets.utils;

import java.util.HashMap;
import java.util.Map;

import r48.R48;
import r48.dbs.CMDB;
import r48.dbs.RPGCommand;
import r48.dbs.RPGCommand.Param;
import r48.dbs.SDB;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.ArrayElementSchemaElement;
import r48.schema.DisambiguatorSchemaElement;
import r48.schema.EnumSchemaElement;
import r48.schema.PathSchemaElement;
import r48.schema.SchemaElement;
import r48.tr.TrPage.FF0;
import r48.ui.dialog.UIEnumChoice;

/**
 * Hopefully should help.
 * Created 4th September 2023.
 */
public class LibLCF245Dumper extends R48.Svc {
    public final IRIO root;
    public final IRIO sdbNodes;
    public final HashMap<SchemaElement, Integer> seGraph = new HashMap<>();
    public LibLCF245Dumper(R48 app) {
        super(app);
        root = new IRIOGeneric(app.ilg.adhocIOContext).setHash();
        sdbNodes = root.addHashVal(DMKey.ofStr("sdbNodes")).setArray();
    }

    private static IRIO putProp(IRIO root, String string) {
        return root.addHashVal(DMKey.ofStr(string));
    }

    public RORIO dumpRoot() {
        putProp(root, "type").setString("dump file for liblcf#245, format version 1");
        dumpSDB(putProp(root, "sdbID"));
        dumpCMDBs(putProp(root, "cmdbs"));
        return root;
    }

    private void dumpSDB(IRIO sdbID) {
        sdbID.setHash();
        for (String id : app.sdb.getAllSDBEntryIDs()) {
            SchemaElement se = app.sdb.getSDBEntry(id);
            sdbID.addHashVal(DMKey.ofStr(id)).setFX(dumpSDBNode(se));
        }
    }
    private void dumpCMDBs(IRIO cmdbs) {
        cmdbs.setHash();
        for (String id : app.cmdbs.getAllCMDBIDs()) {
            CMDB cmdb = app.cmdbs.getCMDB(id);
            dumpCMDB(cmdb, putProp(cmdbs, id));
        }
    }
    private void dumpCMDB(CMDB cmdb, IRIO target) {
        target.setHash();
        // categories
        IRIO categories = putProp(target, "categories").setArray();
        for (FF0 ff0 : cmdb.categories)
            categories.appendAElem().setString(ff0.r());
        // command order
        IRIO knownCommands = putProp(target, "knownCommands").setArray();
        for (Integer i : cmdb.knownCommandOrder) {
            dumpCMDBEntry(cmdb.knownCommands.get(i), knownCommands.appendAElem());
        }
    }

    private void dumpCMDBEntry(RPGCommand rc, IRIO target) {
        target.setHash();
        // metadata
        putProp(target, "commandId").setFX(rc.commandId);
        putProp(target, "srcLoc").setString(rc.srcLoc.toString());
        putProp(target, "name").setString(rc.formatName(null));
        putProp(target, "nameRawUnlocalized").setString(rc.nameRawUnlocalized.toString());
        if (rc.description == null) {
            putProp(target, "description").setNull();
        } else {
            putProp(target, "description").setString(rc.description.r());
        }
        if (rc.specialSchema != null) {
            putProp(target, "specialSchema").setFX(dumpSDBNode(rc.specialSchema));
        } else {
            putProp(target, "specialSchema").setNull();
        }
        putProp(target, "specialSchemaEssential").setBool(rc.specialSchemaEssential);
        IRIO paramT = putProp(target, "params").setArray();
        for (Param param : rc.params)
            dumpCMDBParam(param, paramT.appendAElem());
    }

    private void dumpCMDBParam(Param param, IRIO target) {
        if (param instanceof RPGCommand.PStatic) {
            RPGCommand.PStatic r = (RPGCommand.PStatic) param;
            target.setHash();
            putProp(target, "type").setString("static");
            if (r.name != null) {
                putProp(target, "name").setString(r.name.r());
            } else {
                putProp(target, "name").setNull();
            }
            putProp(target, "se").setFX(dumpSDBNode(r.se));
        } else if (param instanceof RPGCommand.PDyn) {
            RPGCommand.PDyn r = (RPGCommand.PDyn) param;
            target.setHash();
            putProp(target, "type").setString("dynamic");
            dumpCMDBParam(r.def, putProp(target, "def"));
            putProp(target, "arrayDI").setFX(r.arrayDI);
            IRIO contents = putProp(target, "contents").setHash();
            for (Map.Entry<Integer, Param> ent : r.contents.entrySet()) {
                IRIO ct = putProp(contents, ent.getKey().toString());
                dumpCMDBParam(ent.getValue(), ct);
            }
        } else {
            target.setString(param.toString());
        }
    }

    private long dumpSDBNode(SchemaElement se) {
        // deliberately avoid dumping dynamic schema elements
        if (!(se instanceof SDB.DynamicSchemaElement))
            se = AggregateSchemaElement.extractField(se, null);
        Integer present = seGraph.get(se);
        if (present != null)
            return (int) present;
        int alen = sdbNodes.getALen();
        IRIO newSDBNode = sdbNodes.addAElem(alen);
        seGraph.put(se, alen);
        dumpSDBNodeInner(se, newSDBNode);
        return alen;
    }

    private void dumpSDBNodeInner(SchemaElement se, IRIO newSDBNode) {
        if (se instanceof AggregateSchemaElement) {
            newSDBNode.setArray();
            AggregateSchemaElement ase = (AggregateSchemaElement) se;
            for (SchemaElement se2 : ase.aggregate)
                newSDBNode.appendAElem().setFX(dumpSDBNode(se2));
        } else if (se instanceof PathSchemaElement) {
            newSDBNode.setHash();
            putProp(newSDBNode, "type").setString("path");
            PathSchemaElement ese = (PathSchemaElement) se;
            putProp(newSDBNode, "pStr").setString(ese.pStr.decompiled);
            putProp(newSDBNode, "subElem").setFX(dumpSDBNode(ese.subElem));
            if (ese.alias != null) {
                putProp(newSDBNode, "alias").setString(ese.alias.r());
            } else {
                putProp(newSDBNode, "alias").setNull();
            }
            putProp(newSDBNode, "optional").setBool(ese.optional);
        } else if (se instanceof ArrayElementSchemaElement) {
            newSDBNode.setHash();
            putProp(newSDBNode, "type").setString("index");
            ArrayElementSchemaElement ese = (ArrayElementSchemaElement) se;
            putProp(newSDBNode, "index").setFX(ese.index);
            putProp(newSDBNode, "subElem").setFX(dumpSDBNode(ese.subElem));
            if (ese.alias != null) {
                putProp(newSDBNode, "alias").setString(ese.alias.r());
            } else {
                putProp(newSDBNode, "alias").setNull();
            }
            putProp(newSDBNode, "optional").setBool(ese.optional != null);
        } else if (se instanceof EnumSchemaElement) {
            newSDBNode.setHash();
            putProp(newSDBNode, "type").setString("enum");
            EnumSchemaElement ese = (EnumSchemaElement) se;
            IRIO opts = putProp(newSDBNode, "options").setHash();
            for (Map.Entry<String, UIEnumChoice.Option> opt : ese.lookupOptions.entrySet()) {
                IRIO optT = putProp(opts, opt.getKey()).setHash();
                UIEnumChoice.Option optV = opt.getValue();
                putProp(optT, "prefix").setString(optV.textPrefix);
                putProp(optT, "suffix").setString(optV.textSuffix.r());
            }
        } else if (se instanceof DisambiguatorSchemaElement) {
            newSDBNode.setHash();
            putProp(newSDBNode, "type").setString("disambiguator");
            DisambiguatorSchemaElement ese = (DisambiguatorSchemaElement) se;
            putProp(newSDBNode, "dIndex").setString(ese.dIndex.decompiled);
            IRIO dTable = putProp(newSDBNode, "dTable").setHash();
            for (Map.Entry<String, SchemaElement> map : ese.dTable.entrySet()) {
                putProp(dTable, map.getKey()).setFX(dumpSDBNode(map.getValue()));
            }
        } else {
            newSDBNode.setString(se.toString());
        }
    }
}
