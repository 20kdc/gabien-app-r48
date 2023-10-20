/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui.dialog;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import gabien.ui.UIScrollLayout;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITabBar.Tab;
import gabien.ui.UITabBar.TabIcon;
import gabien.ui.UITabPane;
import gabien.ui.UITextBox;
import gabien.ui.UITextButton;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import r48.App;
import r48.dbs.ObjectInfo;
import r48.io.data.IRIO;
import r48.schema.util.SchemaPath;
import r48.search.USFROperationMode;
import r48.ui.UIAppendButton;
import r48.ui.search.UIUSFROperationModeSelector;

/**
 * Universal string locator fun, part 2 (almost a year after the first!)
 * Created on 11th August 2023.
 */
public class UIRMUniversalStringFinder extends App.Prx {
    private UIScrollLayout layout = new UIScrollLayout(true, app.f.generalS);
    private RListPanel settingsFull = new RListPanel(app, T.u.usl_full);
    private RListPanel settingsPartial = new RListPanel(app, T.u.usl_partial);

    private UIObjectInfoSetSelector setSelector;
    private UIUSFROperationModeSelector modeSelector;
    private boolean caseInsensitive = true;
    private boolean detailedInfo = false;

    public UIRMUniversalStringFinder(App app) {
        super(app);
        setSelector = new UIObjectInfoSetSelector(app);
        modeSelector = new UIUSFROperationModeSelector(app, app.f.dialogWindowTH);

        refreshContents();

        proxySetElement(new UISplitterLayout(layout, setSelector, false, 0.5), false);
    }

    private void refreshContents() {
        layout.panelsClear();

        settingsFull.refreshContents();
        settingsPartial.refreshContents();

        layout.panelsAdd(modeSelector);

        UITabPane utp = new UITabPane(app.f.finderTabS, false, false);
        utp.addTab(new Tab(settingsFull, new TabIcon[0]));
        utp.addTab(new Tab(settingsPartial, new TabIcon[0]));
        layout.panelsAdd(utp);

        layout.panelsAdd(new UITextButton(T.u.usl_caseInsensitive, app.f.dialogWindowTH, () -> {
            caseInsensitive = !caseInsensitive;
        }).togglable(caseInsensitive));

        layout.panelsAdd(new UITextButton(T.u.usl_detailedInfo, app.f.dialogWindowTH, () -> {
            detailedInfo = !detailedInfo;
        }).togglable(detailedInfo));

        layout.panelsAdd(new UITextButton(T.u.usl_find, app.f.dialogWindowTH, () -> {
            // full
            final HashSet<String> mapFull = new HashSet<>();
            for (String r : settingsFull.settings)
                mapFull.add(caseInsensitive ? r.toLowerCase() : r);
            final LinkedList<String> listPartial = new LinkedList<>(settingsPartial.settings);
            for (String r : settingsPartial.settings)
                listPartial.add(caseInsensitive ? r.toLowerCase() : r);
            // continue...
            int total = 0;
            int files = 0;
            StringBuilder log = new StringBuilder();
            for (ObjectInfo objInfo : setSelector.getSet()) {
                SchemaPath sp = objInfo.makePath(true);
                if (sp != null) {
                    files++;
                    // now do it!
                    USFROperationMode mode = modeSelector.getSelected();
                    AtomicInteger finds = new AtomicInteger(0);
                    HashSet<IRIO> hsi = new HashSet<>();
                    mode.locate(app, sp, (element, target, path) -> {
                        if (!hsi.add(target))
                            return false;
                        String dec = target.decString();
                        if (caseInsensitive)
                            dec = dec.toLowerCase();
                        boolean didFind = false;
                        if (mapFull.contains(dec)) {
                            didFind = true;
                            return false;
                        }
                        if (!didFind)
                            for (String tst : listPartial)
                                if (dec.contains(tst)) {
                                    didFind = true;
                                    break;
                                }
                        if (didFind) {
                            finds.incrementAndGet();
                            if (detailedInfo)
                                log.append("\n@ " + path.toString() + ": " + dec);
                        }
                        return false;
                    }, detailedInfo);
                    int count = finds.get();
                    total += count;
                    if (!detailedInfo)
                        if (count != 0)
                            log.append("\n" + objInfo.toString() + ": " + count);
                }
            }
            app.ui.launchDialog(T.u.usl_completeReportFind.r(total, files) + log);
        }));
    }

    public static class RListPanel extends App.Prx {
        private UIScrollLayout layout = new UIScrollLayout(true, app.f.generalS);
        private LinkedList<String> settings = new LinkedList<String>();

        private UITextBox adderK = new UITextBox("", app.f.dialogWindowTH);

        private UIElement adderA = new UISplitterLayout(new UILabel(T.u.usl_text, app.f.dialogWindowTH), adderK, false, 0);
        private UIElement adderC = new UITextButton(T.u.usl_addS, app.f.dialogWindowTH, () -> {
            if (!settings.contains(adderK.text)) {
                settings.add(adderK.text);
                refreshContents();
            }
        });

        private final String title;

        public RListPanel(App app, String string) {
            super(app);
            title = string;
            refreshContents();
            proxySetElement(layout, true);
        }

        @Override
        public String toString() {
            return title;
        }

        private void refreshContents() {
            layout.panelsClear();

            for (final String key : settings) {
                UIElement keyLine = new UILabel(key, app.f.dialogWindowTH);
                keyLine = new UIAppendButton("-", keyLine, new Runnable() {
                    @Override
                    public void run() {
                        settings.remove(key);
                        refreshContents();
                    }
                }, app.f.dialogWindowTH);
                layout.panelsAdd(keyLine);
            }
            layout.panelsAdd(adderA);
            layout.panelsAdd(adderC);
        }
    }
}