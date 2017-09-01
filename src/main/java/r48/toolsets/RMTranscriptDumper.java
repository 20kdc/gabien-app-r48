/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.toolsets;

import r48.RubyIO;
import r48.dbs.CMDB;
import r48.dbs.TXDB;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * For documentation purposes.
 * Created on 2/12/17.
 */
public class RMTranscriptDumper {
    private final PrintStream output;

    private LinkedList<String> tableOfContents = new LinkedList<String>();
    private LinkedList<String> tableOfContentsIID = new LinkedList<String>();

    public RMTranscriptDumper(PrintStream ps) {
        output = ps;
    }

    public void start() {
        tableOfContents.clear();
        tableOfContentsIID.clear();
        output.println("<html><head><title>" + TXDB.get("Exported Transcript") + "</title></head><body><a href=\"#toc\">" + TXDB.get("To Table Of Contents") + "</a>");
    }

    public void end() {
        output.println("<a name=\"toc\"/><h1>" + TXDB.get("Table Of Contents") + "</h1><ol>");
        int hIndex = 0;
        for (String s : tableOfContents) {
            if (s != null) {
                output.println("<li><a href=\"#p" + tableOfContentsIID.get(hIndex) + "\">" + escapeHtml(s) + "</a><ol>");
            } else {
                output.println("</ol></li>");
            }
            hIndex++;
        }
        output.println("</ol></body></html>");
    }

    private void anchor(String s, String name) {
        if (s != null)
            output.println("<a name=\"p" + name + "\"/>");
        tableOfContents.add(s);
        tableOfContentsIID.add(name);
    }

    // NOTE: File names must be unique, and are used as IDs, so keep them sane - I recommend using, well, file names.
    public void startFile(String name, String desc) {
        anchor(name + " (" + escapeHtml(desc) + ")", name);
        output.println("<h1>" + name + "</h1>");
        output.println("<h2>" + escapeHtml(desc) + "</h2>");
    }

    public void endFile() {
        anchor(null, null);
    }

    public void dump(String name, RubyIO[] code, CMDB database) {
        //anchor(name);
        output.println("<h3>" + escapeHtml(name) + "</h3>");
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
        output.println("<h3>" + escapeHtml(name) + "</h3>");
        output.print("<ol start=\"" + startIndex + "\">");
        for (String it : items)
            output.println("<li>" + escapeHtml(it) + "</li>");
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

    public void dumpSVList(String n, RubyIO[] arrVal, int st) {
        String[] s = new String[arrVal.length];
        for (int i = 0; i < arrVal.length; i++)
            s[i] = arrVal[i].toString();
        dumpBasicList(n, s, st);
    }
    public void dumpSVListHash(String n, RubyIO arrHashVal) {
        LinkedList<Long> l = new LinkedList<Long>();
        for (RubyIO rio : arrHashVal.hashVal.keySet())
            l.add(rio.fixnumVal);
        Collections.sort(l);
        output.println("<h3>" + escapeHtml(n) + "</h3>");
        output.print("<ul>");
        for (Long ll : l)
            output.println("<li>" + escapeHtml(ll + " : " + arrHashVal.getHashVal(new RubyIO().setFX(ll))) + "</li>");
        output.println("</ul>");
    }

    public void dumpHTML(String s) {
        output.println(s);
    }
}
