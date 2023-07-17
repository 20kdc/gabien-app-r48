/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.toolsets.utils;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.Rect;
import gabien.ui.UIAutoclosingPopupMenu;
import gabien.ui.UILabel;
import gabien.ui.UIPopupMenu;
import gabien.ui.UIScrollLayout;
import gabien.ui.UITextButton;
import r48.App;
import r48.dbs.ObjectInfo;
import r48.io.data.DMKey;
import r48.io.data.RORIO;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

/**
 * ID changer
 * Created 17th July, 2023.
 */
public class UIIDChanger extends App.Prx {
    public @Nullable SchemaPath fixedPath;

    public IDChangerEntry entry;
    public final UITextButton chooseButton;
    public final UITextButton fromButton;
    public final UITextButton toButton;
    public final UITextButton confirmButton;
    public final UITextButton swapModeButton;
    public DMKey fromValue, toValue;

    public UIIDChanger(App app, @Nullable SchemaPath sp) {
        super(app);
        fixedPath = sp;
        entry = app.idc.getFirst();
        fromValue = toValue = entry.extractEnum().defaultVal;
        chooseButton = new UITextButton("", app.f.dialogWindowTH, this::createCBM);
        fromButton = new UITextButton("", app.f.dialogWindowTH, this::fromButton);
        toButton = new UITextButton("", app.f.dialogWindowTH, this::toButton);
        confirmButton = new UITextButton(T.u.usl_confirmReplace, app.f.dialogWindowTH, this::fridge);
        swapModeButton = new UITextButton(T.u.idc_swapMode, app.f.dialogWindowTH, null).togglable(true);
        UIScrollLayout layout = new UIScrollLayout(true, app.f.generalS);
        if (fixedPath != null)
            layout.panelsAdd(new UILabel(T.u.idc_localTo.r(fixedPath.toString()), app.f.dialogWindowTH));
        layout.panelsAdd(chooseButton);
        layout.panelsAdd(new UILabel("", app.f.dialogWindowTH));
        layout.panelsAdd(fromButton);
        layout.panelsAdd(toButton);
        layout.panelsAdd(swapModeButton);
        layout.panelsAdd(new UILabel(T.u.idc_beware, app.f.dialogWindowTH));
        layout.panelsAdd(confirmButton);
        updateText();
        proxySetElement(layout, true);
        setForcedBounds(null, new Rect(0, 0, app.f.scaleGuess(400), app.f.scaleGuess(300)));
    }

    private void createCBM() {
        LinkedList<UIPopupMenu.Entry> ll = new LinkedList<>();
        for (final IDChangerEntry idc : app.idc) {
            ll.add(new UIPopupMenu.Entry(idc.text.r(), () -> {
                entry = idc;
                fromValue = toValue = entry.extractEnum().defaultVal;
                updateText();
            }));
        }
        UIAutoclosingPopupMenu amp = new UIAutoclosingPopupMenu(ll, app.f.menuTH, app.f.menuS, true);
        app.ui.wm.createMenu(chooseButton, amp);
    }

    private void updateText() {
        chooseButton.text = T.u.idc_typeButton.r(entry.text.r());
        fromButton.text = T.u.idc_fromButton.r(app.format(fromValue, entry.userFacing, EnumSchemaElement.Prefix.Prefix));
        toButton.text = T.u.idc_toButton.r(app.format(toValue, entry.userFacing, EnumSchemaElement.Prefix.Prefix));
    }

    private void fromButton() {
        app.ui.wm.createMenu(fromButton, entry.extractEnum().makeEnumChoiceDialog((res) -> {
            fromValue = res;
            updateText();
        }));
    }

    private void toButton() {
        app.ui.wm.createMenu(toButton, entry.extractEnum().makeEnumChoiceDialog((res) -> {
            toValue = res;
            updateText();
        }));
    }

    /**
     * Named so because it's likely to break everything.
     * Seriously, this scans <i>the entire project</i>.
     * It pretty much relies on the hopes and dreams that I've never made a single mistake
     *  that violates constraints that didn't exist when R48 was started.
     * Basically, this function is <i>reckless,</i> and <i>likely to glitch out the world if you make a single mistake.</i>
     */
    private void fridge() {
        SchemaElement[] resolved = entry.resolve();
        LinkedList<SchemaPath> updates = new LinkedList<>();
        SchemaElement.Visitor v = (element, target, path) -> {
            if (IDChangerEntry.match(element, target, resolved)) {
                if (RORIO.rubyEquals(target, fromValue)) {
                    target.setDeepClone(toValue);
                    updates.add(path);
                }
            }
        };
        if (fixedPath != null) {
            fixedPath.editor.visit(fixedPath.targetElement, fixedPath, v);
        } else {
            for (ObjectInfo oi : app.getObjectInfos()) {
                SchemaPath root = new SchemaPath(app.sdb.getSDBEntry(oi.schemaName), app.odb.getObject(oi.idName));
                root.editor.visit(root.targetElement, root, v);
            }
        }
        // Expect the earthquakes to start, around about now...
        for (SchemaPath sp : updates)
            sp.changeOccurred(false);
        // Report what horrors have been committed back to the user.
        app.ui.launchDialog(T.u.idc_fridge.r(updates.size()));
    }

    @Override
    public String toString() {
        return fixedPath != null ? T.t.idChangerLocal : T.t.idChanger;
    }
}
