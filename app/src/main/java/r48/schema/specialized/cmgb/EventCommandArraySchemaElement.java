/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.cmgb;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;

import gabien.ui.*;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import r48.App;
import r48.dbs.CMDB;
import r48.dbs.RPGCommand;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.ArrayElementSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.SubwindowSchemaElement;
import r48.schema.arrays.ArraySchemaElement;
import r48.schema.arrays.StandardArrayInterface;
import r48.schema.specialized.textboxes.R2kTextRules;
import r48.schema.specialized.textboxes.UITextStuffMenu;
import r48.schema.util.EmbedDataDir;
import r48.schema.util.EmbedDataKey;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * ArraySchemaElement + some eventcommand specific stuff to automatically correct issues.
 * (On top of it's previous behaviors, this is now responsible for indent. It monitors subelements *anyway*,
 * and moving the logic here allows me to cut off some of SchemaPath's rube-goldberg-iness.
 * Anything to simplify that thing. Jun 2, 2017.)
 *
 * OK, so here's the March 13th, 2018 accurate notes:
 *
 * The direct subelements of this are halfsplit schema elements designed to generate the indentation reliably.
 * The right side of those is a set of subwindows containing super-special schema elements,
 *  that are tagged with the specific command RubyIO to look for.
 * This allows editing to work properly with groups *and* for it to still be able to handle command movement and deletion.
 * getGroupElement is run inside of these after the target has been located,
 *  in order to finish up the specific interface we're showing to the user at a given time.
 *
 * Created on 1/2/17.
 */
public class EventCommandArraySchemaElement extends ArraySchemaElement {
    public final CMDB database;
    public final RPGCommandSchemaElement baseElement;
    public final EmbedDataKey<Double> eventCommandContextualScrollKey = new EmbedDataKey<>();

    public EventCommandArraySchemaElement(App app, SchemaElement a, SchemaElement b, CMDB db, boolean indentControl) {
        super(app, -1, 0, 0, new StandardArrayInterface().withoutIndexLabels());
        baseElement = new RPGCommandSchemaElement(app, a, b, db, indentControl, true);
        // gets rid of subwindows & proxies
        database = db;
        app.sdb.registerECA(this);
    }

    @Override
    public boolean monitorsSubelements() {
        return true;
    }

    @Override
    public boolean autoCorrectArray(IRIO array, SchemaPath path) {
        if (!app.engine.allowIndentControl)
            return false;

        boolean debugInfloop = false;

        if (debugInfloop)
            System.out.println("---");

        boolean modified = false;

        // NOTE: This method is deliberately awkward to allow for the concurrent modification...
        // Attempting to 'fix' it will only make it worse.
        boolean lastWasBlockLeave = false;
        boolean lastWasStrictLeave = false;
        int lastCode = -1;

        // Indent tracking
        int indent = 0;

        // Note that this array can grow as it's being searched.
        boolean hasValidListLeave = database.listLeaveCmd == null;
        for (int i = 0; i < array.getALen(); i++) {
            IRIO commandTarg = array.getAElem(i);
            int code = (int) commandTarg.getIVar("@code").getFX();
            RPGCommand rc = database.knownCommands.get(code);
            if (rc != null) {
                // Indent stuff
                final int indentOld = indent;
                indent += rc.indentPre;
                if (baseElement.allowControlOfIndent) {
                    IRIO indentTarg = commandTarg.getIVar("@indent");
                    if (indent != indentTarg.getFX()) {
                        indentTarg.setFX(indent);
                        modified = true;
                    }
                }
                // Used to understand infinite loops
                if (debugInfloop)
                    System.out.println("i " + i + " " + code + " " + indent);
                indent += rc.indentPost.apply(commandTarg.getIVar("@parameters"));
                // Group Behavior
                for (IGroupBehavior groupBehavior : rc.groupBehaviors)
                    modified |= groupBehavior.correctElement(array, i, commandTarg);

                if (rc.needsBlockLeavePre) {
                    if (!lastWasBlockLeave) {
                        if (rc.blockLeaveReplacement != lastCode) {
                            IRIO c = array.addAElem(i);
                            SchemaPath.setDefaultValue(c, baseElement, DMKey.of(i));
                            c.getIVar("@code").setFX(database.blockLeaveCmd);
                            if (baseElement.allowControlOfIndent)
                                c.getIVar("@indent").setFX(indentOld);

                            // About to re-handle the same code.
                            indent = indentOld;
                            lastWasBlockLeave = true;
                            // What to do here depends on a few things. They'll be handled in CMDB.
                            lastCode = database.blockLeaveCmd;
                            modified = true;
                            continue;
                        }
                    }
                } else {
                    if (lastWasBlockLeave && lastWasStrictLeave) {
                        array.rmAElem(i - 1);
                        i--;
                        modified = true;
                    }
                }
                if (rc.typeListLeave) {
                    if (i != array.getALen() - 1) {
                        if (rc.typeStrictLeave) {
                            array.rmAElem(i);
                            i--;
                            modified = true;
                            continue;
                        }
                    } else {
                        hasValidListLeave = true;
                    }
                }
                lastWasBlockLeave = rc.typeBlockLeave;
                lastWasStrictLeave = rc.typeStrictLeave;
            } else {
                lastWasBlockLeave = false;
                lastWasStrictLeave = false;
            }
            lastCode = code;
        }

        // This second pass is used by certain group-behaviors that *really, really* need accurate indent information to not cause damage.
        // Specifically consider this for behaviors which add/remove commands.
        boolean continueToBreak = false;
        for (int i = 0; i < array.getALen(); i++) {
            IRIO commandTarg = array.getAElem(i);
            int code = (int) commandTarg.getIVar("@code").getFX();
            RPGCommand rc = database.knownCommands.get(code);
            if (rc != null) {
                for (IGroupBehavior groupBehavior : rc.groupBehaviors) {
                    if (groupBehavior.majorCorrectElement(array, i, commandTarg, baseElement)) {
                        // System.err.println(code);
                        modified = true;
                        continueToBreak = true;
                        break;
                    }
                }
            }
            if (continueToBreak)
                break;
        }

        // After it's done with major structural work, add ending block
        if (!continueToBreak) {
            if (!hasValidListLeave) {
                // 0 so that the code won't combust from lacking an array
                int l = array.getALen();
                IRIO c = array.addAElem(l);
                SchemaPath.setDefaultValue(c, baseElement, DMKey.of(l));
                c.getIVar("@code").setDeepClone(database.listLeaveCmd);
                modified = true;
            }
        }

        return modified;
    }

