/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp;

import static datum.DatumTreeUtils.decVisitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import datum.DatumReaderTokenSource;
import datum.DatumSrcLoc;
import datum.DatumSymbol;
import datum.DatumWriter;
import gabien.GaBIEn;
import gabien.uslx.vfs.FSBackend;

/**
 * Created 20th May, 2024.
 */
public class CommandDBRewriter {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        GaBIEn.initializeEmbedded();
        String[] cmds = new String[] {
                "R2K/Commands.scm",
                "R2K/CommandsGI_0.scm",
                "R2K/CommandsGI_B.scm",
                "R2K/CommandsGI_M.scm",
                "R2K/CommandsMove.scm",
                "RCOM/CommonCommands.scm",
                "RCOM/CommonMove.scm",
                "RVXA/Commands.scm",
                "RVXA/CommandsMove.scm",
                "RXP/Commands.scm",
                "RXP/CommandsI3.scm",
                "RXP/CommandsI4.scm",
                "RXP/CommandsI5.scm",
                "RXP/CommandsI6.scm",
                "RXP/CommandsI7.scm",
                "RXP/CommandsMove.scm",
                "Sticki/NoteTypes.scm",
        };
        for (String fn : cmds) {
            String outPath = "rewriter/" + fn;
            FSBackend fsb = GaBIEn.mutableDataFS.intoRelPath(outPath).parentMkdirs();
            OutputStream os = fsb.openWrite();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            InputStreamReader isr = GaBIEn.getTextResource(fn);
            BufferedReader br = new BufferedReader(isr);
            boolean doNewLine = false;
            while (true) {
                String ln = br.readLine();
                if (ln == null)
                    break;
                if (doNewLine)
                    osw.write("\n");
                if (ln.startsWith("(obj") && ln.endsWith(")")) {
                    DatumWriter dw = new DatumWriter(osw);
                    DatumWriter dwl = dw.visitList(DatumSrcLoc.NONE);
                    dwl.visitId("cmd", DatumSrcLoc.NONE);
                    new DatumReaderTokenSource("tmp", ln).visit(decVisitor((obj, srcLoc) -> {
                        LinkedList<Object> contents = (LinkedList<Object>) obj;
                        dwl.visitInt((Long) contents.get(1), DatumSrcLoc.NONE);
                        new DatumReaderTokenSource("tmp-i", (String) contents.get(2)).visit(dwl);
                    }));
                    dwl.visitEnd(DatumSrcLoc.NONE);
                } else if (ln.startsWith("(d ") && ln.endsWith(")")) {
                    DatumWriter dw = new DatumWriter(osw);
                    DatumWriter dwl = dw.visitList(DatumSrcLoc.NONE);
                    dwl.visitId("d", DatumSrcLoc.NONE);
                    new DatumReaderTokenSource("tmp", ln).visit(decVisitor((obj, srcLoc) -> {
                        LinkedList<Object> contents = (LinkedList<Object>) obj;
                        StringBuilder sa = new StringBuilder();
                        for (int i = 1; i < contents.size(); i++) {
                            if (i != 1)
                                sa.append(' ');
                            sa.append(((DatumSymbol) contents.get(i)).id);
                        }
                        dwl.visitString(sa.toString(), DatumSrcLoc.NONE);
                    }));
                    dwl.visitEnd(DatumSrcLoc.NONE);
                } else if ((ln.startsWith("(P ") || ln.startsWith("(v ")) && ln.endsWith(")")) {
                    new DatumReaderTokenSource("tmp", ln).visit(decVisitor((obj, srcLoc) -> {
                        LinkedList<Object> contents = (LinkedList<Object>) obj;
                        contents.set(1, Integer.valueOf(((DatumSymbol) contents.get(1)).id));
                        try {
                            osw.write(DatumWriter.objectToString(obj));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }));
                } else {
                    String ln2 = ln;
                    ln2 = ln2.replace("\\0", "0");
                    ln2 = ln2.replace("\\1", "1");
                    ln2 = ln2.replace("\\2", "2");
                    ln2 = ln2.replace("\\3", "3");
                    ln2 = ln2.replace("\\4", "4");
                    ln2 = ln2.replace("\\5", "5");
                    ln2 = ln2.replace("\\6", "6");
                    ln2 = ln2.replace("\\7", "7");
                    ln2 = ln2.replace("\\8", "8");
                    ln2 = ln2.replace("\\9", "9");
                    osw.write(ln2);
                }
                doNewLine = true;
            }
            osw.close();
        }
        GaBIEn.ensureQuit();
    }
}
