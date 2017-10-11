/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

import gabien.GaBIEn;
import gabien.ui.Rect;
import gabien.ui.UIPanel;
import gabien.ui.UIScrollLayout;
import gabien.ui.UITextButton;
import r48.dbs.TXDB;
import r48.ui.UIAppendButton;
import r48.ui.UINSVertLayout;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * At first was a break-into-console - now a proper window, if crude.
 * Does the job better than the previous solution, in any case.
 * Got an update (12/31/16) to use UIScrollVertLayout...
 * ...which is why it's now missing the useful left/right scroll control and the "DS" (save currently viewed object) button.
 * Created on 12/27/16.
 */
public class UITest extends UIPanel {
    public RubyIO currentObj;
    public String[] navigaList;
    public RubyIO[] objectList;
    int offset = 0;
    public LinkedList<RubyIO> back = new LinkedList<RubyIO>();
    // the naming got screwed up with the Nth layout redesign.
    // UITest -> outerPanel -> Back/PRINT
    //                      -> masterPanel
    public UIScrollLayout masterPanel = new UIScrollLayout(true, FontSizes.generalScrollersize);
    public UINSVertLayout outerPanel = new UINSVertLayout(new UIAppendButton(TXDB.get("PTS"), new UIAppendButton(TXDB.get("PTF"), new UITextButton(FontSizes.inspectorBackTextHeight, TXDB.get("Back..."), new Runnable() {
        @Override
        public void run() {
            if (back.size() > 0)
                loadObject(back.removeLast());
        }
    }), new Runnable() {
        @Override
        public void run() {
            try {
                OutputStream fos = GaBIEn.getOutFile("PRINT.txt");
                PrintStream ps = new PrintStream(fos);
                ps.print(currentObj.toStringLong(""));
                fos.close();
                AppMain.launchDialog(TXDB.get("PRINT.txt written!"));
            } catch (Exception e) {
                AppMain.launchDialog(TXDB.get("Could not print.") + "\n" + e);
            }
        }
    }, FontSizes.inspectorBackTextHeight), new Runnable() {
        @Override
        public void run() {
            AppMain.launchDialog(currentObj.toStringLong(""));
        }
    }, FontSizes.inspectorBackTextHeight), masterPanel);

    public UITest(RubyIO obj) {
        loadObject(obj);
        allElements.add(outerPanel);
        setBounds(new Rect(0, 0, 320, 200));
    }

    public void loadObject(final RubyIO obj) {
        offset = 0;
        currentObj = obj;
        LinkedList<String> strings = new LinkedList<String>();
        LinkedList<RubyIO> targs = new LinkedList<RubyIO>();
        // -- Actually collate things
        for (String s : sortedKeysArr(obj.iVarKeys)) {
            strings.add("IVar " + s + " -> " + obj.getInstVarBySymbol(s));
            targs.add(obj.getInstVarBySymbol(s));
        }
        if (obj.hashVal != null) {
            for (RubyIO s : sortedKeys(obj.hashVal.keySet())) {
                strings.add(s + " -> " + obj.hashVal.get(s));
                targs.add(obj.hashVal.get(s));
            }
        }
        if (obj.arrVal != null) {
            for (int i = 0; i < obj.arrVal.length; i++) {
                RubyIO o = obj.arrVal[i];
                strings.add(i + " -> " + o);
                targs.add(o);
            }
        }
        // --
        navigaList = strings.toArray(new String[0]);
        objectList = targs.toArray(new RubyIO[0]);
        masterPanel.panels.clear();
        for (int i = 0; i < navigaList.length; i++) {
            final int j = i;
            UITextButton button = new UITextButton(FontSizes.inspectorTextHeight, navigaList[i], new Runnable() {
                @Override
                public void run() {
                    back.addLast(obj);
                    loadObject(objectList[j]);
                }
            });
            masterPanel.panels.add(button);
        }
        masterPanel.setBounds(masterPanel.getBounds());
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        outerPanel.setBounds(new Rect(0, 0, r.width, r.height));
    }

    public static int natStrComp(String s, String s1) {
        // Notably, numeric length is major so numbered lists look right.
        // This is the reason a custom comparator was used.
        int nma = numLen(s);
        int nmb = numLen(s1);
        if (nma == 0)
            if (nmb == 0) {
                // Non-sortable via numerics, so stick to something sensible
                nma = s.length();
                nmb = s1.length();
            }

        if (nma < nmb)
            return -1;
        if (nma > nmb)
            return 1;

        // Ok, so natural length sorting didn't quite work out.
        while (true) {
            if (s.length() == 0)
                return 0;
            if (s1.length() == 0)
                return 0;
            char a = s.charAt(0);
            char b = s1.charAt(0);
            if (a > b)
                return 1;
            if (a < b)
                return -1;
            s = s.substring(1);
            s1 = s1.substring(1);
        }
    }

    private static int numLen(String s1) {
        int nm = 0;
        for (char c : s1.toCharArray()) {
            // Breaks at the first thing that couldn't conceivably be sortable.
            // (This usually gives a decently sorted list)
            if ((c >= '0') && (c <= '9')) {
                nm++;
            } else {
                break;
            }
        }
        return nm;
    }

    public static LinkedList<String> sortedKeysStr(Set<String> keys) {
        LinkedList<String> ios = new LinkedList<String>(keys);
        Collections.sort(ios, new Comparator<String>() {
            @Override
            public int compare(String t0, String t1) {
                return natStrComp(t0, t1);
            }
        });
        return ios;
    }

    private LinkedList<String> sortedKeysArr(String[] iVarKeys) {
        HashSet<String> hs = new HashSet<String>();
        if (iVarKeys != null)
            Collections.addAll(hs, iVarKeys);
        return sortedKeysStr(hs);
    }

    public static LinkedList<RubyIO> sortedKeys(Set<RubyIO> rubyIOs) {
        LinkedList<RubyIO> ios = new LinkedList<RubyIO>(rubyIOs);
        Collections.sort(ios, new Comparator<RubyIO>() {
            @Override
            public int compare(RubyIO rubyIO, RubyIO t1) {
                return natStrComp(rubyIO.toString(), t1.toString());
            }
        });
        return ios;
    }
}
