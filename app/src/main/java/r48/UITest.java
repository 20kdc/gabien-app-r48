/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48;

import gabien.GaBIEn;
import gabien.datum.DatumSymbol;
import gabien.ui.*;
import gabien.uslx.append.*;
import r48.io.IObjectBackend;
import r48.io.PathUtils;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.specialized.MagicalBinders;
import r48.ui.UIAppendButton;
import r48.ui.UINSVertLayout;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import org.eclipse.jdt.annotation.Nullable;

/**
 * At first was a break-into-console - now a proper window, if crude.
 * Does the job better than the previous solution, in any case.
 * Got an update (12/31/16) to use UIScrollVertLayout...
 * ...which is why it's now missing the useful left/right scroll control and the "DS" (save currently viewed object) button.
 * Created on 12/27/16.
 */
public class UITest extends App.Prx {
    public static final Comparator<String> COMPARATOR_NATSTRCOMP = UITest::natStrComp;
    
    public RORIO currentObj;
    public String[] navigaList;
    public RORIO[] objectList;
    int offset = 0;
    public LinkedList<RORIO> back = new LinkedList<RORIO>();
    // the naming got screwed up with the Nth layout redesign.
    // UITest -> outerPanel -> Back/PRINT
    //                      -> masterPanel
    public UIScrollLayout masterPanel = new UIScrollLayout(true, app.f.generalS);

    public static String getPrintPath(App app) {
        return PathUtils.autoDetectWindows(app.rootPath + "PRINT.txt");
    }

    public UITest(App app, RORIO obj, final @Nullable IObjectBackend.ILoadedObject rootObj) {
        super(app);
        loadObject(obj);
        UIElement topBar = new UITextButton(T.u.test_back, app.f.inspectorBackTH, () -> {
            if (back.size() > 0)
                loadObject(back.removeLast());
        });
        topBar = new UIAppendButton(T.u.test_PTF, topBar, () -> {
            try {
                OutputStream fos = GaBIEn.getOutFile(getPrintPath(app));
                PrintStream ps = new PrintStream(fos);
                ps.print(currentObj.toStringLong(""));
                fos.close();
                app.ui.launchDialog(T.u.test_prOk);
            } catch (Exception e) {
                app.ui.launchDialog(T.u.test_prFail, e);
            }
        }, app.f.inspectorBackTH);
        topBar = new UIAppendButton(T.u.test_PTS, topBar, () -> {
            app.ui.launchDialog(currentObj.toStringLong(""));
        }, app.f.inspectorBackTH);
        topBar = new UIAppendButton(T.u.test_toREPL, topBar, () -> {
            app.vmCtx.ensureSlot(new DatumSymbol("$obj")).v = currentObj;
            app.ui.launchDialog(T.u.test_toREPLOk);
        }, app.f.inspectorBackTH);
        if (rootObj != null) {
            topBar = new UIAppendButton(T.u.test_withSchema, topBar, () -> {
                app.ui.launchPrompt(T.z.prSchemaID, (res) -> {
                    if (currentObj == rootObj.getObject()) {
                        app.ui.launchSchema(res, rootObj, null);
                    } else {
                        app.ui.launchNonRootSchema(rootObj, "OPAQUE", DMKey.NULL, (IRIO) currentObj, res, "", null);
                    }
                });
            }, app.f.inspectorBackTH);
        }
        UINSVertLayout outerPanel = new UINSVertLayout(topBar, masterPanel);
        proxySetElement(outerPanel, false);
        setForcedBounds(null, new Rect(0, 0, app.f.scaleGuess(320), app.f.scaleGuess(240)));
    }

    public void loadObject(final RORIO obj) {
        offset = 0;
        currentObj = obj;
        LinkedList<String> strings = new LinkedList<String>();
        LinkedList<RORIO> targs = new LinkedList<RORIO>();
        // -- Actually collate things
        for (String s : sortedKeysStrArr(obj.getIVars())) {
            strings.add("IVar " + s + " -> " + obj.getIVar(s));
            targs.add(obj.getIVar(s));
        }
        if (obj.getType() == '{') {
            for (DMKey s : sortedKeysArr(obj.getHashKeys())) {
                strings.add(s + " -> " + obj.getHashVal(s));
                targs.add(obj.getHashVal(s));
            }
        }
        if (obj.getType() == '[') {
            int alen = obj.getALen();
            for (int i = 0; i < alen; i++) {
                RORIO o = obj.getAElem(i);
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
            UIElement button = new UITextButton(navigaList[i], app.f.inspectorTH, () -> {
                back.addLast(obj);
                loadObject(objectList[j]);
            });
            final IMagicalBinder b = MagicalBinders.getBinderFor(app, objectList[j]);
            if (b != null)
                button = new UIAppendButton(T.u.test_binding, button, () -> {
                    back.addLast(obj);
                    loadObject(MagicalBinders.toBoundWithCache(app, b, (IRIO) objectList[j]));
                }, app.f.inspectorTH);
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

    public static LinkedList<DMKey> sortedKeys(Set<DMKey> rubyIOs) {
        return sortedKeys(rubyIOs, RORIO::toString);
    }

    public static LinkedList<DMKey> sortedKeys(Set<DMKey> rubyIOs, final IFunction<RORIO, String> toString) {
        LinkedList<DMKey> ios = new LinkedList<>(rubyIOs);
        Collections.sort(ios, (t0, t1) -> natStrComp(toString.apply(t0), toString.apply(t1)));
        return ios;
    }

    public static LinkedList<DMKey> sortedKeysArr(DMKey[] iVarKeys) {
        HashSet<DMKey> hs = new HashSet<>();
        if (iVarKeys != null)
            Collections.addAll(hs, iVarKeys);
        return sortedKeys(hs);
    }

    public static LinkedList<DMKey> sortedKeysArr(DMKey[] iVarKeys, final IFunction<RORIO, String> toString) {
        HashSet<DMKey> hs = new HashSet<>();
        if (iVarKeys != null)
            Collections.addAll(hs, iVarKeys);
        return sortedKeys(hs, toString);
    }
}
