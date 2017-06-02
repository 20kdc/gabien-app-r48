/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.specialized;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.CMDB;
import r48.dbs.RPGCommand;
import r48.schema.*;
import r48.schema.arrays.StandardArraySchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.integers.ROIntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UIEnumChoice;
import r48.ui.UIHHalfsplit;
import r48.ui.UIScrollVertLayout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Used to make RPGCommands bearable.
 * (Essentially a version of the ArrayDisambiguatorSchema logic,
 * but if the schema system was used to build commands... yeah, no, that ain't happening.)
 * Created on 12/30/16.
 */
public class RPGCommandSchemaElement extends SchemaElement {
    public boolean allowControlOfIndent = false;

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
    public boolean monitorsSubelements() {
        return true;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UIPanel uip = new UIPanel() {
            UITextButton chooseCode = new UITextButton(FontSizes.schemaButtonTextHeight, database.buildCodename(target, true), new Runnable() {
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
                    launcher.switchObject(path.newWindow(new TempDialogSchemaChoice(new UIEnumChoice(new IConsumer<Integer>() {
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
                                if (rc.specialSchemaName != null) {
                                    SchemaElement schemaElement = AppMain.schemas.getSDBEntry(rc.specialSchemaName);
                                    schemaElement.modifyVal(target, path, true);
                                }
                            }
                            // Indent recalculation, and such.
                            path.changeOccurred(false);
                            // On the one hand, the elements are stale.
                            // On the other hand, the elements will be obliterated anyway before reaching the user.
                            launcher.switchObject(path);
                        }
                    }, rvi, order, "Code"), path), target, launcher));
                }
            });
            UIElement subElem = buildSubElem();

            private UIElement buildSubElem() {
                RPGCommand rc = database.knownCommands.get((int) target.getInstVarBySymbol("@code").fixnumVal);
                if (rc != null) {
                    if (rc.specialSchemaName != null)
                        return AppMain.schemas.getSDBEntry(rc.specialSchemaName).buildHoldingEditor(target, launcher, path);
                    RubyIO param = target.getInstVarBySymbol("@parameters");
                    UIScrollVertLayout uiSVL = new UIScrollVertLayout();

                    int height = 0;
                    if (target.getInstVarBySymbol("@indent") != null) {
                        SchemaElement ise = new IVarSchemaElement("@indent", new ROIntegerSchemaElement(0), false);
                        if (!allowControlOfIndent)
                            ise = new IVarSchemaElement("@indent", new IntegerSchemaElement(0), false);
                        height += ise.maxHoldingHeight();
                        uiSVL.panels.add(ise.buildHoldingEditor(target, launcher, path));
                    }
                    SchemaPath parameterPath = path.arrayEntry(param, new StandardArraySchemaElement(new OpaqueSchemaElement(), 0, false));
                    for (int i = 0; i < param.arrVal.length; i++) {
                        if (param.arrVal.length <= i) {
                            uiSVL.panels.add(new UILabel("WARNING: Missing E" + i + ".", FontSizes.schemaFieldTextHeight));
                            continue;
                        }
                        String paramName = rc.getParameterName(i);
                        // Hidden parameters, introduced to deal with the "text" thing brought about by R2k
                        if (!paramName.equals("_")) {
                            SchemaElement ise = rc.getParameterSchema(param, i);
                            height += ise.maxHoldingHeight();
                            UIElement uie = ise.buildHoldingEditor(param.arrVal[i], launcher, parameterPath.arrayHashIndex(new RubyIO().setFX(i), "[" + i + "]"));
                            uiSVL.panels.add(new UIHHalfsplit(1, 3, new UILabel(rc.getParameterName(i), FontSizes.schemaFieldTextHeight), uie));
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
                allElements.add(chooseCode);
                allElements.add(subElem);
                int cch = chooseCode.getBounds().height;
                chooseCode.setBounds(new Rect(0, 0, r.width, cch));
                subElem.setBounds(new Rect(0, cch, r.width, r.height - cch));
            }
        };
        uip.setBounds(new Rect(0, 0, 320, 200));
        return uip;
    }

    @Override
    public int maxHoldingHeight() {
        throw new RuntimeException("Just don't.");
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        actualSchema.modifyVal(target, path, setDefault);
        RPGCommand rc = database.knownCommands.get((int) target.getInstVarBySymbol("@code").fixnumVal);
        if (rc != null) {
            if (rc.specialSchemaName != null) {
                // The amount of parameters isn't always fully described.
                // Cutting down on length is done when the command code is set - That's as good as it gets.
                AppMain.schemas.getSDBEntry(rc.specialSchemaName).modifyVal(target, path, setDefault);
            } else {
                RubyIO param = target.getInstVarBySymbol("@parameters");
                // All parameters are described, and the SASE will ensure length is precisely equal
                SchemaElement parametersSanitySchema = new StandardArraySchemaElement(new OpaqueSchemaElement(), rc.paramName.size(), false);
                parametersSanitySchema.modifyVal(param, path, setDefault);
                SchemaPath parameterPath = path.arrayEntry(param, parametersSanitySchema);
                for (int i = 0; i < param.arrVal.length; i++) {
                    SchemaElement ise = rc.getParameterSchema(param, i);
                    ise.modifyVal(param.arrVal[i], parameterPath.arrayHashIndex(new RubyIO().setFX(i), "[" + i + "]"), setDefault);
                }
            }
        }
        if (allowControlOfIndent) {
            RubyIO rio = target.getInstVarBySymbol("@indent");
            // If it passed actualSchema and @indent doesn't exist, let's just leave it alone
            if (rio != null) {
                long targetIndent = calculateIndent(target, path);
                if (targetIndent != rio.fixnumVal) {
                    rio.fixnumVal = targetIndent;
                    path.changeOccurred(true);
                }
            }
        }
    }

    private long calculateIndent(RubyIO targ, SchemaPath path) {
        // Calculate indent
        SchemaPath array = path.lastArray;
        long targetIndex = path.lastArrayIndex.fixnumVal;

        long indent = 0;
        for (int i = 0; i <= targetIndex; i++) {
            RubyIO cmd = targ;
            // used to deal with arrays which are still in the process of adding this command,
            //  or cases where this value is just being created via a method which doesn't pass the array parameter.
            if (i != targetIndex)
                cmd = array.targetElement.arrVal[i];
            int code = (int) cmd.getInstVarBySymbol("@code").fixnumVal;
            RPGCommand rc = database.knownCommands.get(code);
            if (rc != null) {
                indent += rc.indentPre;
                if (i != targetIndex)
                    indent += rc.indentPost;
            }
        }
        return indent;
    }
}
