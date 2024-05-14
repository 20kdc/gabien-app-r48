/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import gabien.GaBIEn;
import gabien.datum.DatumSrcLoc;
import gabien.datum.DatumWriter;
import gabien.uslx.vfs.FSBackend;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;

/**
 * Created 14th May, 2024.
 */
public class DBMigrator {
    public static void main(String[] args) throws Exception {
        try {
            tmp();
        } finally {
            GaBIEn.ensureQuit();
        }
    }
    public static void tmp() throws Exception {
        String[] files = {
            "CharGen/Layers",
            "CharGen/Modes",
            "Help/Launcher/Entry",
            "Help/Main/Entry",
            "Help/Main/MapEdit",
            "Help/Main/Toolsets",
            "Help/Tips/Adventure",
            "Help/Tips/Entry",
            "Help/Tips/Secret",
            "Ika/Schema",
            "OSLoc/Schema",
            "R2K/AutoTiles",
            "R2K/Commands",
            "R2K/CommandsGI_0",
            "R2K/CommandsGI_B",
            "R2K/CommandsGI_M",
            "R2K/CommandsMove",
            "R2K/H_ItemActorClass",
            "R2K/H_ItemActorClass2",
            "R2K/H_Levelling",
            "R2K/H_MusicType",
            "R2K/SchemaGeneral",
            "R2K/SchemaLDB",
            "R2K/SchemaLDBTerms",
            "R2K/SchemaLMT",
            "R2K/SchemaLMU",
            "R2K/SchemaLSD",
            "R2K/SchemaScripting",
            "R2K/SchemaScriptingFooter",
            "R2K/SchemaScriptingKII",
            "R2K/SchemaScriptingMonolith",
            "R2K/SchemaScriptingMonolith2",
            "R2K/SchemaScriptingMonolith3",
            "R2K/SchemaScriptingSBGM",
            "R2K/TS144",
            "R2K/TS162",
            "R2K/TSPass",
            "R2K/TSPass144",
            "R2K/TSPass162",
            "R2K/WaterATRules",
            "R2K/WaterATs",
            "R2KXPCOM/AutoTileRules",
            "RCOM/CommonCommands",
            "RCOM/CommonConditionals",
            "RCOM/CommonMove",
            "RCOM/CommonSV",
            "RCOM/SchemaScript",
            "RVXA/AutoTileRules",
            "RVXA/AutoTiles",
            "RVXA/BatActions",
            "RVXA/BatEffects",
            "RVXA/BatFeatures",
            "RVXA/Commands",
            "RVXA/CommandsMove",
            "RVXA/H_BatParamsTableDoc",
            "RVXA/Schema",
            "RVXA/SchemaBatDI",
            "RVXA/SchemaBatEF",
            "RVXA/SchemaCommandHelpers",
            "RVXA/SchemaEditing",
            "RVXA/SchemaFiles",
            "RVXA/WallAT",
            "RVXA/WaterfallAT",
            "RXP/AutoTiles",
            "RXP/Commands",
            "RXP/CommandsI3",
            "RXP/CommandsI4",
            "RXP/CommandsI5",
            "RXP/CommandsI6",
            "RXP/CommandsI7",
            "RXP/CommandsMove",
            "RXP/H_EventGraphics",
            "RXP/H_StuffAboutTiles",
            "RXP/Schema",
            "RXP/SchemaCommandHelpers",
            "RXP/SchemaEditing",
            "RXP/SchemaFiles",
            "RXP/TSTables",
            "RXP/TSTablesPass",
            "Sticki/NoteTypes",
            "Sticki/Schema"
        };
        GaBIEn.initializeEmbedded();
        for (String f : files) {
            System.out.println(f + ".txt");
            FSBackend src = GaBIEn.mutableDataFS.into("src", "main", "resources", "assets").intoRelPath(f + ".txt");
            FSBackend dst = GaBIEn.mutableDataFS.into("src", "main", "resources", "assets").intoRelPath(f + ".scm");
            dst.parentMkdirs();

            StringBuilder sb = new StringBuilder();
            DatumWriter sw = new DatumWriter(sb);
            DBLoader.readFile(f + ".txt", GaBIEn.getResource(f + ".txt"), new IDatabase() {
                @Override
                public void newObj(int objId, String objName) throws IOException {
                    DatumWriter list = sw.visitList(DatumSrcLoc.NONE);
                    list.visitId("obj", DatumSrcLoc.NONE);
                    list.visitInt(objId, DatumSrcLoc.NONE);
                    list.visitString(objName, DatumSrcLoc.NONE);
                    list.visitEnd(DatumSrcLoc.NONE);
                    sw.visitNewline();
                }

                @Override
                public void execCmd(char c, String[] args) throws IOException {
                    DatumWriter list = sw.visitList(DatumSrcLoc.NONE);
                    list.visitId(Character.toString(c), DatumSrcLoc.NONE);
                    for (String s : args)
                        list.visitId(s, DatumSrcLoc.NONE);
                    list.visitEnd(DatumSrcLoc.NONE);
                    sw.visitNewline();
                }

                @Override
                public void comment(String string) {
                    if (string.equals("")) {
                        sw.visitNewline();
                    } else if (string.startsWith("* ")) {
                        sw.visitComment(string.substring(2));
                    } else {
                        sw.visitComment(string);
                    }
                }
            });

            try (OutputStream os = dst.openWrite()) {
                os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            }
            src.delete();
        }
    }
}
