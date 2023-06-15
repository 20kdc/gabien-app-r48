/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package gabienapp;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import gabien.GaBIEn;
import gabien.datum.DatumSrcLoc;
import gabien.datum.DatumSymbol;
import gabien.datum.DatumTreeUtils;
import gabienapp.state.LSInApp;
import gabienapp.state.LSMain;
import r48.minivm.MVMU;
import r48.tr.TrNames;
import r48.tr.TrPage.FF0;
import r48.tr.pages.TrRoot;
import r48.ui.dialog.UIReadEvaluatePrintLoop;

/**
 * Severe rearrangement 5th April 2023.
 */
public class LauncherEntry {
    public final FF0 name;
    public final Runnable runnable;
    public LauncherEntry(FF0 n, Runnable r) {
        name = n;
        runnable = r;
    }

    public static LinkedList<LauncherEntry> makeFrom(@Nullable final LinkedList<LauncherEntry> back, LSMain lm, List<Object> o) {
        final LinkedList<LauncherEntry> gp = new LinkedList<>();
        if (back != null)
            gp.add(new LauncherEntry(() -> lm.lun.ilg.t.g.bBack, () -> {
                lm.uiLauncher.setPanel(back);
            }));
        for (Object obj : o)
            gp.add(makeSingleFrom(gp, lm, obj));
        return gp;
    }
    public static LauncherEntry makeSingleFrom(LinkedList<LauncherEntry> here, LSMain lm, Object obj) {
        final TrRoot T = lm.lun.ilg.t;
        if (DatumTreeUtils.isSym(obj, "entry-repl")) {
            return new LauncherEntry(() -> T.g.accessLauncherREPL, () -> {
                String title = T.t.launcherREPL;
                UIReadEvaluatePrintLoop repl = new UIReadEvaluatePrintLoop(lm.c, lm.lun.vmCtx, title);
                repl.setLAFParentOverride(lm.lun.c.lafRoot);
                lm.lun.uiTicker.accept(repl);
                lm.uiLauncher.requestClose();
                lm.lun.currentState = new LSInApp(lm.lun);
            });
        } else if (DatumTreeUtils.isSym(obj, "entry-trs")) {
            return new LauncherEntry(() -> T.g.dumpLLang, () -> {
                GaBIEn.startFileBrowser(T.g.dumpLLang, true, "", (str) -> {
                    if (str == null)
                        return;
                    lm.lun.ilg.launcherDynTrDump(str);
                });
            });
        } else if (obj instanceof DatumSymbol) {
            throw new RuntimeException("unable entry thingy: " + obj);
        }
        List<Object> entList = DatumTreeUtils.cList(obj);
        Object sym = entList.get(0);
        if (DatumTreeUtils.isSym(sym, "category")) {
            if (entList.size() < 3)
                throw new RuntimeException("category TXTSYM TXT");
            String txtSym = MVMU.coerceToString(entList.get(1));
            String txt = MVMU.coerceToString(entList.get(2));
            LinkedList<Object> results = new LinkedList<>();
            for (int i = 3; i < entList.size(); i++)
                results.add(entList.get(i));
            LinkedList<LauncherEntry> state = makeFrom(here, lm, results);
            return new LauncherEntry(lm.dTr(DatumSrcLoc.NONE, TrNames.dynLn(txtSym), txt), () -> {
                lm.uiLauncher.setPanel(state);
            });
        } else if (DatumTreeUtils.isSym(sym, "entry")) {
            if (entList.size() != 5)
                throw new RuntimeException("entry TXTSYM TXT GP ENC");
            String txtSym = MVMU.coerceToString(entList.get(1));
            String txt = MVMU.coerceToString(entList.get(2));
            String gpSym = MVMU.coerceToString(entList.get(3));
            String encSym = MVMU.coerceToString(entList.get(4));
            Runnable state = new StartupCause(lm, encSym, gpSym);
            return new LauncherEntry(lm.dTr(DatumSrcLoc.NONE, TrNames.dynLn(txtSym), txt), state);
        }
        throw new RuntimeException("unable to handle " + sym);
    }
}
