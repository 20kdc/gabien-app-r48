/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.cmgb;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.CMDB;
import r48.dbs.FormatSyntax;
import r48.dbs.RPGCommand;
import r48.dbs.TXDB;
import r48.schema.*;
import r48.schema.arrays.StandardArrayInterface;
import r48.schema.arrays.StandardArraySchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.integers.ROIntegerSchemaElement;
import r48.schema.specialized.TempDialogSchemaChoice;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;
import r48.ui.UIEnumChoice;
import r48.ui.help.UIHelpSystem;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

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

    public RPGCommandSchemaElement(SchemaElement ise, SchemaElement mos, CMDB db, boolean allowIndentControl, boolean showHdr) {
        actualSchema = ise;
        mostOfSchema = mos;
        database = db;
        allowControlOfIndent = allowIndentControl;
        showHeader = showHdr;
        if (!showHeader)
            hiddenHeadVer = this;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path2) {
        // A note here:
        // Using newWindow on path will cause a growing stack issue:
        //  newWindow always returns DIRECTLY to the path, subwindows use Back which 
        //  skips past anything which hasn't been called upon for UI.
        // Basically, don't use newWindow directly on a tagSEMonitor, don't switchObject to a tagSEMonitor.
        // They're tags for stuff done inside the schema, not part of the schema itself.

        final SchemaPath path = path2.tagSEMonitor(target, this, false);

        UIElement chooseCode = new UIAppendButton(TXDB.get(" ? "), new UITextButton(database.buildCodename(target, true), FontSizes.schemaButtonTextHeight, new Runnable() {
            @Override
            public void run() {
                navigateToCode(launcher, path2, target, path, database);
            }
        }), new Runnable() {
            @Override
            public void run() {
                int code = (int) target.getInstVarBySymbol("@code").fixnumVal;
                RPGCommand rc = database.knownCommands.get(code);
                String title = code + " : " + rc.formatName(null, null);
                String result = TXDB.get("This command isn't known by the schema's CMDB.");
                if (rc != null) {
                    if (rc.description == null) {
                        result = TXDB.get("This command is known, but no description exists.");
                    } else {
                        result = rc.description;
                    }
                } else {
                    title += TXDB.get("Unknown Command");
                }
                UIHelpSystem uis = new UIHelpSystem();
                uis.page.add(new UIHelpSystem.HelpElement('.', title.split(" ")));
                uis.page.add(new UIHelpSystem.HelpElement('.', result.split(" ")));
                launcher.launchOther(uis);
            }
        }, FontSizes.schemaButtonTextHeight);

        return new UISplitterLayout(chooseCode, buildSubElem(target, launcher, path), true, 0);
    }

    private UIElement buildSubElem(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        RPGCommand rc = database.knownCommands.get((int) target.getInstVarBySymbol("@code").fixnumVal);
        if (rc != null) {
            if (rc.specialSchema != null)
                return rc.specialSchema.buildHoldingEditor(target, launcher, path);
            RubyIO param = target.getInstVarBySymbol("@parameters");
            final UIScrollLayout uiSVL = AggregateSchemaElement.createScrollSavingSVL(path, launcher, RPGCommandSchemaElement.this, target);

            if (target.getInstVarBySymbol("@indent") != null) {
                if (showHeader) {
                    SchemaElement ise = new PathSchemaElement("@indent", TXDB.get("@indent"), new ROIntegerSchemaElement(0), false);
                    if (!allowControlOfIndent)
                        ise = new PathSchemaElement("@indent", TXDB.get("@indent"), new IntegerSchemaElement(0), false);
                    uiSVL.panelsAdd(ise.buildHoldingEditor(target, launcher, path));
                }
            }
            UILabel[] labels = new UILabel[param.arrVal.length];
            AtomicInteger labelWidth = new AtomicInteger();
            for (int i = 0; i < param.arrVal.length; i++) {
                if (param.arrVal.length <= i)
                    continue;
                String paramName = rc.getParameterName(param, i);
                // Hidden parameters, introduced to deal with the "text as first parameter" thing brought about by R2k
                if (paramName.equals("_"))
                    continue;
                labels[i] = new ArrayElementSchemaElement.UIOverridableWidthLabel(paramName + " ", FontSizes.schemaFieldTextHeight, labelWidth, true);
                labelWidth.set(Math.max(labelWidth.get(), labels[i].getWantedSize().width));
            }
            for (int i = 0; i < param.arrVal.length; i++) {
                if (param.arrVal.length <= i) {
                    uiSVL.panelsAdd(new UILabel(FormatSyntax.formatExtended(TXDB.get("WARNING: Missing param. #A"), new RubyIO().setFX(i)), FontSizes.schemaFieldTextHeight));
                    continue;
                }
                if (labels[i] != null) {
                    SchemaElement ise = rc.getParameterSchema(param, i);
                    UIElement uie = ise.buildHoldingEditor(param.arrVal[i], launcher, path.arrayHashIndex(new RubyIO().setFX(i), "[" + i + "]"));
                    uiSVL.panelsAdd(new UISplitterLayout(labels[i], uie, false, 0d));
                    rc.paramSpecialTags.get(i).applyTo(i, uiSVL, param, launcher, path);
                }
            }
            return uiSVL;
        }
        return mostOfSchema.buildHoldingEditor(target, launcher, path);
    }

    // Used by EventCommandArray for edit-on-create.
    protected static void navigateToCode(final ISchemaHost launcher, final SchemaPath path2, final RubyIO target, final SchemaPath path, final CMDB database) {
        UIEnumChoice.Category[] categories = new UIEnumChoice.Category[database.categories.length];
        for (int i = 0; i < categories.length; i++) {
            LinkedList<UIEnumChoice.Option> llo = new LinkedList<UIEnumChoice.Option>();
            for (Integer key : database.knownCommandOrder) {
                RPGCommand rc = database.knownCommands.get(key);
                String text = key + ";" + rc.formatName(null, null);
                if (rc.category == i)
                    llo.add(new UIEnumChoice.Option(text, new RubyIO().setFX(key)));
            }
            categories[i] = new UIEnumChoice.Category(database.categories[i], llo);
        }

        launcher.switchObject(path2.newWindow(new TempDialogSchemaChoice(new UIEnumChoice(new IConsumer<RubyIO>() {
            @Override
            public void accept(RubyIO integer) {
                // NOTE: This just uses ints for everything.
                RPGCommand rc = database.knownCommands.get((int) integer.fixnumVal);
                target.getInstVarBySymbol("@code").fixnumVal = integer.fixnumVal;
                RubyIO param = target.getInstVarBySymbol("@parameters");
                if (rc != null) {
                    // Notice: Both are used!
                    // Firstly nuke it to whatever the command says for array-len-reduce, then use the X-code to fill in details
                    param.arrVal = new RubyIO[rc.paramType.size()];
                    for (int i = 0; i < param.arrVal.length; i++) {
                        RubyIO rio = new RubyIO();
                        SchemaElement ise = rc.getParameterSchema(param, i);
                        ise.modifyVal(rio, path.arrayHashIndex(new RubyIO().setFX(i), "[" + i + "]"), true);
                        param.arrVal[i] = rio;
                    }
                    if (rc.specialSchema != null) {
                        SchemaElement schemaElement = rc.specialSchema;
                        schemaElement.modifyVal(target, path, true);
                    }
                }
                // Indent recalculation, and such.
                path.changeOccurred(false);
                // On the one hand, the elements are stale.
                // On the other hand, the elements will be obliterated anyway before reaching the user.
                launcher.switchObject(path2);
            }
        }, categories, TXDB.get("Code"), UIEnumChoice.EntryMode.INT), null, path), target));
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        path = path.tagSEMonitor(target, this, false);
        actualSchema.modifyVal(target, path, setDefault);
        RPGCommand rc = database.knownCommands.get((int) target.getInstVarBySymbol("@code").fixnumVal);
        if (rc != null) {
            if (rc.specialSchema != null) {
                // The amount of parameters isn't always fully described.
                // Cutting down on length is done when the command code is set - That's as good as it gets.
                rc.specialSchema.modifyVal(target, path, setDefault);
            } else {
                RubyIO param = target.getInstVarBySymbol("@parameters");
                // All parameters are described, and the SASE will ensure length is precisely equal
                SchemaElement parametersSanitySchema = new StandardArraySchemaElement(new OpaqueSchemaElement(), rc.paramName.size(), false, 0, new StandardArrayInterface());
                parametersSanitySchema.modifyVal(param, path, setDefault);
                for (int i = 0; i < param.arrVal.length; i++) {
                    SchemaElement ise = rc.getParameterSchema(param, i);
                    ise.modifyVal(param.arrVal[i], path.arrayHashIndex(new RubyIO().setFX(i), "[" + i + "]"), setDefault);
                }
            }
        }
    }

    public RPGCommandSchemaElement hideHeaderVer() {
        if (hiddenHeadVer != null)
            return hiddenHeadVer;
        RPGCommandSchemaElement rcse = new RPGCommandSchemaElement(actualSchema, mostOfSchema, database, allowControlOfIndent, false);
        hiddenHeadVer = rcse;
        return rcse;
    }
}
