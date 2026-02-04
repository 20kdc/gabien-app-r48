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

import gabien.ui.UIElement;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import r48.dbs.ObjectInfo;
import r48.io.data.IRIO;
import r48.schema.util.SchemaPath;
import r48.search.CompoundTextAnalyzer;
import r48.search.ITextAnalyzer;
import r48.search.USFROperationMode;
import r48.ui.AppUI;
import r48.ui.search.UIUSFROperationModeSelector;

/**
 * Universal string locator fun, part 2 (almost a year after the first!)
 * Created on 11th August 2023.
 */
public class UIRMUniversalStringFinder extends AppUI.Prx {
    private UIScrollLayout layout;

    private ITextAnalyzer.Instance taInstance;
    private UIObjectInfoSetSelector setSelector;
    private UIUSFROperationModeSelector modeSelector;
    private boolean detailedInfo = false;

    public UIRMUniversalStringFinder(AppUI aui) {
        super(aui);
        layout = new UIScrollLayout(true, app.f.generalS);
        taInstance = CompoundTextAnalyzer.I.instance(app);
        setSelector = new UIObjectInfoSetSelector(U);
        modeSelector = new UIUSFROperationModeSelector(aui, app.f.dialogWindowTH);

        refreshContents();

        proxySetElement(new UISplitterLayout(layout, setSelector, false, 0.5), false);
    }

    private void refreshContents() {
        LinkedList<UIElement> elms = new LinkedList<>();
        elms.add(modeSelector);

        taInstance.setupEditor(U, elms, this::refreshContents);

        elms.add(new UITextButton(T.u.usl_detailedInfo, app.f.dialogWindowTH, () -> {
            detailedInfo = !detailedInfo;
        }).togglable(detailedInfo));

        elms.add(new UITextButton(T.u.usl_find, app.f.dialogWindowTH, () -> {
            // continue...
            int total = 0;
            int files = 0;
            StringBuilder log = new StringBuilder();
            for (ObjectInfo objInfo : setSelector.getSet()) {
                SchemaPath.Page sp = objInfo.makePath(true);
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
                        if (taInstance.matches(dec)) {
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
            U.launchDialog(T.u.usl_completeReportFind.r(total, files) + log);
        }));
        layout.panelsSet(elms);
    }
}
