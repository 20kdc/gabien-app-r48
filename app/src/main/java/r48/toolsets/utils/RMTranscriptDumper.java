/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.toolsets.utils;

import r48.App;
import r48.dbs.CMDB;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;

/**
 * For documentation purposes.
 * Created on 2/12/17.
 */
public class RMTranscriptDumper extends App.Svc {
    private final PrintStream output;

    private LinkedList<String> tableOfContents = new LinkedList<String>();
    private LinkedList<String> tableOfContentsIID = new LinkedList<String>();

    public RMTranscriptDumper(App app, PrintStream ps) {
        super(app);
        output = ps;
    }

    public void start() {
        tableOfContents.clear();
        tableOfContentsIID.clear();
        output.println("<!DOCTYPE html>");
        output.println("<html><head><title>" + T.h.title + "</title></head><body><a href=\"#toc\">" + T.h.tocLink + "</a>");
    }

    public void end() {
        output.println("<a name=\"toc\"/><h1>" + T.h.toc + "</h1><ol>");
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
        anchor(name + " (" + desc + ")", name);
        output.println("<h1>" + name + "</h1>");
        output.println("<h2>" + desc + "</h2>");
    }

    public void endFile() {
        anchor(null, null);
    }

    public void dump(String name, IRIO code, CMDB database) {
        //anchor(name);
        output.println("<h3>" + escapeHtml(name) + "</h3>");
        output.println("<code><ul>");
        int ci = 0;
        int alen = code.getALen();
        for (int i = 0; i < alen; i++) {
            IRIO cm = code.getAElem(i);
            int ti = (int) cm.getIVar("@indent").getFX();
            while (ci < ti) {
                output.print("<ul>");
                ci++;
            }
            while (ci > ti) {
                output.print("</ul>");
                ci--;
            }
            output.println("<li>" + escapeHtml(database.buildCodename(cm, false, true)) + "</li>");
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

    public static String escapeHtml(String s) {
        StringBuilder r = new StringBuilder();
        // Uses Java codePointAt to save some UTF-16 decoding code
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int cp = s.codePointAt(i);
            String rb = Character.toString(c);
            // Detect UTF-16 fun
            if (c != cp)
                rb += Character.toString(s.charAt(++i));
            boolean special = false;
            // The rules can't be too inclusive because browsers screw up links for no good reason.
            if (c == '<')
                special = true;
            if (c == '>')
                special = true;
            if (c == '&')
                special = true;
            if (special) {
                r.append("&#");
                r.append(cp);
                r.append(";");
            } else {
                r.append(rb);
            }
        }
        return r.toString();
    }

    public void dumpSVList(String n, IRIO arrVal, int st) {
        String[] s = new String[arrVal.getALen()];
        for (int i = 0; i < s.length; i++)
            s[i] = arrVal.getAElem(i).toString();
        dumpBasicList(n, s, st);
    }

    public void dumpSVListHash(String n, IRIO arrHashVal) {
        LinkedList<Long> l = new LinkedList<Long>();
        for (DMKey rio : arrHashVal.getHashKeys())
            l.add(rio.getFX());
        Collections.sort(l);
        output.println("<h3>" + escapeHtml(n) + "</h3>");
        output.print("<ul>");
        for (Long ll : l)
            output.println("<li>" + escapeHtml(ll + " : " + arrHashVal.getHashVal(DMKey.of(ll))) + "</li>");
        output.println("</ul>");
    }

    public void dumpHTML(String s) {
        output.println(s);
    }
}
