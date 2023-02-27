/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48;

import gabien.GaBIEn;
import gabien.ui.*;
import gabien.uslx.append.*;
import r48.dbs.TXDB;
import r48.io.PathUtils;
import r48.io.data.IRIO;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.specialized.MagicalBinders;
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
public class UITest extends App.Prx {
    public static final Comparator<String> COMPARATOR_NATSTRCOMP = new Comparator<String>() {
        @Override
        public int compare(String t0, String t1) {
            return natStrComp(t0, t1);
        }
    };
    
    public IRIO currentObj;
    public String[] navigaList;
    public IRIO[] objectList;
    int offset = 0;
    public LinkedList<IRIO> back = new LinkedList<IRIO>();
    // the naming got screwed up with the Nth layout redesign.
    // UITest -> outerPanel -> Back/PRINT
    //                      -> masterPanel
    public UIScrollLayout masterPanel = new UIScrollLayout(true, FontSizes.generalScrollersize);
    public UINSVertLayout outerPanel = new UINSVertLayout(new UIAppendButton(TXDB.get("PTS"), new UIAppendButton(TXDB.get("PTF"), new UITextButton(TXDB.get("Back..."), FontSizes.inspectorBackTextHeight, new Runnable() {
        @Override
        public void run() {
            if (back.size() > 0)
                loadObject(back.removeLast());
        }
    }), new Runnable() {
        @Override
        public void run() {
            try {
                OutputStream fos = GaBIEn.getOutFile(getPrintPath(app));
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

    public static String getPrintPath(App app) {
        return PathUtils.autoDetectWindows(app.rootPath + "PRINT.txt");
    }

    public UITest(App app, IRIO obj) {
        super(app);
        loadObject(obj);
        proxySetElement(outerPanel, false);
        setForcedBounds(null, new Rect(0, 0, FontSizes.scaleGuess(320), FontSizes.scaleGuess(240)));
    }

    public void loadObject(final IRIO obj) {
        offset = 0;
        currentObj = obj;
        LinkedList<String> strings = new LinkedList<String>();
        LinkedList<IRIO> targs = new LinkedList<IRIO>();
        // -- Actually collate things
        for (String s : sortedKeysStrArr(obj.getIVars())) {
            strings.add("IVar " + s + " -> " + obj.getIVar(s));
            targs.add(obj.getIVar(s));
        }
        if (obj.getType() == '{') {
            for (IRIO s : sortedKeysArr(obj.getHashKeys())) {
                strings.add(s + " -> " + obj.getHashVal(s));
                targs.add(obj.getHashVal(s));
            }
        }
        if (obj.getType() == '[') {
            int alen = obj.getALen();
            for (int i = 0; i < alen; i++) {
                IRIO o = obj.getAElem(i);
                strings.add(i + " -> " + o);
                targs.add(o);
            }
        }
        // --
        navigaList = strings.toArray(new String[0]);
        objectList = targs.toArray(new IRIO[0]);
        masterPanel.panelsClear();
        for (int i = 0; i < navigaList.length; i++) {
            final int j = i;
            UIElement button = new UITextButton(navigaList[i], FontSizes.inspectorTextHeight, new Runnable() {
                @Override
                public void run() {
                    back.addLast(obj);
                    loadObject(objectList[j]);
                }
            });
            final IMagicalBinder b = MagicalBinders.getBinderFor(objectList[j]);
            if (b != null)
                button = new UIAppendButton(TXDB.get("Binding"), button, new Runnable() {
                    @Override
                    public void run() {
                        back.addLast(obj);
                        loadObject(MagicalBinders.toBoundWithCache(app, b, objectList[j]));
                    }
                }, FontSizes.inspectorTextHeight);
            masterPanel.panelsAdd(button);
        }
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
        Collections.sort(ios, COMPARATOR_NATSTRCOMP);
        return ios;
    }

    public static LinkedList<String> sortedKeysStrArr(String[] iVarKeys) {
        HashSet<String> hs = new HashSet<String>();
        if (iVarKeys != null)
            Collections.addAll(hs, iVarKeys);
        return sortedKeysStr(hs);
    }

    public static LinkedList<IRIO> sortedKeys(Set<IRIO> rubyIOs) {
        return sortedKeys(rubyIOs, new IFunction<IRIO, String>() {
            @Override
            public String apply(IRIO rubyIO) {
                return rubyIO.toString();
            }
        });
    }

    public static LinkedList<IRIO> sortedKeys(Set<IRIO> rubyIOs, final IFunction<IRIO, String> toString) {
        LinkedList<IRIO> ios = new LinkedList<IRIO>(rubyIOs);
        Collections.sort(ios, new Comparator<IRIO>() {
            @Override
            public int compare(IRIO rubyIO, IRIO t1) {
                return natStrComp(toString.apply(rubyIO), toString.apply(t1));
            }
        });
        return ios;
    }

    public static LinkedList<IRIO> sortedKeysArr(IRIO[] iVarKeys) {
        HashSet<IRIO> hs = new HashSet<IRIO>();
        if (iVarKeys != null)
            Collections.addAll(hs, iVarKeys);
        return sortedKeys(hs);
    }

    public static LinkedList<IRIO> sortedKeysArr(IRIO[] iVarKeys, final IFunction<IRIO, String> toString) {
        HashSet<IRIO> hs = new HashSet<IRIO>();
        if (iVarKeys != null)
            Collections.addAll(hs, iVarKeys);
        return sortedKeys(hs, toString);
    }
}
