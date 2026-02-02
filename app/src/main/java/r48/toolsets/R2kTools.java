/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.toolsets;

import java.util.LinkedList;
import java.util.function.Consumer;

import r48.dbs.ObjectRootHandle;
import r48.io.data.DMKey;
import r48.io.data.DMPath;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;
import r48.schema.util.UISchemaHostWidget;
import r48.ui.AppUI;
import r48.ui.UIDynAppPrx;
import r48.ui.search.UICommandSites;
import gabien.ui.UIElement;
import gabien.ui.dialogs.UIPopupMenu;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UISplitterLayout;
import gabien.uslx.append.Rect;

/**
 * R2k-specific tools.
 * Created 21st August, 2024.
 */
public class R2kTools extends AppUI.Svc implements Consumer<LinkedList<UIPopupMenu.Entry>> {
    public R2kTools(AppUI app) {
        super(app);
    }

    @Override
    public void accept(LinkedList<UIPopupMenu.Entry> entries) {
        entries.add(new UIPopupMenu.Entry(T.u.mFindCommonEventsWithSwitchID, () -> {
            IRIOGeneric ig = new IRIOGeneric(app.ctxWorkspaceAppEncoding);
            ig.setFX(1);
            SchemaElement se = app.sdb.getSDBEntry("switch_id");
            ObjectRootHandle orh = new ObjectRootHandle.Isolated(se, ig, T.u.mFindCommonEventsWithSwitchID);
            UITextButton ok = new UITextButton(T.g.bConfirm, app.f.dialogWindowTH, null);
            UISchemaHostWidget w = new UISchemaHostWidget(U, null);
            w.pushObject(new SchemaPath(orh));
            UIDynAppPrx prx = UIDynAppPrx.wrap(U, new UISplitterLayout(w, ok, false, 1));
            ok.onClick = () -> {
                ObjectRootHandle database = app.odb.getObject("RPG_RT.ldb");
                long fx = ig.getFX();
                UICommandSites ucs = new UICommandSites(U, T.u.mFindCommonEventsWithSwitchID, () -> {
                    LinkedList<UIElement> sites = new LinkedList<>();
                    // alright, now get ahold of the common events...
                    IRIO cevs = database.getObject().getIVar("@common_events");
                    DMPath cevsPath = DMPath.EMPTY_RELAXED.withIVar("@common_events");
                    for (DMKey key : cevs.getHashKeys()) {
                        IRIO target = cevs.getHashVal(key);
                        if (target.getIVar("@condition_switch").getType() != 'T')
                            continue;
                        if (target.getIVar("@condition_switch_id").getFX() != fx)
                            continue;
                        DMPath targetPath = cevsPath.withHash(key);
                        sites.add(new UITextButton(key + ": " + app.format(target), app.f.schemaFieldTH, () -> {
                            U.launchSchemaTrace(database, null, targetPath);
                        }));
                    }
                    // we're done!
                    return sites.toArray(new UIElement[0]);
                }, new ObjectRootHandle[] {database});
                ucs.show();
                prx.selfClose = true;
            };
            prx.setForcedBounds(null, new Rect(0, 0, prx.getWantedSize().width * 2, prx.getWantedSize().height));
            U.wm.createWindow(prx);
        }));
    }
}
