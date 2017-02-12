/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.toolsets;

import r48.RubyIO;
import r48.dbs.CMDB;

import java.io.PrintStream;

/**
 * For documentation purposes.
 * Created on 2/12/17.
 */
public class RMTranscriptDumper {
    private final PrintStream output;
    public RMTranscriptDumper(PrintStream ps) {
        output = ps;
    }

    public void start() {
        output.println("<html><head><title>Exported Transcript</title></head><body>");
    }

    public void end() {
        output.println("</body></html>");
    }

    public void startFile(String name, String desc) {
        output.println("<h1>" + name + "</h1>");
        output.println("<h2>" + desc + "</h2>");
    }

    public void endFile() {
    }

    public void dump(String name, RubyIO[] code, CMDB database) {
        output.println("<h3>" + name + "</h3>");
        output.println("<code>");
        for (RubyIO cm : code)
            output.println(escapeHtml(database.buildCodename(cm)) + "<br/>");
        output.println("</code>");
    }

    private String escapeHtml(String s) {
        String r = "";
        // Crude mechanism for decoding UTF-16 bullshit.
        // (gabien.ui just gives up for now until a proper interface between the app (which wants to display things however it wants)
        //  and the world (which wants the app to display things how it wants) can be found
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int cp = s.codePointAt(i);
            String rb = Character.toString(c);
            if (c != cp) {
                // Special case
                rb += Character.toString(s.charAt(++i));
            }
            boolean special = false;
            // These rules are probably too inclusive, but they ought to work
            if (c == '<')
                special = true;
            if (c == '>')
                special = true;
            if (c == '&')
                special = true;
            if (cp > 127)
                special = true;
            if (special) {
                r += "&#" + cp + ";";
            } else {
                r += rb;
            }
        }
        return r;
    }
}
