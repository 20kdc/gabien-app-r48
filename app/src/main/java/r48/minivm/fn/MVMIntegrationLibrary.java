/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import gabien.GaBIEn;
import gabien.datum.DatumSymbol;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMU;
import r48.minivm.MVMSlot;

/**
 * MiniVM standard library.
 * Created 28th February 2023.
 */
public class MVMIntegrationLibrary {
    public static void add(MVMEnvR48 ctx) {
        ctx.defLib("include", (a0) -> {
            ctx.include((String) a0, false);
            return null;
        }).attachHelp("(include FILE) : Includes the given file. The code within magically counts as top-level even if it shouldn't. The filename has \".scm\" appended, and a second file is checked for with \".aux.scm\" appended for user additions.");
        ctx.defineSlot(new DatumSymbol("log")).v = new Log()
                .attachHelp("(log V...) : Logs the given values.");
        ctx.defineSlot(new DatumSymbol("help-html")).v = new HelpHTML(ctx)
                .attachHelp("(help-html) : Creates r48-repl-help.html in the R48 launch directory.");
    }
    public static final class Log extends MVMFn.VA {
        public Log() {
            super("log");
        }

        @Override
        public Object callIndirect(Object[] args) {
            for (Object arg : args)
                System.out.println("MVM Log: " + MVMU.userStr(arg));
            return null;
        }
    }
    public static final class HelpHTML extends MVMFn.Fixed {
        public final MVMEnv ctx;
        public HelpHTML(MVMEnv ctx) {
            super("help-html");
            this.ctx = ctx;
        }
        public String textToHTML(String txt) {
            return txt.replace("\n", "<br/>");
        }
        public String encodeAnchor(String txt) {
            StringBuilder a = new StringBuilder();
            for (char c : txt.toCharArray()) {
                a.append('_');
                a.append((int) c);
            }
            return a.toString();
        }
        @Override
        public Object callDirect() {
            StringBuilder sb = new StringBuilder();
            sb.append("<h1>R48 MiniVM Help</h1>");
            sb.append("<p><i>This is attempting to be a Scheme dialect. However, remember:</i></p><ul>");
            sb.append("<li>Lists are not made up of cons pairs here, they are java.util.List</li>");
            sb.append("<li>The goal is ultimately to create a language to reduce typing without infinitely increasing Java code size for every possible shortcut required</li>");
            sb.append("<li>Things are added on an as-needed basis</li>");
            sb.append("</ul>");
            LinkedList<MVMSlot> slots = new LinkedList<MVMSlot>(ctx.listSlots());
            slots.sort((a, b) -> {
                return a.s.id.compareTo(b.s.id);
            });
            sb.append("Central index: <ul>");
            for (MVMSlot s : slots) {
                Object v = s.v;
                if (v instanceof MVMHelpable) {
                    String help = ((MVMHelpable) v).help;
                    if (help != null) {
                        sb.append("<li><a href=\"#");
                        sb.append(encodeAnchor(s.s.id));
                        sb.append("\">");
                        sb.append(textToHTML(s.s.id));
                        sb.append("</a></li>");
                    }
                }
            }
            sb.append("</ul>");
            for (MVMSlot s : slots) {
                Object v = s.v;
                if (v instanceof MVMHelpable) {
                    String help = ((MVMHelpable) v).help;
                    if (help != null) {
                        sb.append("<a name=\"");
                        sb.append(encodeAnchor(s.s.id));
                        sb.append("\">");
                        sb.append("<h2>");
                        sb.append(textToHTML(s.s.id));
                        sb.append(": ");
                        sb.append(v);
                        sb.append("</h2>");
                        sb.append("</a>");
                        sb.append(textToHTML(help));
                    }
                }
            }
            sb.append("<h2>No help available for...</h2>");
            sb.append("only worry if one of these isn't a translation routine");
            sb.append("<ul>");
            for (MVMSlot s : slots) {
                Object v = s.v;
                if (v instanceof MVMHelpable) {
                    String help = ((MVMHelpable) v).help;
                    if (help == null) {
                        sb.append("<li>");
                        sb.append(textToHTML(s.s.id));
                        sb.append("</li>");
                    }
                }
            }
            sb.append("</ul>");
            try (OutputStream os = GaBIEn.getOutFile("r48-repl-help.html")) {
                os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return "done";
        }
    }
}
