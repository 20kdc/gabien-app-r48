/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.dbs;

import gabien.ui.IFunction;
import gabien.ui.ISupplier;
import gabien.ui.UIElement;
import r48.AppMain;
import r48.DictionaryUpdaterRunnable;
import r48.RubyIO;
import r48.schema.*;
import r48.schema.arrays.*;
import r48.schema.displays.EPGDisplaySchemaElement;
import r48.schema.displays.HWNDSchemaElement;
import r48.schema.displays.HuePickerSchemaElement;
import r48.schema.displays.TonePickerSchemaElement;
import r48.schema.integers.BitfieldSchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.integers.LowerBoundIntegerSchemaElement;
import r48.schema.integers.ROIntegerSchemaElement;
import r48.schema.specialized.*;
import r48.schema.specialized.cmgb.EventCommandArraySchemaElement;
import r48.schema.specialized.tbleditors.BitfieldTableCellEditor;
import r48.schema.specialized.tbleditors.DefaultTableCellEditor;
import r48.schema.specialized.tbleditors.ITableCellEditor;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The ultimate database, more or less, since this houses the data definitions needed to do things like edit Events.
 * Kinda required for reading maps.
 * Created on 12/30/16.
 */
public class SDB {
    // The very unsafe option which will turn on all sorts of automatic script helper functions.
    // Some of which currently WILL destroy scripts. 315, map29
    // Ok, so I've checked in various ways and done a full restore from virgin copy.
    // I've used UITest to avoid triggering any potentially destructive Schema code.
    // The answer is the same:
    // Entries 9 and 10 of entity 315, in the Map029 file, contain a duplicate Leave Block.
    // I have no idea how this was managed.

    public static boolean allowControlOfEventCommandIndent = false;
    private HashMap<String, SchemaElement> schemaDatabase = new HashMap<String, SchemaElement>();
    protected HashMap<String, SchemaElement> schemaTrueDatabase = new HashMap<String, SchemaElement>();
    private LinkedList<DictionaryUpdaterRunnable> dictionaryUpdaterRunnables = new LinkedList<DictionaryUpdaterRunnable>();
    private LinkedList<Runnable> mergeRunnables = new LinkedList<Runnable>();
    private LinkedList<String> remainingExpected = new LinkedList<String>();

    private HashMap<String, CMDB> cmdbs = new HashMap<String, CMDB>();
    public SDBHelpers helpers = new SDBHelpers();

    private StandardArrayInterface standardArrayUi = new StandardArrayInterface();

    public SDB() {
        schemaDatabase.put("nil", new OpaqueSchemaElement());
        schemaDatabase.put("int", new IntegerSchemaElement(0));
        schemaDatabase.put("roint", new ROIntegerSchemaElement(0));
        schemaDatabase.put("int+0", new LowerBoundIntegerSchemaElement(0, 0));
        schemaDatabase.put("int+1", new LowerBoundIntegerSchemaElement(1, 1));
        schemaDatabase.put("index", new AMAISchemaElement());
        schemaDatabase.put("float", new FloatSchemaElement("0", false));
        schemaDatabase.put("jnum", new FloatSchemaElement("0", true));
        schemaDatabase.put("string", new StringSchemaElement("", '\"'));
        schemaDatabase.put("boolean", new BooleanSchemaElement(false));
        schemaDatabase.put("booleanDefTrue", new BooleanSchemaElement(true));
        schemaDatabase.put("int_boolean", new IntBooleanSchemaElement(false));
        schemaDatabase.put("int_booleanDefTrue", new IntBooleanSchemaElement(true));
        schemaDatabase.put("OPAQUE", new OpaqueSchemaElement());
        schemaDatabase.put("hue", new HuePickerSchemaElement());

        schemaDatabase.put("zlibBlobEditor", new ZLibBlobSchemaElement());
        schemaDatabase.put("stringBlobEditor", new StringBlobSchemaElement());

        schemaDatabase.put("internal_EPGD", new EPGDisplaySchemaElement());
        // Note the deliberate avoidance of the expectation checker here.
        SchemaElement vid = new NameProxySchemaElement("var_id", false);
        schemaDatabase.put("internal_r2kPPPID", helpers.makePicPointerPatchID(vid));
        schemaDatabase.put("internal_r2kPPPV", helpers.makePicPointerPatchVar(vid));
        schemaDatabase.put("internal_scriptIE", new ScriptControlSchemaElement());

        schemaDatabase.put("internal_LF_INDEX", new OSStrHashMapSchemaElement());

        schemaTrueDatabase.putAll(schemaDatabase);
    }

    private IFunction<RubyIO, String> getFunctionToReturn(final String s) {
        return new IFunction<RubyIO, String>() {
            @Override
            public String apply(RubyIO rubyIO) {
                return s;
            }
        };
    }