    @Override
    protected SchemaElement getElementSchema(int j) {
        return new SubwindowSchemaElement(baseElement, (rubyIO) -> "This text should not be visible. Grouping is used for all commands.");
    }

    protected SchemaElement buildGroupContextualUntracked(IRIO arr, final int start, final int length, boolean addRemove, final EmbedDataDir embedDataDir) {
        EmbedDataKey<Double> scrollKey = embedDataDir.key(eventCommandContextualScrollKey);
        // Uhoh.
        boolean canCopyText = false;
        SchemaElement[] group = new SchemaElement[length + 1];
        RPGCommandSchemaElement rcse = baseElement;
        for (int i = 0; i < group.length - 1; i++) {
            IRIO commandTarg = arr.getAElem(start + i);
            int code = (int) commandTarg.getIVar("@code").getFX();
            RPGCommand rc = database.knownCommands.get(code);
            // make group element
            boolean elemAddRemove = addRemove && (i != 0);
            group[i] = new ArrayElementSchemaElement(app, start + i, () -> "", rcse, elemAddRemove ? (() -> "") : null, elemAddRemove);
            if (i == 0)
                rcse = rcse.hideHeaderVer();
            // specifics
            if (rc != null) {
                if (rc.textArg != -1)
                    canCopyText = true;
            }
        }
        final String addText = T.s.bAddToGroup;
        final boolean addRemoveF = addRemove;
        final boolean cctF = canCopyText;
        group[group.length - 1] = new SchemaElement.Leaf(app) {
            @Override
            public UIElement buildHoldingEditorImpl(final IRIO target, ISchemaHost launcher, final SchemaPath path) {
                LinkedList<UIElement> addons = new LinkedList<>();
                if (addRemoveF) {
                    addons.add(new UITextButton(addText, app.f.schemaFieldTH, () -> {
                        IRIO commandTarg = target.getAElem(start);
                        int code = (int) commandTarg.getIVar("@code").getFX();
                        RPGCommand rc = database.knownCommands.get(code);
                        if (rc != null)
                            for (IGroupBehavior groupBehavior : rc.groupBehaviors) {
                                if (groupBehavior.handlesAddition()) {
                                    IRIO ne = target.addAElem(start + length);
                                    SchemaPath.setDefaultValue(ne, baseElement, null);
                                    ne.getIVar("@code").setFX(groupBehavior.getAdditionCode());
                                    path.changeOccurred(false);
                                    break;
                                }
                            }
                    }));
                }
                if (cctF) {
                    UITextButton alignMenuButton = new UITextButton(T.s.align_button, app.f.schemaFieldTH, null);
                    alignMenuButton.onClick = () -> {
                        UITextStuffMenu tsm = new UITextStuffMenu(app, () -> {
                            LinkedList<String> total = new LinkedList<>();
                            for (int i = 0; i < length; i++) {
                                IRIO commandTarg = target.getAElem(start + i);
                                int code = (int) commandTarg.getIVar("@code").getFX();
                                RPGCommand rc = database.knownCommands.get(code);
                                if (rc != null)
                                    if (rc.textArg != -1)
                                        total.add(commandTarg.getIVar("@parameters").getAElem(rc.textArg).decString());
                            }
                            return total.toArray(new String[0]);
                        }, (res) -> {
                            int resIdx = 0;
                            for (int i = 0; i < length; i++) {
                                IRIO commandTarg = target.getAElem(start + i);
                                int code = (int) commandTarg.getIVar("@code").getFX();
                                RPGCommand rc = database.knownCommands.get(code);
                                if (rc != null)
                                    if (rc.textArg != -1)
                                        if (resIdx < res.length)
                                            commandTarg.getIVar("@parameters").getAElem(rc.textArg).setString(res[resIdx++]);
                            }
                            path.changeOccurred(false);
                        }, new R2kTextRules(), 50);
                        app.ui.wm.createMenu(alignMenuButton, tsm);
                    };
                    addons.add(alignMenuButton);
                }
                return new UIScrollLayout(true, app.f.generalS, addons);
            }

            @Override
            public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
            }
        };
        return new AggregateSchemaElement(app, group, scrollKey);
    }

    @Override
    protected GroupInfo getGroupInfo(IRIO arr, final int start, final HashMap<Integer, Integer> indentAnchors) {
        int length = database.getGroupLengthCore(arr, start);
        final boolean addRemove = length >= 1;
        if (length < 1)
            length = 1;
        final int finalLength = length; 

        // Record the first RubyIO of the group.
        // getGroupElement seeks for it now, so it "tracks" the group properly despite array changes.
        final IRIO tracker = arr.getAElem(start);
        int indent = 0;
        final IRIO trackerIndent = tracker.getIVar("@indent");
        if (trackerIndent != null)
            indent = (int) trackerIndent.getFX();
        RPGCommand cmd = database.entryOf(tracker);
        boolean anchor = false;
        boolean shouldShowAnchor = false;
        if (cmd != null) {
            IRIO param = tracker.getIVar("@parameters");
            anchor = cmd.isAnchor(param);
            shouldShowAnchor = cmd.isAnchorVis(param);
        }
        String st = "";
        if (shouldShowAnchor) {
            if (anchor) {
                st = "@" + start + " ";
                indentAnchors.put(indent, start);
            } else {
                Integer lai = indentAnchors.get(indent);
                if (lai != null)
                    st = "@" + lai + " ";
            }
        }
        return new GroupInfo(indent, getElementContextualSubwindowSchema(tracker, start, st), (embedDataDir) -> {
            return buildGroupContextualUntracked(arr, start, finalLength, addRemove, embedDataDir);
        }, length);
    }

    /**
     * Note that this expects the command IRIO passed in as the first argument.
     * It also expects the command index as the second argument.
     * The reason why the schema element changes index but the outside uses a fixed one,
     *  is because the subwindow gets recreated anytime a change happens, while the inside doesn't.
     * The IRIO used for this element is expected to be the list.
     */
    private SubwindowSchemaElement getElementContextualSubwindowSchema(final IRIO tracker, final int start, final String displayPrefix) {
        return new SubwindowSchemaElement(getGroupTrackedWindowSchema(tracker), (rubyIO) -> {
            return displayPrefix + database.buildGroupCodename(rubyIO, start, false);
        });
    }

    @Override
    protected void elementOnCreateMagic(final IRIO target, final int idx, ISchemaHost launcher, SchemaPath ind, SchemaPath path) {
        final SchemaPath sp = path;

        IRIO targetElem = target.getAElem(idx);

        // Notably:
        //  1. the inner-schema always uses the 'path' path.
        //  2. the path constructed must have "back" going to inside the command, then to the array
        //     (so the user knows the command was added anyway)
        SubwindowSchemaElement targ = getElementContextualSubwindowSchema(targetElem, idx, "");
        path = path.arrayHashIndex(DMKey.of(idx), "[" + idx + "]");
        path = path.newWindow(targ.heldElement, target);
        launcher.pushObject(path);
        // Ok, now navigate to the command selector
        path = path.newWindow(RPGCommandSchemaElement.navigateToCode(launcher, targetElem, new Consumer<int[]>() {
            @Override
            public void accept(int[] i) {
                for (int j = 0; j < i.length; j++) {
                    IRIO ne = target.addAElem(idx + j + 1);
                    SchemaPath.setDefaultValue(ne, baseElement, null);
                    ne.getIVar("@code").setFX(i[j]);
                }
                sp.changeOccurred(false);
            }
        }, path, database), target);
        launcher.pushObject(path);
    }
}
