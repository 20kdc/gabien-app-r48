/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.toolsets;

import r48.RubyIO;
import r48.dbs.CMDB;

import java.io.PrintStream;
import java.util.LinkedList;

/**
 * For documentation purposes.
 * Created on 2/12/17.
 */
public class RMTranscriptDumper {
    private final PrintStream output;

    private LinkedList<String> tableOfContents = new LinkedList<String>();

    public RMTranscriptDumper(PrintStream ps) {
        output = ps;
    }

    public void start() {
        tableOfContents.clear();
        output.println("<html><head><title>Exported Transcript</title></head><body><a href=\"#toc\">to table of contents</a>");
    }

    public void end() {
        output.println("<a name=\"toc\"/><h1>Table Of Contents</h1><ol>");
        int hIndex = 0;
        for (String s : tableOfContents) {
            if (s != null) {
                output.println("<li><a href=\"#p" + hIndex + "\">" + escapeHtml(s) + "</a><ol>");
            } else {
                output.println("</ol></li>");
            }
            hIndex++;
        }
        output.println("</ol></body></html>");
    }

    private void anchor(String s) {
        if (s != null)
            output.println("<a name=\"p" + tableOfContents.size() + "\"/>");
        tableOfContents.add(s);
    }

    public void startFile(String name, String desc) {
        anchor(name + " (" + desc + ")");
        output.println("<h1>" + name + "</h1>");
        output.println("<h2>" + desc + "</h2>");
    }

    public void endFile() {
        anchor(null);
    }

    public void dump(String name, RubyIO[] code, CMDB database) {
        //anchor(name);
        output.println("<h3>" + name + "</h3>");
        output.println("<code><ul>");
        int ci = 0;
        for (RubyIO cm : code) {
            int ti = (int) cm.getInstVarBySymbol("@indent").fixnumVal;
            while (ci < ti) {
                output.print("<ul>");
                ci++;
            }
            while (ci > ti) {
                output.print("</ul>");
                ci--;
            }
            output.println("<li>" + escapeHtml(database.buildCodename(cm, false)) + "</li>");
        }
        output.println("</ul></code>");
        //anchor(null);
    }

    public void dumpBasicList(String name, String[] items, int startIndex) {
        //anchor(name);
        output.println("<h3>" + name + "</h3>");
        output.print("<ol start=\"" + startIndex + "\">");
        for (String it : items)
            output.println("<li>" + it + "</li>");
        output.println("</ol>");
        //anchor(null);
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
