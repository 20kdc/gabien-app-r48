/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.cmgb;

import gabien.ui.*;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UISplitterLayout;
import r48.App;
import r48.dbs.CMDB;
import r48.dbs.PathSyntax;
import r48.dbs.RPGCommand;
import r48.io.IntUtils;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.OpaqueSchemaElement;
import r48.schema.PathSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.arrays.StandardArrayInterface;
import r48.schema.arrays.StandardArraySchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.integers.ROIntegerSchemaElement;
import r48.schema.specialized.TempDialogSchemaChoice;
import r48.schema.util.EmbedDataKey;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.pages.TrRoot;
import r48.ui.UIAppendButton;
import r48.ui.UIFieldLayout;
import r48.ui.dialog.UIEnumChoice;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Used to make RPGCommands bearable.
 * (Essentially a version of the ArrayDisambiguatorSchema logic,
 * but if the schema system was used to build commands... yeah, no, that ain't happening.)
 * Created on 12/30/16.
 */
public class RPGCommandSchemaElement extends SchemaElement {
    public final boolean allowControlOfIndent;
    public final boolean showHeader;

    // actualSchema is used for modifyVal,
    // while mostOfSchema is used for display.
    public final SchemaElement actualSchema, mostOfSchema;

    public final CMDB database;

    private RPGCommandSchemaElement hiddenHeadVer;

    public final EmbedDataKey<Double> scrollPointKey = new EmbedDataKey<>();

    public RPGCommandSchemaElement(App app, SchemaElement ise, SchemaElement mos, CMDB db, boolean allowIndentControl, boolean showHdr) {
        super(app);
        actualSchema = ise;
        mostOfSchema = mos;
        database = db;
        allowControlOfIndent = allowIndentControl;
        showHeader = showHdr;
        if (!showHeader)
            hiddenHeadVer = this;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path2) {
        // A note here:
        // Using newWindow on path will cause a growing stack issue:
        //  newWindow always returns DIRECTLY to the path, subwindows use Back which 
        //  skips past anything which hasn't been called upon for UI.
        // Basically, don't use newWindow directly on a tagSEMonitor, don't switchObject to a tagSEMonitor.
        // They're tags for stuff done inside the schema, not part of the schema itself.

        final SchemaPath path = path2.tagSEMonitor(target, this, false);

        if (showHeader) {
            UIElement chooseCode = new UIAppendButton(T.s.cmdHelp, new UITextButton(database.buildCodename(target, true, true), app.f.schemaFieldTH, () -> {
                launcher.pushObject(path2.newWindow(navigateToCode(launcher, target, (_tmp) -> {
                    // Templates don't work from here, but the path does
                    path.changeOccurred(false);
                }, path, database), target));
            }), () -> {
                int code = (int) target.getIVar("@code").getFX();
                RPGCommand rc = database.knownCommands.get(code);
                String title = code + " : ";
                String result = T.s.cmdUnk;
                if (rc != null) {
                    title += rc.formatName(null);
                    if (rc.description == null) {
                        result = T.s.cmdNoDescription;
                    } else {
                        result = rc.description.r();
                    }
                } else {
                    title += T.s.cmdUnkName;
                }
                app.ui.launchDialog(title + "\n" + result);
            }, app.f.schemaFieldTH);

            return new UISplitterLayout(chooseCode, buildSubElem(target, launcher, path), true, 0);
        }
        return buildSubElem(target, launcher, path);
    }

    private UIElement buildSubElem(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        RPGCommand rc = getRPGCommand(target);
        if (rc != null) {
            if (rc.specialSchema != null)
                return rc.specialSchema.buildHoldingEditor(target, launcher, path);
            IRIO param = target.getIVar("@parameters");
            LinkedList<UIElement> uiSVLList = new LinkedList<>();

            if (target.getIVar("@indent") != null) {
                if (showHeader) {
                    PathSyntax indent = PathSyntax.compile(app, "@indent");
                    SchemaElement ise = new PathSchemaElement(indent, () -> T.s.theTrueNameOfAtIndent, new ROIntegerSchemaElement(app, 0), false);
                    if (!allowControlOfIndent)
                        ise = new PathSchemaElement(indent, () -> T.s.theTrueNameOfAtIndent, new IntegerSchemaElement(app, 0), false);
                    uiSVLList.add(ise.buildHoldingEditor(target, launcher, path));
                }
            }
            UILabel[] labels = new UILabel[param.getALen()];
            AtomicInteger labelWidth = new AtomicInteger();
            for (int i = 0; i < labels.length; i++) {
                @Nullable String paramName = rc.getParameterName(param, i);
                if (paramName == null)
                    continue;
                labels[i] = new UILabel(paramName + " ", app.f.schemaFieldTH);
                labelWidth.set(Math.max(labelWidth.get(), labels[i].getWantedSize().width));
            }
            for (int i = 0; i < labels.length; i++) {
                if (labels[i] != null) {
                    SchemaElement ise = rc.getParameterSchema(param, i);
                    UIElement uie = ise.buildHoldingEditor(param.getAElem(i), launcher, path.arrayHashIndex(DMKey.of(i), "[" + i + "]"));
                    uiSVLList.add(new UIFieldLayout(labels[i], uie, labelWidth, true));
                    rc.paramSpecialTags.get(i).applyTo(i, uiSVLList, param, launcher, path);
                }
            }
            return AggregateSchemaElement.createScrollSavingSVL(launcher, scrollPointKey, target, uiSVLList);
        }
        return mostOfSchema.buildHoldingEditor(target, launcher, path);
    }

    // Used by EventCommandArray for edit-on-create.
    // NOTE: displayPath is the path of the command window
    protected static TempDialogSchemaChoice navigateToCode(final ISchemaHost launcher, final IRIO target, final Consumer<int[]> templateAndConfirm, final SchemaPath path, final CMDB database) {
        UIEnumChoice.Category[] categories = database.buildEnum();

        final App app = launcher.getApp();
        final TrRoot T = app.t;
        return new TempDialogSchemaChoice(app, new UIEnumChoice(app, (integer) -> {
            long fnv = integer.getFX();
            // NOTE: This just uses ints for everything.
            RPGCommand rc = database.knownCommands.get((int) fnv);
            target.getIVar("@code").setFX(fnv);
            IRIO param = target.getIVar("@parameters");
            if (rc != null) {
                // Notice: Both are used!
                // Firstly nuke it to whatever the command says for array-len-reduce, then use the X-code to fill in details
                param.setArray();
                int size = rc.params.size();
                IntUtils.resizeArrayTo(param, size);
                for (int i = 0; i < size; i++) {
                    IRIO rio = param.getAElem(i);
                    SchemaElement ise = rc.getParameterSchema(param, i);
                    ise.modifyVal(rio, path.arrayHashIndex(DMKey.of(i), "[" + i + "]"), true);
                }
                if (rc.specialSchema != null) {
                    SchemaElement schemaElement = rc.specialSchema;
                    schemaElement.modifyVal(target, path, true);
                }
                templateAndConfirm.accept(rc.template);
            } else {
                templateAndConfirm.accept(new int[0]);
            }
            // On the one hand, the elements are stale.
            // On the other hand, the elements will be obliterated anyway before reaching the user.
            // This isn't done automatically by UIEnumChoice.
            launcher.popObject();
        }, categories, T.s.codeAsInOpcode, UIEnumChoice.EntryMode.INT), null, path);
    }

    public @Nullable RPGCommand getRPGCommand(RORIO target) {
        return database.knownCommands.get((int) target.getIVar("@code").getFX());
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        path = path.tagSEMonitor(target, this, false);
        actualSchema.modifyVal(target, path, setDefault);
        RPGCommand rc = getRPGCommand(target);
        if (rc != null) {
            if (rc.specialSchema != null) {
                // The amount of parameters isn't always fully described.
                // Cutting down on length is done when the command code is set - That's as good as it gets.
                rc.specialSchema.modifyVal(target, path, setDefault);
            } else {
                IRIO param = target.getIVar("@parameters");
                // All parameters are described, and the SASE will ensure length is precisely equal
                SchemaElement parametersSanitySchema = new StandardArraySchemaElement(app, new OpaqueSchemaElement(app), rc.params.size(), false, 0, new StandardArrayInterface());
                parametersSanitySchema.modifyVal(param, path, setDefault);
                int alen = param.getALen();
                for (int i = 0; i < alen; i++) {
                    SchemaElement ise = rc.getParameterSchema(param, i);
                    ise.modifyVal(param.getAElem(i), path.arrayHashIndex(DMKey.of(i), "[" + i + "]"), setDefault);
                }
            }
        }
    }

    @Override
    public void visitChildren(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths) {
        path = path.tagSEMonitor(target, this, false);
        actualSchema.visit(target, path, v, detailedPaths);
        RPGCommand rc = getRPGCommand(target);
        if (rc != null) {
            if (rc.specialSchema != null) {
                // The amount of parameters isn't always fully described.
                // Cutting down on length is done when the command code is set - That's as good as it gets.
                rc.specialSchema.visit(target, path, v, detailedPaths);
            } else {
                IRIO param = target.getIVar("@parameters");
                // All parameters are described, and the SASE will ensure length is precisely equal
                SchemaElement parametersSanitySchema = new StandardArraySchemaElement(app, new OpaqueSchemaElement(app), rc.params.size(), false, 0, new StandardArrayInterface());
                parametersSanitySchema.visit(param, path, v, detailedPaths);
                int alen = param.getALen();
                for (int i = 0; i < alen; i++) {
                    SchemaElement ise = rc.getParameterSchema(param, i);
                    ise.visit(param.getAElem(i), path.arrayHashIndex(DMKey.of(i), "[" + i + "]"), v, detailedPaths);
                }
            }
        }
    }

    public RPGCommandSchemaElement hideHeaderVer() {
        if (hiddenHeadVer != null)
            return hiddenHeadVer;
        RPGCommandSchemaElement rcse = new RPGCommandSchemaElement(app, actualSchema, mostOfSchema, database, allowControlOfIndent, false);
        hiddenHeadVer = rcse;
        return rcse;
    }
}
