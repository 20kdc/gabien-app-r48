/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.search;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UIElement;
import gabien.ui.elements.UITextButton;
import r48.App;
import r48.dbs.CMDB;
import r48.dbs.RPGCommand;
import r48.io.data.RORIO;
import r48.ui.UIChoiceButton;
import r48.ui.dialog.UIEnumChoice;

/**
 * Find a command by a specific command code.
 * Created 1st December, 2023.
 */
public class ByCodeCommandClassifier implements ICommandClassifier {
    @Override
    @NonNull
    public String getName(App app) {
        return app.t.u.ccs_byCode;
    }

    @Override
    @NonNull
    public Instance instance(App app) {
        final CMDB[] cmdbs = app.sdb.getAllCMDBs();
        if (cmdbs.length == 0) {
            // Uhhhh
            return new Instance() {
                @Override
                public void setupEditor(@NonNull LinkedList<UIElement> usl, @NonNull Runnable onEdit) {
                }

                @Override
                public boolean matches(@Nullable RPGCommand dbEntry, @Nullable RORIO cmd) {
                    return false;
                }
            };
        }
        return new Instance() {
            public @NonNull CMDB cmdb = cmdbs[0];
            public @Nullable RPGCommand rpgCommand;
            @Override
            public void setupEditor(@NonNull LinkedList<UIElement> usl, @NonNull Runnable onEdit) {
                UIChoiceButton<CMDB> whichDB = new UIChoiceButton<CMDB>(app, app.f.dialogWindowTH, cmdb, cmdbs) {
                    @Override
                    public String choiceToText(CMDB choice) {
                        return choice.dbId;
                    }
                    @Override
                    public void setSelected(CMDB defChoice) {
                        super.setSelected(defChoice);
                        cmdb = defChoice;
                        rpgCommand = null;
                        onEdit.run();
                    }
                };
                usl.add(whichDB);
                String buttonText = rpgCommand != null ? rpgCommand.formatName(null) : "";
                UITextButton utb = new UITextButton(buttonText, app.f.dialogWindowTH, null);
                utb.onClick = () -> {
                    app.ui.wm.createMenu(utb, new UIEnumChoice(app, (res) -> {
                        rpgCommand = cmdb.knownCommands.get((int) res.getFX());
                        onEdit.run();
                    }, cmdb.buildEnum(), buttonText, UIEnumChoice.EntryMode.INT));
                };
                usl.add(utb);
            }

            @Override
            public boolean matches(@Nullable RPGCommand dbEntry, @Nullable RORIO cmd) {
                if (rpgCommand == null)
                    return true;
                return dbEntry == rpgCommand;
            }
        };
    }

}