    public void readFile(final String fName) {
        final String fPfx = "SDB@" + fName;
        DBLoader.readFile(fName, new IDatabase() {
            AggregateSchemaElement workingObj;

            HashMap<String, String> commandBufferNames = new HashMap<String, String>();
            HashMap<String, SchemaElement> commandBufferSchemas = new HashMap<String, SchemaElement>();

            String outerContext = fPfx + "/NONE";

            @Override
            public void newObj(int objId, String objName) {
                outerContext = fPfx + "/commandBuffer";
                workingObj = new AggregateSchemaElement(new SchemaElement[] {});
                if (objId != -1) {
                    commandBufferNames.put(Integer.toString(objId), TXDB.get(outerContext, objName));
                    commandBufferSchemas.put(Integer.toString(objId), workingObj);
                } else {
                    commandBufferSchemas.put("", workingObj);
                }
                //MapSystem.out.println("Array definition when inappropriate: " + objName);
            }

            public SchemaElement handleChain(final String[] args, final int start) {
                return new ISupplier<SchemaElement>() {
                    // This function is recursive but needs state to be kept around after exit.
                    // Kind of a pain, *unless* you have a surrounding instance.
                    public int point = start;

                    @Override
                    public SchemaElement get() {
                        final String text = args[point++];
                        if (text.equals("roint=")) {
                            int n = Integer.parseInt(args[point++]);
                            return new ROIntegerSchemaElement(n);
                        }
                        if (text.equals("int=")) {
                            int n = Integer.parseInt(args[point++]);
                            return new IntegerSchemaElement(n);
                        }
                        if (text.equals("int+0=")) {
                            int n = Integer.parseInt(args[point++]);
                            return new LowerBoundIntegerSchemaElement(0, n);
                        }
                        if (text.equals("int+1=")) {
                            int n = Integer.parseInt(args[point++]);
                            return new LowerBoundIntegerSchemaElement(1, n);
                        }
                        if (text.equals("float="))
                            return new FloatSchemaElement(args[point++], false);
                        if (text.equals("jnum="))
                            return new FloatSchemaElement(args[point++], true);
                        // To translate, or not to? Unfortunately these can point at files.
                        // (later) However, context makes it obvious
                        if (text.equals("string=")) {
                            String esc = args[point++];
                            return new StringSchemaElement(TXDB.get(outerContext, esc), '\"');
                        }
                        // Before you go using these - They are based on *visual* length, and are not hard limits.
                        if (text.equals("stringLen")) {
                            int l = Integer.parseInt(args[point++]);
                            return new StringLenSchemaElement("", l);
                        }
                        if (text.equals("stringLen=")) {
                            String esc = args[point++];
                            int l = Integer.parseInt(args[point++]);
                            return new StringLenSchemaElement(TXDB.get(outerContext, esc), l);
                        }
                        //
                        if (text.equals("hwnd")) {
                            // These need their own translation mechanism
                            String a = args[point++];
                            if (a.equals("."))
                                a = null;
                            return new HWNDSchemaElement(a, args[point++], false);
                        }
                        if (text.equals("hide")) {
                            SchemaElement hide = get();
                            return new HiddenSchemaElement(hide, new IFunction<RubyIO, Boolean>() {
                                @Override
                                public Boolean apply(RubyIO rubyIO) {
                                    return false;
                                }
                            });
                        }
                        if (text.equals("condHide") || text.equals("condHide!")) {
                            final String path = args[point++];
                            SchemaElement hide = get();
                            return new HiddenSchemaElement(hide, new IFunction<RubyIO, Boolean>() {
                                @Override
                                public Boolean apply(RubyIO rubyIO) {
                                    return PathSyntax.parse(rubyIO, path, true).type == (text.endsWith("!") ? 'F' : 'T');
                                }
                            });
                        }
                        if (text.equals("path") || text.equals("pathN")) {
                            String path = args[point++];
                            String txt = "";
                            if (!text.endsWith("N")) {
                                txt = TXDB.get(outerContext, path);
                            } else {
                                txt = TXDB.getExUnderscore(outerContext, args[point++]);
                                if (txt.equals("_"))
                                    txt = null;
                            }
                            SchemaElement hide = get();
                            return new PathSchemaElement(path, txt, hide, true, false);
                        }
                        if (text.equals("optP") || text.equals("optPN")) {
                            String path = args[point++];
                            String txt = "";
                            if (!text.endsWith("N")) {
                                txt = TXDB.get(outerContext, path);
                            } else {
                                txt = TXDB.getExUnderscore(outerContext, args[point++]);
                                if (txt.equals("_"))
                                    txt = null;
                            }
                            SchemaElement hide = get();
                            return new PathSchemaElement(path, txt, hide, true, true);
                        }

                        // CS means "control indent if allowed"
                        // MS means "never control indent"
                        if (text.equals("arrayCS")) {
                            SchemaElement s1, s2;
                            s1 = get();
                            s2 = get();
                            String a = args[point++];
                            return new EventCommandArraySchemaElement(s1, s2, getCMDB(a), allowControlOfEventCommandIndent);
                        }
                        if (text.equals("arrayMS")) {
                            SchemaElement s1, s2;
                            s1 = get();
                            s2 = get();
                            String a = args[point++];
                            return new EventCommandArraySchemaElement(s1, s2, getCMDB(a), false);
                        }

                        // array[E][P][IdX/AL1/AX1/Ix1/IxN]
                        if (text.startsWith("array")) {
                            String ending = text.substring(5);
                            SchemaElement enu = null;
                            IArrayInterface iai = standardArrayUi;
                            if (ending.startsWith("E")) {
                                enu = get();
                                ending = ending.substring(1);
                            }
                            if (ending.startsWith("P")) {
                                iai = new PagerArrayInterface();
                                ending = ending.substring(1);
                            }
                            if (ending.equals("")) {
                                int n = Integer.parseInt(args[point++]);
                                if (n == 0)
                                    n = -1;
                                if (enu != null) {
                                    return new StandardArraySchemaElement(get(), n, false, 0, iai, enu);
                                } else {
                                    return new StandardArraySchemaElement(get(), n, false, 0, iai);
                                }
                            } else if (ending.equals("IdX")) {
                                int x = Integer.parseInt(args[point++]);
                                int n = Integer.parseInt(args[point++]);
                                if (n == 0)
                                    n = -1;
                                if (enu != null) {
                                    return new StandardArraySchemaElement(get(), n, false, x, iai, enu);
                                } else {
                                    return new StandardArraySchemaElement(get(), n, false, x, iai);
                                }
                            } else if (ending.equals("AL1")) {
                                if (enu != null) {
                                    return new StandardArraySchemaElement(get(), -1, true, 0, iai, enu);
                                } else {
                                    return new StandardArraySchemaElement(get(), -1, true, 0, iai);
                                }
                            } else if (ending.equals("AX1")) {
                                if (enu != null) {
                                    return new ArbIndexedArraySchemaElement(get(), 1, 1, -1, iai, enu);
                                } else {
                                    return new ArbIndexedArraySchemaElement(get(), 1, 1, -1, iai);
                                }
                            } else if (ending.equals("Ix1")) {
                                if (enu != null) {
                                    return new ArbIndexedArraySchemaElement(get(), 1, -1, -1, iai, enu);
                                } else {
                                    return new ArbIndexedArraySchemaElement(get(), 1, -1, -1, iai);
                                }
                            } else if (ending.equals("IxN")) {
                                int ofx = Integer.parseInt(args[point++]);
                                int sz = Integer.parseInt(args[point++]);
                                if (sz == 0)
                                    sz = -1;
                                if (enu != null) {
                                    throw new RuntimeException("Incompatible with enumerations!");
                                } else {
                                    return new ArbIndexedArraySchemaElement(get(), ofx, -1, sz, iai);
                                }
                            } else {
                                throw new RuntimeException("Cannot handle array ending " + ending);
                            }
                        }
                        if (text.equals("DA{")) {
                            String disambiguatorIndex = args[point++];
                            SchemaElement backup = get();
                            HashMap<String, SchemaElement> disambiguations = new HashMap<String, SchemaElement>();
                            while (!args[point].equals("}"))
                                disambiguations.put(args[point++], get());
                            disambiguations.put("", backup);
                            return new DisambiguatorSchemaElement(disambiguatorIndex, disambiguations);
                        }
                        if (text.equals("lengthAdjust")) {
                            String text2 = args[point++];
                            int len = Integer.parseInt(args[point++]);
                            return new LengthChangeSchemaElement(TXDB.get(outerContext, text2), len, false);
                        }
                        if (text.equals("lengthAdjustDef")) {
                            String text2 = args[point++];
                            int len = Integer.parseInt(args[point++]);
                            return new LengthChangeSchemaElement(TXDB.get(outerContext, text2), len, true);
                        }
                        /*
                         * Command buffers are assembled by putting entries into commandBufferNames (which is ValueSyntax, Text),
                         *  and into commandBufferSchemas (which is DisambiguatorSyntax, Schema)
                         */
                        if (text.equals("flushCommandBuffer")) {
                            // time to flush it!
                            String disambiguationIVar = args[point++];
                            setSDBEntry(args[point++], new EnumSchemaElement(commandBufferNames, new RubyIO().setFX(0), "INT:" + TXDB.get("Code")));
                            HashMap<String, SchemaElement> baseSE = commandBufferSchemas;
                            commandBufferNames = new HashMap<String, String>();
                            commandBufferSchemas = new HashMap<String, SchemaElement>();
                            return new DisambiguatorSchemaElement(disambiguationIVar, baseSE);
                        }
                        if (text.equals("flushCommandBufferStr")) {
                            // time to flush it!
                            String disambiguationIVar = args[point++];
                            setSDBEntry(args[point++], new EnumSchemaElement(commandBufferNames, new RubyIO().setString("", true), "STR:" + TXDB.get("Code")));
                            HashMap<String, SchemaElement> baseSE = commandBufferSchemas;
                            commandBufferNames = new HashMap<String, String>();
                            commandBufferSchemas = new HashMap<String, SchemaElement>();
                            return new DisambiguatorSchemaElement(disambiguationIVar, baseSE);
                        }
                        if (text.equals("hash")) {
                            SchemaElement k = get();
                            return new HashSchemaElement(k, get(), false);
                        }
                        if (text.equals("hashFlex")) {
                            SchemaElement k = get();
                            return new HashSchemaElement(k, get(), true);
                        }
                        if (text.equals("hashObject") || text.equals("hashObjectInner")) {
                            // This never got used by anything, so it's set as always-v1p1
                            LinkedList<RubyIO> validKeys = new LinkedList<RubyIO>();
                            while (point < args.length)
                                validKeys.add(ValueSyntax.decode(args[point++], true));
                            return new HashObjectSchemaElement(validKeys, text.equals("hashObjectInner"));
                        }
                        if (text.equals("subwindow"))
                            return new SubwindowSchemaElement(get());
                        // subwindow: This\_Is\_A\_Test
                        if (text.equals("subwindow:")) {
                            String text2 = args[point++];
                            if (text2.startsWith("@")) {
                                final String textFinal = text2.substring(1);
                                return new SubwindowSchemaElement(get(), new IFunction<RubyIO, String>() {
                                    @Override
                                    public String apply(RubyIO rubyIO) {
                                        return TXDB.nameDB.get("Interp." + textFinal).apply(rubyIO);
                                    }
                                });
                            } else {
                                return new SubwindowSchemaElement(get(), getFunctionToReturn(TXDB.get(outerContext, text2)));
                            }
                        }

                        if (text.equals("{")) {
                            // Aggregate
                            AggregateSchemaElement subag = new AggregateSchemaElement(new SchemaElement[] {});
                            SchemaElement ise = get();
                            while (ise != null) {
                                subag.aggregate.add(ise);
                                ise = get();
                            }
                            return subag;
                        }

                        if (text.equals("typeChanger{")) {
                            // Type Changer
                            LinkedList<String> strs = new LinkedList<String>();
                            LinkedList<SchemaElement> scms = new LinkedList<SchemaElement>();
                            while (true) {
                                SchemaElement a = get();
                                if (a == null)
                                    break;
                                String b = args[point++];
                                strs.add(b);
                                scms.add(a);
                            }
                            return new TypeChangerSchemaElement(strs.toArray(new String[0]), scms.toArray(new SchemaElement[0]));
                        }

                        // -- These two must be in this order.
                        if (text.startsWith("]?")) {
                            // yay for... well, semi-consistency!
                            String a = text.substring(2);
                            String b = TXDB.getExUnderscore(outerContext, args[point++]);
                            String o = TXDB.get(outerContext, args[point++]);
                            return new ArrayElementSchemaElement(Integer.parseInt(a), b, get(), o, false);
                        }
                        if (text.startsWith("]")) {
                            // yay for consistency!
                            String a = text.substring(1);
                            String b = TXDB.getExUnderscore(outerContext, args[point++]);
                            return new ArrayElementSchemaElement(Integer.parseInt(a), b, get(), null, false);
                        }
                        // --

                        if (text.equals("}"))
                            return null;

                        // Specialized stuff starts here.
                        // This includes anything of type 'u'.
                        if (text.equals("fileSelector")) {
                            String tx = args[point++];
                            String txHR = FormatSyntax.formatExtended(TXDB.get("Browse #A"), new RubyIO().setString(tx, true));
                            return new SubwindowSchemaElement(new FileSelectorSchemaElement(tx, null), getFunctionToReturn(txHR));
                        }
                        if (text.equals("imgSelector")) {
                            String tx = args[point++];
                            String tx2 = args[point++];
                            String txHR = FormatSyntax.formatExtended(TXDB.get("Browse #A"), new RubyIO().setString(tx, true));
                            return new SubwindowSchemaElement(new FileSelectorSchemaElement(tx, tx2), getFunctionToReturn(txHR));
                        }
                        if (text.equals("halfsplit")) {
                            SchemaElement a = get();
                            SchemaElement b = get();
                            return new HalfsplitSchemaElement(a, b);
                        }
                        if (text.equals("spriteSelector")) {
                            // C spritesheet[ Select face index... ] FaceSets/ 48 48 4 0 0 48 48 0
                            // +spriteSelector @face_index @face_name FaceSets/
                            final String varPath = args[point++];
                            final String imgPath = args[point++];
                            final String imgPfx = args[point++];
                            return helpers.makeSpriteSelector(varPath, imgPath, imgPfx, false);
                        }
                        if (text.equals("r2kTonePicker")) {
                            final String rPath = args[point++];
                            final String gPath = args[point++];
                            final String bPath = args[point++];
                            final String sPath = args[point++];
                            return new TonePickerSchemaElement(rPath, gPath, bPath, sPath, 100, false);
                        }
                        if (text.equals("binding")) {
                            String type = args[point++];
                            IMagicalBinder binder = MagicalBinders.getBinderByName(type);
                            if (binder == null)
                                throw new RuntimeException("Unknown binding " + type);
                            return new MagicalBindingSchemaElement(binder, get());
                        }
                        if (text.equals("context?")) {
                            // context? <id> <default>
                            final String idx = args[point++];
                            final SchemaElement insideThat = get();
                            return new SchemaElement() {
                                @Override
                                public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
                                    return getSchema(path).buildHoldingEditor(target, launcher, path);
                                }

                                private SchemaElement getSchema(SchemaPath path) {
                                    SchemaElement se = path.contextualSchemas.get(idx);
                                    if (se == null)
                                        return insideThat;
                                    return se;
                                }

                                @Override
                                public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
                                    getSchema(path).modifyVal(target, path, setDefault);
                                }
                            };
                        }
                        if (text.equals("contextDictionary")) {
                            // D <name> <base or '.'> <default value> <outer path, including root> <'1' means hash> <inner path> <inner path interpretation id> <element to surround with this>
                            // contextDictionary <ctxId> <default 0 @name rpg_troop_core
                            final String contextName = args[point++];
                            final String base = args[point++];
                            final RubyIO defVal = ValueSyntax.decode(args[point++], true);
                            final String outer = args[point++];
                            final boolean hash = args[point++].equals("1");
                            final String inner = args[point++];
                            final String interpret = args[point++];
                            final SchemaElement insideThat = get();
                            return new SchemaElement() {
                                @Override
                                public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath path) {
                                    return insideThat.buildHoldingEditor(target, launcher, applySchema(target, path));
                                }

                                private SchemaPath applySchema(final RubyIO host, SchemaPath path) {
                                    EnumSchemaElement sce = new EnumSchemaElement(new HashMap<String, String>(), defVal, "INT:" + TXDB.get("ID.")) {
                                        @Override
                                        public void liveUpdate() {
                                            options.clear();
                                            if (!base.equals(".")) {
                                                EnumSchemaElement baseEnum = (EnumSchemaElement) schemaTrueDatabase.get(base);
                                                options.putAll(baseEnum.options);
                                                entryMode = baseEnum.entryMode;
                                                // Default val doesn't get carried over since it gets specced here
                                                buttonText = baseEnum.buttonText;
                                            }
                                            RubyIO p = PathSyntax.parse(host, outer, true);
                                            if (p != null)
                                                DictionaryUpdaterRunnable.coreLogic(options, createPathMap(inner), p, hash, interpret);
                                            convertOptions();
                                        }
                                    };
                                    sce.liveUpdate();
                                    return path.contextSchema(contextName, sce);
                                }

                                @Override
                                public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
                                    insideThat.modifyVal(target, applySchema(target, path), setDefault);
                                }
                            };
                        }
                        if (text.equals("bitfield=")) {
                            int i = Integer.parseInt(args[point++]);
                            LinkedList<String> flags = new LinkedList<String>();
                            while (point < args.length)
                                flags.add(args[point++]);
                            return new BitfieldSchemaElement(i, flags.toArray(new String[0]));
                        }
                        if (text.startsWith("table")) {
                            String eText = text;
                            boolean disableRsz = eText.endsWith("X");
                            if (disableRsz)
                                eText = eText.substring(0, eText.length() - 1);
                            boolean hasFlags = eText.endsWith("F");
                            if (hasFlags)
                                eText = eText.substring(0, eText.length() - 1);
                            boolean hasDefault = eText.endsWith("D");
                            if (hasDefault)
                                eText = eText.substring(0, eText.length() - 1);
                            // combination order is tableSTADFX

                            TSDB tilesetAllocations = null;
                            if (eText.equals("tableSTA"))
                                tilesetAllocations = new TSDB(args[point++]);
                            String iV = args[point++];
                            if (iV.equals("."))
                                iV = null;
                            String wV = args[point++];
                            if (wV.equals("."))
                                wV = null;
                            String hV = args[point++];
                            if (hV.equals("."))
                                hV = null;

                            IFunction<RubyIO, String> iVT = getFunctionToReturn(iV == null ? TXDB.get("Open Table...") : TXDB.get(outerContext, iV));

                            int dc = Integer.parseInt(args[point++]);
                            int aW = Integer.parseInt(args[point++]);
                            int aH = Integer.parseInt(args[point++]);
                            int aI = Integer.parseInt(args[point++]);
                            int[] defVals = new int[aI];
                            ITableCellEditor tcf = new DefaultTableCellEditor();
                            if (hasDefault)
                                for (int i = 0; i < defVals.length; i++)
                                    defVals[i] = Integer.parseInt(args[point++]);
                            // Flags which are marked with "." are hidden. Starts with 1, then 2, then 4...
                            if (hasFlags) {
                                LinkedList<String> flags = new LinkedList<String>();
                                while (point < args.length)
                                    flags.add(args[point++]);
                                tcf = new BitfieldTableCellEditor(flags.toArray(new String[0]));
                            }
                            RubyTableSchemaElement r = null;
                            if (eText.equals("tableSTA")) {
                                r = new TilesetAllocTableSchemaElement(tilesetAllocations, iV, wV, hV, dc, aW, aH, aI, tcf, defVals);
                            } else if (eText.equals("tableTS")) {
                                r = new TilesetTableSchemaElement(iV, wV, hV, dc, aW, aH, aI, tcf, defVals);
                            } else if (eText.equals("table")) {
                                r = new RubyTableSchemaElement(iV, wV, hV, dc, aW, aH, aI, tcf, defVals);
                            } else {
                                throw new RuntimeException("Unknown table type " + text);
                            }
                            r.allowResize &= !disableRsz;
                            return new SubwindowSchemaElement(r, iVT);
                        }
                        if (text.equals("CTNative"))
                            return new CTNativeSchemaElement(args[point++]);

                        if (text.equals("mapPositionHelper")) {
                            String a = args[point++];
                            if (a.equals("."))
                                a = null;
                            String b = args[point++];
                            String c = args[point++];
                            return new MapPositionHelperSchemaElement(a, b, c, true);
                        }
                        if (text.equals("eventTileHelper")) {
                            String c = args[point++];
                            String d = args[point++];
                            String a = args[point++];
                            String b = args[point++];
                            return new SubwindowSchemaElement(new EventTileReplacerSchemaElement(new TSDB(b), Integer.parseInt(a), c, d, false), getFunctionToReturn(TXDB.get("Select Tile Graphic...")));
                        }
                        // -- If all else fails, it's an ID to be looked up. --
                        return getSDBEntry(text);
                    }
                }.get();
            }

            private IFunction<RubyIO, RubyIO> createPathMap(final String inner) {
                return new IFunction<RubyIO, RubyIO>() {
                    @Override
                    public RubyIO apply(RubyIO rubyIO) {
                        return PathSyntax.parse(rubyIO, inner, true);
                    }
                };
            }

            @Override
            public void execCmd(char c, final String[] args) throws IOException {
                if (c == 'a') {
                    if (!schemaDatabase.containsKey(args[0]))
                        throw new RuntimeException("Bad Schema Database: 'a' used to expect item " + args[0] + " that didn't exist.");
                } else if (c == ':') {
                    if (args.length == 1) {
                        workingObj = new AggregateSchemaElement(new SchemaElement[]{});
                        outerContext = fPfx + "/" + args[0];
                        setSDBEntry(args[0], new ObjectClassSchemaElement(args[0], workingObj, 'o'));
                    } else {
                        String backup = outerContext;
                        outerContext = args[0];
                        setSDBEntry(args[0], new ObjectClassSchemaElement(args[0], handleChain(args, 1), 'o'));
                        outerContext = backup;
                    }
                } else if (c == '.') {
                    workingObj = new AggregateSchemaElement(new SchemaElement[] {});
                    outerContext = fPfx + "/" + args[0];
                    setSDBEntry(args[0], workingObj);
                } else if (c == '@') {
                    String t = "@" + PathSyntax.poundEscape(args[0]);
                    // Note: the unescaping happens in the Path
                    workingObj.aggregate.add(new PathSchemaElement(t, TXDB.get(outerContext, t), handleChain(args, 1), true, false));
                } else if (c == '}') {
                    String intA0 = args[0];
                    boolean opt = false;
                    // This shouldn't collide with PathSyntax
                    if (intA0.startsWith("?")) {
                        intA0 = intA0.substring(1);
                        opt = true;
                    }
                    // Note: the unescaping happens in the Path
                    // Automatically escape.
                    String t = ":{" + PathSyntax.poundEscape(intA0);
                    workingObj.aggregate.add(new PathSchemaElement(t, TXDB.get(outerContext, args[1]), handleChain(args, 2), true, opt));
                } else if (c == '+') {
                    workingObj.aggregate.add(handleChain(args, 0));
                } else if (c == '>') {
                    String backup = outerContext;
                    outerContext = args[0];
                    setSDBEntry(args[0], handleChain(args, 1));
                    outerContext = backup;
                } else if (c == 'e') {
                    HashMap<String, String> options = new HashMap<String, String>();
                    int defVal = 0;
                    for (int i = 1; i < args.length; i += 2) {
                        int k = Integer.parseInt(args[i]);
                        if (i == 1)
                            defVal = k;
                        String ctx = "SDB@" + args[0];
                        options.put(Integer.toString(k), TXDB.get(ctx, args[i + 1]));
                    }
                    // INT: is part of the format
                    EnumSchemaElement e = new EnumSchemaElement(options, new RubyIO().setFX(defVal), "INT:" + TXDB.get("Integer"));
                    setSDBEntry(args[0], e);
                } else if (c == 's') {
                    // Symbols
                    HashMap<String, String> options = new HashMap<String, String>();
                    for (int i = 1; i < args.length; i++)
                        options.put(":" + args[i], TXDB.get(args[0], args[i]));

                    EnumSchemaElement ese = new EnumSchemaElement(options, ValueSyntax.decode(":" + args[1], true), "SYM:" + TXDB.get("Symbol"));
                    setSDBEntry(args[0], ese);
                } else if (c == 'E') {
                    HashMap<String, String> options = new HashMap<String, String>();
                    for (int i = 2; i < args.length; i += 2) {
                        String ctx = "SDB@" + args[0];
                        options.put(args[i], TXDB.get(ctx, args[i + 1]));
                    }
                    EnumSchemaElement e = new EnumSchemaElement(options, ValueSyntax.decode(args[2], true), "INT:" + TXDB.get(args[0], args[1]));
                    setSDBEntry(args[0], e);
                } else if (c == 'M') {
                    mergeRunnables.add(new Runnable() {
                        @Override
                        public void run() {
                            // Proxies are bad for this.
                            EnumSchemaElement mergeA = (EnumSchemaElement) schemaTrueDatabase.get(args[0]);
                            EnumSchemaElement mergeB = (EnumSchemaElement) schemaTrueDatabase.get(args[1]);
                            HashMap<String, String> finalMap = new HashMap<String, String>();
                            finalMap.putAll(mergeA.options);
                            finalMap.putAll(mergeB.options);
                            SchemaElement ise = new EnumSchemaElement(finalMap, mergeB.defaultVal, mergeB.buttonText);
                            setSDBEntry(args[2], ise);
                        }
                    });
                } else if (c == ']') {
                    workingObj.aggregate.add(new ArrayElementSchemaElement(Integer.parseInt(args[0]), TXDB.get(outerContext, args[1]), handleChain(args, 2), null, false));
                } else if (c == 'i') {
                    readFile(args[0]);
                } else if (c == 'D') {
                    // D <name> <default value> <outer path, including root> <'1' means hash> <inner path> [<interpretation ID>]
                    final String[] root = PathSyntax.breakToken(args[2], true);
                    String interpret = null;
                    if (args.length == 6) {
                        interpret = args[5];
                    } else if (args.length != 5) {
                        throw new RuntimeException("Expects D <name> <default value> <outer path, including root> <'1' means hash> <inner path> [interpretation ID]");
                    }
                    dictionaryUpdaterRunnables.add(new DictionaryUpdaterRunnable(args[0], root[0], createPathMap(root[1]), args[3].equals("1"), createPathMap(args[4]), Integer.parseInt(args[1]), interpret));
                } else if (c == 'd') {
                    // OLD SYSTEM
                    System.err.println("'d'-format is old. It'll stay around but won't get updated. Use 'D'-format instead. " + args[0]);
                    dictionaryUpdaterRunnables.add(new DictionaryUpdaterRunnable(args[0], args[2], new IFunction<RubyIO, RubyIO>() {
                        @Override
                        public RubyIO apply(RubyIO rubyIO) {
                            for (int i = 3; i < args.length; i++)
                                rubyIO = rubyIO.getInstVarBySymbol(args[i]);
                            return rubyIO;
                        }
                    }, false, null, Integer.parseInt(args[1]), null));
                } else if (c == 'A') {
                    // This is needed so the engine actually understands which autotiles map to what
                    int p = 0;
                    AppMain.autoTiles = new ATDB[args.length / 2];
                    for (int i = 0; i < args.length; i += 2) {
                        AppMain.autoTiles[p] = new ATDB(args[i]);
                        // This is needed to make actual autotile *placement* work.
                        // In theory, it's independent of the AutoTiles setup,
                        //  so long as the AutoTiles setup's using the same sprite-sheets.
                        // In practice, it's only been tested with the default AutoTiles.txt setup.
                        if (!args[i + 1].equals("."))
                            AppMain.autoTiles[p].calculateInverseMap(args[i + 1]);
                        p++;
                    }
                } else if (c == 'C') {
                    if (args[0].equals("md")) {
                        // This is getting changed over for sanity reasons.
                        // It's not used right now, so it's safe to move it over.
                        // The new syntax is Cmd <DisambiguatorSyntax> ["Name!"]
                        outerContext = fPfx + "/commandBuffer";
                        workingObj = new AggregateSchemaElement(new SchemaElement[] {});
                        String val = args[1];
                        String nam = val;
                        if (args.length == 3) {
                            nam = args[2];
                        } else if (args.length != 2) {
                            throw new RuntimeException("Cmd <Disambiguator> [<Name>]");
                        }
                        nam = TXDB.get(outerContext + "/" + val, nam);
                        commandBufferNames.put(val, nam);
                        commandBufferSchemas.put(val, workingObj);
                    }
                    if (args[0].equals("allowIndentControl"))
                        allowControlOfEventCommandIndent = true;
                    if (args[0].equals("defineIndent")) {
                        if (allowControlOfEventCommandIndent) {
                            schemaDatabase.put("indent", new ROIntegerSchemaElement(0));
                        } else {
                            schemaDatabase.put("indent", new IntegerSchemaElement(0));
                        }
                        schemaTrueDatabase.put("indent", schemaDatabase.get("indent"));
                    }
                    if (args[0].equals("objectDB"))
                        AppMain.odbBackend = args[1];
                    if (args[0].equals("recommendMkdir"))
                        AppMain.recommendedDirs.add(args[1]);
                    if (args[0].equals("dataPath"))
                        AppMain.dataPath = args[1];
                    if (args[0].equals("dataExt"))
                        AppMain.dataExt = args[1];
                    if (args[0].equals("versionId"))
                        AppMain.sysBackend = args[1];
                    if (args[0].equals("defaultCB")) {
                        workingObj = new AggregateSchemaElement(new SchemaElement[] {});
                        commandBufferSchemas.put("x default", workingObj);
                    }
                    if (args[0].equals("magicGenpos")) {
                        // Really special schema
                        String aS = args[2];
                        String bS = args[3];
                        String cS = args[4];
                        String dS = args[5];
                        if (aS.equals("."))
                            aS = null;
                        if (bS.equals("."))
                            bS = null;
                        if (cS.equals("."))
                            cS = null;
                        if (dS.equals("."))
                            dS = null;
                        workingObj.aggregate.add(new GenposSchemaElement(args[1], aS, bS, cS, dS, Integer.parseInt(args[6])));
                    }
                    if (args[0].equals("magicR2kSystemDefaults")) {
                        // Really special schema
                        workingObj.aggregate.add(new R2kSystemDefaultsInstallerSchemaElement(Integer.parseInt(args[1])));
                    }
                    if (args[0].equals("name")) {
                        final LinkedList<String> arguments = new LinkedList<String>();
                        String text = "";
                        boolean nextState = false;
                        for (int i = 2; i < args.length; i++) {
                            if (nextState) {
                                if (text.length() > 0)
                                    text += " ";
                                text += args[i];
                            } else {
                                if (!args[i].equals("|")) {
                                    arguments.add(args[i]);
                                } else {
                                    nextState = true;
                                }
                            }
                        }
                        final String textF = TXDB.get(fPfx + "/" + args[1], text);

                        TXDB.nameDB.put(args[1], new IFunction<RubyIO, String>() {
                            @Override
                            public String apply(RubyIO rubyIO) {
                                LinkedList<RubyIO> parameters = new LinkedList<RubyIO>();
                                for (String arg : arguments) {
                                    RubyIO res = PathSyntax.parse(rubyIO, arg, true);
                                    if (res == null)
                                        break;
                                    parameters.add(res);
                                }
                                return FormatSyntax.formatNameExtended(textF, rubyIO, parameters.toArray(new RubyIO[0]), null);
                            }
                        });
                    }
                    // Defines a spritesheet for spriteSelector.
                    if (args[0].equals("spritesheet[")) {
                        int point = 1;
                        String text2 = args[point++];
                        while (!args[point].equals("]"))
                            text2 += " " + args[point++];
                        point++; // skip ]
                        // returns new point
                        helpers.createSpritesheet(args, point, text2);
                    }
                } else if (c != ' ') {
                    for (String arg : args)
                        System.err.print(arg + " ");
                    System.err.println("(The command " + c + " in the SDB is not supported.)");
                }
            }
        });
    }

    public CMDB getCMDB(String arg) {
        CMDB cm = cmdbs.get(arg);
        if (cm != null)
            return cm;
        CMDB r = new CMDB(arg);
        cmdbs.put(arg, r);
        return r;
    }

    public void confirmAllExpectationsMet() {
        if (remainingExpected.size() > 0)
            throw new RuntimeException("Remaining expectation " + remainingExpected.getFirst());
    }

    public boolean hasSDBEntry(String text) {
        return schemaDatabase.containsKey(text);
    }

    public void setSDBEntry(final String text, SchemaElement ise) {
        remainingExpected.remove(text);
        // If a placeholder exists, keep using that
        if (!schemaDatabase.containsKey(text))
            schemaDatabase.put(text, ise);
        schemaTrueDatabase.put(text, ise);
    }

    public SchemaElement getSDBEntry(final String text) {
        if (schemaDatabase.containsKey(text))
            return schemaDatabase.get(text);
        // Notably, the proxy is put in the database so the expectation is only added once.
        remainingExpected.add(text);
        SchemaElement ise = new NameProxySchemaElement(text, true);
        schemaDatabase.put(text, ise);
        return ise;
    }

    // Use if and only if you deliberately need the changing nature of a proxy (this disables the cache)
    public void ensureSDBProxy(String text) {
        NameProxySchemaElement npse = new NameProxySchemaElement(text, false);
        schemaDatabase.put(text, npse);
    }

    public LinkedList<String> listFileDefs() {
        LinkedList<String> fd = new LinkedList<String>();
        for (String s : schemaDatabase.keySet())
            if (s.startsWith("File."))
                fd.add(s.substring(5));
        return fd;
    }

    public void startupSanitizeDictionaries() {
        for (DictionaryUpdaterRunnable dur : dictionaryUpdaterRunnables)
            dur.sanitize();
        for (Runnable merge : mergeRunnables)
            merge.run();
    }

    public void updateDictionaries(RubyIO map) {
        boolean needsMerge = false;
        for (DictionaryUpdaterRunnable dur : dictionaryUpdaterRunnables)
            needsMerge |= dur.actIfRequired(map);
        if (needsMerge)
            for (Runnable merge : mergeRunnables)
                merge.run();
    }

    public void kickAllDictionariesForMapChange() {
        for (DictionaryUpdaterRunnable dur : dictionaryUpdaterRunnables)
            dur.run();
    }
}
