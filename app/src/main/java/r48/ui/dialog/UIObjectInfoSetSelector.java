/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui.dialog;

import java.util.function.Consumer;

import gabien.wsi.IPeripherals;
import r48.dbs.ObjectInfo;
import r48.schema.util.SchemaPath;
import r48.ui.AppUI;
import r48.ui.UISetSelector;

/**
 * Created 11th August, 2023.
 */
public class UIObjectInfoSetSelector extends UISetSelector<ObjectInfo> {
    private boolean scheduleSetSelectorUpdate = false;
    private Consumer<SchemaPath> refreshOnObjectChange = (t) -> {
        scheduleSetSelectorUpdate = true;
    };

    public UIObjectInfoSetSelector(AppUI app) {
        super(app, app.app.getObjectInfos());
        for (ObjectInfo ii : getSet())
            app.app.odb.registerModificationHandler(ii.idName, refreshOnObjectChange);
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        if (scheduleSetSelectorUpdate) {
            scheduleSetSelectorUpdate = false;
            refreshButtonText();
        }
        super.update(deltaTime, selected, peripherals);
    }

}
