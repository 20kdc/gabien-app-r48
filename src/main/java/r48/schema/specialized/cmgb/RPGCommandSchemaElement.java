/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized.cmgb;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.CMDB;
import r48.dbs.FormatSyntax;
import r48.dbs.RPGCommand;
import r48.dbs.TXDB;
import r48.schema.IVarSchemaElement;
import r48.schema.OpaqueSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.arrays.StandardArraySchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.integers.ROIntegerSchemaElement;
import r48.schema.specialized.TempDialogSchemaChoice;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIAppendButton;
import r48.ui.UIEnumChoice;
import r48.ui.UIHHalfsplit;
import gabien.ui.UIScrollLayout;
import r48.ui.help.UIHelpSystem;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used to make RPGCommands bearable.
 * (Essentially a version of the ArrayDisambiguatorSchema logic,
 * but if the schema system was used to build commands... yeah, no, that ain't happening.)
 * Created on 12/30/16.
 */
public class RPGCommandSchemaElement extends SchemaElement {
    public boolean allowControlOfIndent = false;
    public boolean showHeader = true;

    // actualSchema is used for modifyVal,
    // while mostOfSchema is used for display.
    public SchemaElement actualSchema, mostOfSchema;

    public CMDB database;

    public RPGCommandSchemaElement(SchemaElement ise, SchemaElement mos, CMDB db, boolean allowIndentControl) {
        actualSchema = ise;
        mostOfSchema = mos;
        database = db;
        allowControlOfIndent = allowIndentControl;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path2) {
        // A note here:
        // Using newWindow on path will cause a growing stack issue:
        //  newWindow always returns DIRECTLY to the path, subwindows use Back which 
        //  skips past anything which hasn't been called upon for UI.
        // Basically, don't use newWindow directly on a tagSEMonitor, don't switchObject to a tagSEMonitor.
        // They're tags for stuff done inside the schema, not part of the schema itself.

        final SchemaPath path = path2.tagSEMonitor(target, this);
        final AtomicInteger passbackHeight = new AtomicInteger(0);
        final UIPanel uip = new UIPanel() {
            UIElement chooseCode = new UIAppendButton(TXDB.get(" ? "), new UITextButton(FontSizes.schemaButtonTextHeight, database.buildCodename(target, true), new Runnable() {
                @Override
                public void run() {
                    HashMap<String, Integer> rvi = new HashMap<String, Integer>();
                    HashMap<Integer, String> rvs = new HashMap<Integer, String>();
                    for (Map.Entry<Integer, RPGCommand> me : database.knownCommands.entrySet()) {
                        String text = me.getKey() + ";" + me.getValue().formatName(null, null);
                        rvs.put(me.getKey(), text);
                        rvi.put(text, me.getKey());
                    }
                    LinkedList<String> order = new LinkedList<String>();
                    for (Integer i : database.knownCommandOrder)
                        order.add(rvs.get(i));
                    launcher.switchObject(path2.newWindow(new TempDialogSchemaChoice(new UIEnumChoice(new IConsumer<Integer>() {
                        @Override
                        public void accept(Integer integer) {
                            RPGCommand rc = database.knownCommands.get(integer);
                            target.getInstVarBySymbol("@code").fixnumVal = integer;
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
                    }, rvi, order, TXDB.get("Code")), path), target, launcher));
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
                    uis.setBounds(new Rect(0, 0, 320, 200));
                    launcher.launchOther(uis);
                }
            }, FontSizes.schemaButtonTextHeight);
            UIElement subElem = buildSubElem();

            private UIElement buildSubElem() {
                RPGCommand rc = database.knownCommands.get((int) target.getInstVarBySymbol("@code").fixnumVal);
                if (rc != null) {
                    if (rc.specialSchema != null)
                        return rc.specialSchema.buildHoldingEditor(target, launcher, path);
                    RubyIO param = target.getInstVarBySymbol("@parameters");
                    UIScrollLayout uiSVL = new UIScrollLayout(true);

                    int height = 0;
                    if (target.getInstVarBySymbol("@indent") != null) {
                        if (showHeader) {
                            SchemaElement ise = new IVarSchemaElement("@indent", TXDB.get("@indent"), new ROIntegerSchemaElement(0), false);
                            if (!allowControlOfIndent)
                                ise = new IVarSchemaElement("@indent", TXDB.get("@indent"), new IntegerSchemaElement(0), false);
                            height += ise.maxHoldingHeight();
                            uiSVL.panels.add(ise.buildHoldingEditor(target, launcher, path));
                        }
                    }
                    for (int i = 0; i < param.arrVal.length; i++) {
                        if (param.arrVal.length <= i) {
                            uiSVL.panels.add(new UILabel(FormatSyntax.formatExtended(TXDB.get("WARNING: Missing param. #A"), new RubyIO[] {new RubyIO().setFX(i)}), FontSizes.schemaFieldTextHeight));
                            continue;
                        }
                        String paramName = rc.getParameterName(param, i);
                        // Hidden parameters, introduced to deal with the "text" thing brought about by R2k
                        if (!paramName.equals("_")) {
                            SchemaElement ise = rc.getParameterSchema(param, i);
                            height += ise.maxHoldingHeight();
                            UIElement uie = ise.buildHoldingEditor(param.arrVal[i], launcher, path.arrayHashIndex(new RubyIO().setFX(i), "[" + i + "]"));
                            uiSVL.panels.add(new UIHHalfsplit(1, 3, new UILabel(paramName, FontSizes.schemaFieldTextHeight), uie));
                        }
                    }
                    uiSVL.setBounds(new Rect(0, 0, 128, height));
                    return uiSVL;
                }
                return mostOfSchema.buildHoldingEditor(target, launcher, path);
            }

            @Override
            public void setBounds(Rect r) {
                super.setBounds(r);
                allElements.clear();
                if (showHeader) {
                    allElements.add(chooseCode);
                    allElements.add(subElem);
                    int cch = chooseCode.getBounds().height;
                    passbackHeight.set(cch + subElem.getBounds().height);
                    chooseCode.setBounds(new Rect(0, 0, r.width, cch));
                    subElem.setBounds(new Rect(0, cch, r.width, r.height - cch));
                } else {
                    allElements.add(subElem);
                    passbackHeight.set(subElem.getBounds().height);
                    subElem.setBounds(new Rect(0, 0, r.width, r.height));
                }
            }
        };
        uip.setBounds(new Rect(0, 0, 320, 200));
        uip.setBounds(new Rect(0, 0, 320, passbackHeight.get()));
        return uip;
    }

    @Override
    public int maxHoldingHeight() {
        // Guess :(
        return 256;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        path = path.tagSEMonitor(target, this);
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
                SchemaElement parametersSanitySchema = new StandardArraySchemaElement(new OpaqueSchemaElement(), rc.paramName.size(), false);
                parametersSanitySchema.modifyVal(param, path, setDefault);
                for (int i = 0; i < param.arrVal.length; i++) {
                    SchemaElement ise = rc.getParameterSchema(param, i);
                    ise.modifyVal(param.arrVal[i], path.arrayHashIndex(new RubyIO().setFX(i), "[" + i + "]"), setDefault);
                }
            }
        }
    }

    public RPGCommandSchemaElement hideHeaderVer() {
        RPGCommandSchemaElement rcse = new RPGCommandSchemaElement(actualSchema, mostOfSchema, database, allowControlOfIndent);
        rcse.showHeader = false;
        return rcse;
    }
}
