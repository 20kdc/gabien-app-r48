/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.uslx.append.*;
import gabien.ui.UIElement;
import r48.App;
import r48.AppMain;
import r48.DictionaryUpdaterRunnable;
import r48.RubyIO;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.schema.*;
import r48.schema.arrays.*;
import r48.schema.displays.EPGDisplaySchemaElement;
import r48.schema.displays.HWNDSchemaElement;
import r48.schema.displays.HuePickerSchemaElement;
import r48.schema.displays.SoundPlayerSchemaElement;
import r48.schema.displays.TonePickerSchemaElement;
import r48.schema.displays.WindowTitleAttachmentSchemaElement;
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
import r48.ui.dialog.UIEnumChoice;
import r48.ui.dialog.UIEnumChoice.EntryMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The ultimate database, more or less, since this houses the data definitions needed to do things like edit Events.
 * Kinda required for reading maps.
 * Created on 12/30/16.
 */
public class SDB extends App.Svc {
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

    protected HashMap<String, CMDB> cmdbs = new HashMap<String, CMDB>();
    public final SDBHelpers helpers;

    private StandardArrayInterface standardArrayUi = new StandardArrayInterface();

    public LinkedList<String> recommendedDirs = new LinkedList<String>();
    public final OpaqueSchemaElement opaque;

    public SDB(App app) {
        super(app);
        helpers = new SDBHelpers(app);
        opaque = new OpaqueSchemaElement(app);
        schemaDatabase.put("nil", opaque);
        schemaDatabase.put("int", new IntegerSchemaElement(app, 0));
        schemaDatabase.put("roint", new ROIntegerSchemaElement(app, 0));
        schemaDatabase.put("int+0", new LowerBoundIntegerSchemaElement(app, 0, 0));
        schemaDatabase.put("int+1", new LowerBoundIntegerSchemaElement(app, 1, 1));
        schemaDatabase.put("index", new AMAISchemaElement(app));
        schemaDatabase.put("float", new FloatSchemaElement(app, "0", false));
        schemaDatabase.put("jnum", new FloatSchemaElement(app, "0", true));
        schemaDatabase.put("string", new StringSchemaElement(app, "", '\"'));
        schemaDatabase.put("boolean", new BooleanSchemaElement(app, false));
        schemaDatabase.put("booleanDefTrue", new BooleanSchemaElement(app, true));
        schemaDatabase.put("int_boolean", new IntBooleanSchemaElement(app, false));
        schemaDatabase.put("int_booleanDefTrue", new IntBooleanSchemaElement(app, true));
        schemaDatabase.put("OPAQUE", opaque);
        schemaDatabase.put("hue", new HuePickerSchemaElement(app));

        schemaDatabase.put("percent", new LowerBoundIntegerSchemaElement(app, 0, 100));

        schemaDatabase.put("zlibBlobEditor", new ZLibBlobSchemaElement(app));
        schemaDatabase.put("stringBlobEditor", new StringBlobSchemaElement(app));

        schemaDatabase.put("internal_EPGD", new EPGDisplaySchemaElement(app));
        schemaDatabase.put("internal_scriptIE", new ScriptControlSchemaElement(app));

        schemaDatabase.put("internal_LF_INDEX", new OSStrHashMapSchemaElement(app));

        schemaTrueDatabase.putAll(schemaDatabase);
    }

    private IFunction<IRIO, String> getFunctionToReturn(final String s) {
        return new IFunction<IRIO, String>() {
            @Override
            public String apply(IRIO rubyIO) {
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
                workingObj = new AggregateSchemaElement(app, new SchemaElement[] {});
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

                    /**
                     * Gets a PathSyntax that can be substituted by null by replacing it with "."
                     * Null in these cases means unavailable information.
                     */
                    public @Nullable PathSyntax getNullablePathSyntax() {
                        String val = args[point++];
                        if (val.equals("."))
                            return null;
                        return PathSyntax.compile(val);
                    }

                    /**
                     * Gets a PathSyntax.
                     */
                    public @NonNull PathSyntax getPathSyntax() {
                        return PathSyntax.compile(args[point++]);
                    }

                    @Override
                    public SchemaElement get() {
                        final String text = args[point++];
                        if (text.equals("roint=")) {
                            int n = Integer.parseInt(args[point++]);
                            return new ROIntegerSchemaElement(app, n);
                        }
                        if (text.equals("int=")) {
                            int n = Integer.parseInt(args[point++]);
                            return new IntegerSchemaElement(app, n);
                        }
                        if (text.equals("int+0=")) {
                            int n = Integer.parseInt(args[point++]);
                            return new LowerBoundIntegerSchemaElement(app, 0, n);
                        }
                        if (text.equals("int+1=")) {
                            int n = Integer.parseInt(args[point++]);
                            return new LowerBoundIntegerSchemaElement(app, 1, n);
                        }
                        if (text.equals("float="))
                            return new FloatSchemaElement(app, args[point++], false);
                        if (text.equals("jnum="))
                            return new FloatSchemaElement(app, args[point++], true);
                        // To translate, or not to? Unfortunately these can point at files.
                        // (later) However, context makes it obvious
                        if (text.equals("string=")) {
                            String esc = args[point++];
                            return new StringSchemaElement(app, TXDB.get(outerContext, esc), '\"');
                        }
                        // Before you go using these - They are based on *visual* length, and are not hard limits.
                        if (text.equals("stringLen")) {
                            int l = Integer.parseInt(args[point++]);
                            return new StringLenSchemaElement(app, "", l);
                        }
                        if (text.equals("stringLen=")) {
                            String esc = args[point++];
                            int l = Integer.parseInt(args[point++]);
                            return new StringLenSchemaElement(app, TXDB.get(outerContext, esc), l);
                        }

                        //
                        if (text.equals("hwnd")) {
                            // These need their own translation mechanism
                            PathSyntax a = getNullablePathSyntax();
                            return new HWNDSchemaElement(app, a, args[point++]);
                        }
                        if (text.equals("hide")) {
                            SchemaElement hide = get();
                            return new HiddenSchemaElement(hide, new IFunction<IRIO, Boolean>() {
                                @Override
                                public Boolean apply(IRIO rubyIO) {
                                    return false;
                                }
                            });
                        }
                        if (text.equals("condHide") || text.equals("condHide!")) {
                            final PathSyntax path = getPathSyntax();
                            SchemaElement hide = get();
                            return new HiddenSchemaElement(hide, new IFunction<IRIO, Boolean>() {
                                @Override
                                public Boolean apply(IRIO rubyIO) {
                                    return path.get(rubyIO).getType() == (text.endsWith("!") ? 'F' : 'T');
                                }
                            });
                        }
                        if (text.equals("path") || text.equals("pathN")) {
                            PathSyntax path = getPathSyntax();
                            String txt = "";
                            if (!text.endsWith("N")) {
                                txt = TXDB.get(outerContext, path.decompiled);
                            } else {
                                txt = TXDB.getExUnderscore(outerContext, args[point++]);
                                if (txt.equals("_"))
                                    txt = null;
                            }
                            SchemaElement hide = get();
                            return new PathSchemaElement(path, txt, hide, false);
                        }
                        if (text.equals("optP") || text.equals("optPN")) {
                            PathSyntax path = getPathSyntax();
                            String txt = "";
                            if (!text.endsWith("N")) {
                                txt = TXDB.get(outerContext, path.decompiled);
                            } else {
                                txt = TXDB.getExUnderscore(outerContext, args[point++]);
                                if (txt.equals("_"))
                                    txt = null;
                            }
                            SchemaElement hide = get();
                            return new PathSchemaElement(path, txt, hide, true);
                        }

                        // CS means "control indent if allowed"
                        // MS means "never control indent"
                        if (text.equals("arrayCS")) {
                            SchemaElement s1, s2;
                            s1 = get();
                            s2 = get();
                            String a = args[point++];
                            return new EventCommandArraySchemaElement(app, s1, s2, getCMDB(a), allowControlOfEventCommandIndent);
                        }
                        if (text.equals("arrayMS")) {
                            SchemaElement s1, s2;
                            s1 = get();
                            s2 = get();
                            String a = args[point++];
                            return new EventCommandArraySchemaElement(app, s1, s2, getCMDB(a), false);
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
                                    return new StandardArraySchemaElement(app, get(), n, false, 0, iai, enu);
                                } else {
                                    return new StandardArraySchemaElement(app, get(), n, false, 0, iai);
                                }
                            } else if (ending.equals("IdX")) {
                                int x = Integer.parseInt(args[point++]);
                                int n = Integer.parseInt(args[point++]);
                                if (n == 0)
                                    n = -1;
                                if (enu != null) {
                                    return new StandardArraySchemaElement(app, get(), n, false, x, iai, enu);
                                } else {
                                    return new StandardArraySchemaElement(app, get(), n, false, x, iai);
                                }
                            } else if (ending.equals("AL1")) {
                                if (enu != null) {
                                    return new StandardArraySchemaElement(app, get(), -1, true, 0, iai, enu);
                                } else {
                                    return new StandardArraySchemaElement(app, get(), -1, true, 0, iai);
                                }
                            } else if (ending.equals("AX1")) {
                                if (enu != null) {
                                    return new ArbIndexedArraySchemaElement(app, get(), 1, 1, -1, iai, enu);
                                } else {
                                    return new ArbIndexedArraySchemaElement(app, get(), 1, 1, -1, iai);
                                }
                            } else if (ending.equals("Ix1")) {
                                if (enu != null) {
                                    return new ArbIndexedArraySchemaElement(app, get(), 1, -1, -1, iai, enu);
                                } else {
                                    return new ArbIndexedArraySchemaElement(app, get(), 1, -1, -1, iai);
                                }
                            } else if (ending.equals("IxN")) {
                                int ofx = Integer.parseInt(args[point++]);
                                int sz = Integer.parseInt(args[point++]);
                                if (sz == 0)
                                    sz = -1;
                                if (enu != null) {
                                    throw new RuntimeException("Incompatible with enumerations!");
                                } else {
                                    return new ArbIndexedArraySchemaElement(app, get(), ofx, -1, sz, iai);
                                }
                            } else {
                                throw new RuntimeException("Cannot handle array ending " + ending);
                            }
                        }
                        if (text.equals("DA{")) {
                            PathSyntax disambiguatorIndex = getPathSyntax();
                            SchemaElement backup = get();
                            HashMap<String, SchemaElement> disambiguations = new HashMap<String, SchemaElement>();
                            while (!args[point].equals("}"))
                                disambiguations.put(args[point++], get());
                            disambiguations.put("", backup);
                            return new DisambiguatorSchemaElement(app, disambiguatorIndex, disambiguations);
                        }
                        if (text.equals("lengthAdjust") || text.equals("lengthAdjustDef")) {
                            String text2 = args[point++];
                            int len = Integer.parseInt(args[point++]);
                            String cond = "{@[@Interp.lang-Common-arrayLen]|" + len + "|1|0}";
                            SchemaElement reinit = new StandardArraySchemaElement(app, new OpaqueSchemaElement(app), len, 0, 0, null);
                            return new InitButtonSchemaElement(TXDB.get(outerContext, text2), cond, reinit, false, text.equals("lengthAdjustDef"));
                        }
                        if (text.equals("initButton")) {
                            String text2 = args[point++];
                            String cond = args[point++];
                            SchemaElement reinit = get();
                            return new InitButtonSchemaElement(TXDB.get(outerContext, text2), cond, reinit, true, false);
                        }
                        /*
                         * Command buffers are assembled by putting entries into commandBufferNames (which is ValueSyntax, Text),
                         *  and into commandBufferSchemas (which is DisambiguatorSyntax, Schema)
                         */
                        if (text.equals("flushCommandBuffer")) {
                            // time to flush it!
                            PathSyntax disambiguationIVar = getPathSyntax();
                            setSDBEntry(args[point++], new EnumSchemaElement(app, commandBufferNames, new RubyIO().setFX(0), EntryMode.INT, TXDB.get("Code")));
                            HashMap<String, SchemaElement> baseSE = commandBufferSchemas;
                            commandBufferNames = new HashMap<String, String>();
                            commandBufferSchemas = new HashMap<String, SchemaElement>();
                            return new DisambiguatorSchemaElement(app, disambiguationIVar, baseSE);
                        }
                        if (text.equals("flushCommandBufferStr")) {
                            // time to flush it!
                            PathSyntax disambiguationIVar = getPathSyntax();
                            setSDBEntry(args[point++], new EnumSchemaElement(app, commandBufferNames, new RubyIO().setString("", true), EntryMode.STR, TXDB.get("Code")));
                            HashMap<String, SchemaElement> baseSE = commandBufferSchemas;
                            commandBufferNames = new HashMap<String, String>();
                            commandBufferSchemas = new HashMap<String, SchemaElement>();
                            return new DisambiguatorSchemaElement(app, disambiguationIVar, baseSE);
                        }
                        if (text.equals("hash")) {
                            SchemaElement k = get();
                            return new HashSchemaElement(app, k, get(), false);
                        }
                        if (text.equals("hashFlex")) {
                            SchemaElement k = get();
                            return new HashSchemaElement(app, k, get(), true);
                        }
                        if (text.equals("hashObject") || text.equals("hashObjectInner")) {
                            // This never got used by anything, so it's set as always-v1p1
                            LinkedList<RubyIO> validKeys = new LinkedList<RubyIO>();
                            while (point < args.length)
                                validKeys.add(ValueSyntax.decode(args[point++]));
                            return new HashObjectSchemaElement(app, validKeys, text.equals("hashObjectInner"));
                        }
                        if (text.equals("subwindow"))
                            return new SubwindowSchemaElement(get());
                        // subwindow: This\_Is\_A\_Test
                        if (text.equals("subwindow:")) {
                            String text2 = args[point++];
                            if (text2.startsWith("@")) {
                                final String textFinal = text2.substring(1);
                                return new SubwindowSchemaElement(get(), new IFunction<IRIO, String>() {
                                    @Override
                                    public String apply(IRIO rubyIO) {
                                        return TXDB.nameDB.get("Interp." + textFinal).apply(rubyIO);
                                    }
                                });
                            } else {
                                return new SubwindowSchemaElement(get(), getFunctionToReturn(TXDB.get(outerContext, text2)));
                            }
                        }

                        if (text.equals("{")) {
                            // Aggregate
                            AggregateSchemaElement subag = new AggregateSchemaElement(app, new SchemaElement[] {});
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
                            return new TypeChangerSchemaElement(app, strs.toArray(new String[0]), scms.toArray(new SchemaElement[0]));
                        }

                        // -- These two must be in this order.
                        if (text.startsWith("]?")) {
                            // yay for... well, semi-consistency!
                            String a = text.substring(2);
                            String b = TXDB.getExUnderscore(outerContext, args[point++]);
                            String o = TXDB.get(outerContext, args[point++]);
                            return new ArrayElementSchemaElement(app, Integer.parseInt(a), b, get(), o, false);
                        }
                        if (text.startsWith("]")) {
                            // yay for consistency!
                            String a = text.substring(1);
                            String b = TXDB.getExUnderscore(outerContext, args[point++]);
                            return new ArrayElementSchemaElement(app, Integer.parseInt(a), b, get(), null, false);
                        }
                        // --

                        if (text.equals("}"))
                            return null;

                        // Specialized stuff starts here.
                        // This includes anything of type 'u'.
                        if (text.equals("fileSelector")) {
                            String tx = args[point++];
                            String txHR = FormatSyntax.formatExtended(TXDB.get("Browse #A"), new RubyIO().setString(tx, true));
                            return new SubwindowSchemaElement(new FileSelectorSchemaElement(app, tx, null), getFunctionToReturn(txHR));
                        }
                        if (text.equals("imgSelector")) {
                            String tx = args[point++];
                            String tx2 = args[point++];
                            String txHR = FormatSyntax.formatExtended(TXDB.get("Browse #A"), new RubyIO().setString(tx, true));
                            return new SubwindowSchemaElement(new FileSelectorSchemaElement(app, tx, tx2), getFunctionToReturn(txHR));
                        }
                        if (text.equals("halfsplit")) {
                            SchemaElement a = get();
                            SchemaElement b = get();
                            return new HalfsplitSchemaElement(a, b);
                        }
                        if (text.equals("spriteSelector")) {
                            // C spritesheet[ Select face index... ] FaceSets/ 48 48 4 0 0 48 48 0
                            // +spriteSelector @face_index @face_name FaceSets/
                            final PathSyntax varPath = getPathSyntax();
                            final PathSyntax imgPath = getPathSyntax();
                            final String imgPfx = args[point++];
                            return helpers.makeSpriteSelector(varPath, imgPath, imgPfx);
                        }
                        if (text.equals("r2kTonePicker")) {
                            final PathSyntax rPath = getPathSyntax();
                            final PathSyntax gPath = getPathSyntax();
                            final PathSyntax bPath = getPathSyntax();
                            final PathSyntax sPath = getPathSyntax();
                            return new TonePickerSchemaElement(app, rPath, gPath, bPath, sPath, 100);
                        } else if (text.equals("r2kTonePickerPreview")) {
                            final PathSyntax rPath = getPathSyntax();
                            final PathSyntax gPath = getPathSyntax();
                            final PathSyntax bPath = getPathSyntax();
                            final PathSyntax sPath = getPathSyntax();
                            final PathSyntax iPath = getPathSyntax();
                            final String iPrefix = args[point++];
                            return new TonePickerSchemaElement.Thumbnail(app, rPath, gPath, bPath, sPath, 100, iPath, iPrefix);
                        }
                        if (text.equals("binding")) {
                            String type = args[point++];
                            IMagicalBinder binder = MagicalBinders.getBinderByName(type);
                            if (binder == null)
                                throw new RuntimeException("Unknown binding " + type);
                            return new MagicalBindingSchemaElement(app, binder, get());
                        }
                        if (text.equals("context?")) {
                            // context? <id> <default>
                            final String idx = args[point++];
                            final SchemaElement insideThat = get();
                            return new SchemaElement(app) {
                                @Override
                                public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
                                    return getSchema(path).buildHoldingEditor(target, launcher, path);
                                }

                                private SchemaElement getSchema(SchemaPath path) {
                                    SchemaElement se = path.contextualSchemas.get(idx);
                                    if (se == null)
                                        return insideThat;
                                    return se;
                                }

                                @Override
                                public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
                                    getSchema(path).modifyVal(target, path, setDefault);
                                }
                            };
                        }
                        if (text.equals("contextDictionary")) {
                            // D <name> <base or '.'> <default value> <outer path, including root> <'1' means hash> <inner path> <inner path interpretation id> <element to surround with this>
                            // contextDictionary <ctxId> <default 0 @name rpg_troop_core
                            final String contextName = args[point++];
                            final String base = args[point++];
                            final RubyIO defVal = ValueSyntax.decode(args[point++]);
                            final PathSyntax outer = getPathSyntax();
                            final boolean hash = args[point++].equals("1");
                            final PathSyntax inner = getPathSyntax();
                            final String interpret = args[point++];
                            final SchemaElement insideThat = get();
                            return new SchemaElement(app) {
                                @Override
                                public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
                                    return insideThat.buildHoldingEditor(target, launcher, applySchema(target, path, true));
                                }

                                private SchemaPath applySchema(final IRIO host, SchemaPath path, boolean update) {
                                    EnumSchemaElement sce = new EnumSchemaElement(app, new HashMap<String, String>(), defVal, EntryMode.INT, TXDB.get("ID.")) {
                                        @Override
                                        public void liveUpdate() {
                                            viewOptions.clear();
                                            if (!base.equals(".")) {
                                                EnumSchemaElement baseEnum = (EnumSchemaElement) schemaTrueDatabase.get(base);
                                                viewOptions.addAll(baseEnum.viewOptions);
                                                entryMode = baseEnum.entryMode;
                                                // Default val doesn't get carried over since it gets specced here
                                                buttonText = baseEnum.buttonText;
                                            }
                                            IRIO p = outer.get(host);
                                            if (p != null)
                                                DictionaryUpdaterRunnable.coreLogic(app, viewOptions, inner, null, null, p, hash, interpret);
                                            convertViewToLookup();
                                        }
                                    };
                                    if (update)
                                        sce.liveUpdate();
                                    return path.contextSchema(contextName, sce);
                                }

                                @Override
                                public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
                                    insideThat.modifyVal(target, applySchema(target, path, false), setDefault);
                                }
                            };
                        }
                        if (text.equals("bitfield=")) {
                            int i = Integer.parseInt(args[point++]);
                            LinkedList<String> flags = new LinkedList<String>();
                            while (point < args.length)
                                flags.add(args[point++]);
                            return new BitfieldSchemaElement(app, i, flags.toArray(new String[0]));
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
                                tilesetAllocations = new TSDB(app, args[point++]);
                            PathSyntax iV = getNullablePathSyntax();
                            PathSyntax wV = getNullablePathSyntax();
                            PathSyntax hV = getNullablePathSyntax();

                            IFunction<IRIO, String> iVT = getFunctionToReturn(iV == null ? TXDB.get("Open Table...") : TXDB.get(outerContext, iV.decompiled));

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
                                tcf = new BitfieldTableCellEditor(app, flags.toArray(new String[0]));
                            }
                            @SuppressWarnings("rawtypes")
                            RubyTableSchemaElement r = null;
                            if (eText.equals("tableSTA")) {
                                r = new TilesetAllocTableSchemaElement(app, tilesetAllocations, iV, wV, hV, dc, aW, aH, aI, tcf, defVals);
                            } else if (eText.equals("tableTS")) {
                                r = new TilesetTableSchemaElement(app, iV, wV, hV, dc, aW, aH, aI, tcf, defVals);
                            } else if (eText.equals("table")) {
                                r = new RubyTableSchemaElement<Object>(app, iV, wV, hV, dc, aW, aH, aI, tcf, defVals);
                            } else {
                                throw new RuntimeException("Unknown table type " + text);
                            }
                            r.allowResize &= !disableRsz;
                            return new SubwindowSchemaElement(r, iVT);
                        }
                        if (text.equals("internal_vxaTilesetFlags")) {
                            // Need to work out a nice way to handle this.
                            // For now, kick the problem Java-side so that it can at least be worked on.
                            String[] flags = new String[] {
                                "blockDown", "blockLeft", "blockRight", "blockUp",
                                "'star'", "ladder", "submerge", "'counter'",
                                "poison", "noBoat", "noShip", "noAShip", "[terrainTag;8"
                            };
                            PathSyntax fp = PathSyntax.compile("@flags");
                            return new FancyCategorizedTilesetRubyTableSchemaElement(app, 8192, 1, 1, 1, fp, new int[] {0}, new BitfieldTableCellEditor(app, flags));
                        }
                        if (text.equals("internal_r2kPPPID")) {
                            SchemaElement se = get();
                            return helpers.makePicPointerPatchID(getSDBEntry("var_id"), se);
                        }
                        if (text.equals("internal_r2kPPPV")) {
                            String txt = TXDB.get(outerContext, args[point++]);
                            SchemaElement se = get();
                            return helpers.makePicPointerPatchVar(getSDBEntry("var_id"), txt, se);
                        }
                        if (text.equals("CTNative"))
                            return new CTNativeSchemaElement(app, args[point++]);

                        if (text.equals("mapPositionHelper")) {
                            PathSyntax a = getNullablePathSyntax();
                            PathSyntax b = getPathSyntax();
                            PathSyntax c = getPathSyntax();
                            return new MapPositionHelperSchemaElement(app, a, b, c);
                        }
                        if (text.equals("eventTileHelper")) {
                            PathSyntax c = getPathSyntax();
                            PathSyntax d = getPathSyntax();
                            String a = args[point++];
                            String b = args[point++];
                            return new SubwindowSchemaElement(new EventTileReplacerSchemaElement(new TSDB(app, b), Integer.parseInt(a), c, d), getFunctionToReturn(TXDB.get("Select Tile Graphic...")));
                        }
                        if (text.equals("windowTitleAttachment")) {
                            String txt = TXDB.get(outerContext, args[point++]);
                            return new WindowTitleAttachmentSchemaElement(app, txt);
                        }
                        if (text.equals("soundPlayer")) {
                            String a = args[point++];
                            return new SoundPlayerSchemaElement(app, a, PathSyntax.compile(""), null, null, null);
                        }
                        if (text.equals("soundPlayerComplex")) {
                            String prefix = args[point++];
                            PathSyntax namePath = getPathSyntax();
                            PathSyntax volumePath = getNullablePathSyntax();
                            PathSyntax tempoPath = getNullablePathSyntax();
                            PathSyntax balancePath = getNullablePathSyntax();
                            return new SoundPlayerSchemaElement(app, prefix, namePath, volumePath, tempoPath, balancePath);
                        }
                        // -- If all else fails, it's an ID to be looked up. --
                        return getSDBEntry(text);
                    }
                }.get();
            }

            @Override
            public void execCmd(char c, final String[] args) throws IOException {
                if (c == 'a') {
                    if (!schemaDatabase.containsKey(args[0]))
                        throw new RuntimeException("Bad Schema Database: 'a' used to expect item " + args[0] + " that didn't exist.");
                } else if (c == ':') {
                    if (args.length == 1) {
                        workingObj = new AggregateSchemaElement(app, new SchemaElement[]{});
                        outerContext = fPfx + "/" + args[0];
                        setSDBEntry(args[0], new ObjectClassSchemaElement(args[0], workingObj, 'o'));
                    } else {
                        String backup = outerContext;
                        outerContext = args[0];
                        setSDBEntry(args[0], new ObjectClassSchemaElement(args[0], handleChain(args, 1), 'o'));
                        outerContext = backup;
                    }
                } else if (c == '.') {
                    workingObj = new AggregateSchemaElement(app, new SchemaElement[] {});
                    outerContext = fPfx + "/" + args[0];
                    setSDBEntry(args[0], workingObj);
                } else if (c == '@') {
                    PathSyntax t = PathSyntax.compile("@" + PathSyntax.poundEscape(args[0]));
                    // Note: the unescaping happens in the Path
                    workingObj.aggregate.add(new PathSchemaElement(t, TXDB.get(outerContext, t.decompiled), handleChain(args, 1), false));
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
                    PathSyntax t = PathSyntax.compile(":{" + PathSyntax.poundEscape(intA0));
                    workingObj.aggregate.add(new PathSchemaElement(t, TXDB.get(outerContext, args[1]), handleChain(args, 2), opt));
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
                    EnumSchemaElement e = new EnumSchemaElement(app, options, new RubyIO().setFX(defVal), EntryMode.INT, TXDB.get("Integer"));
                    setSDBEntry(args[0], e);
                } else if (c == 's') {
                    // Symbols
                    HashMap<String, String> options = new HashMap<String, String>();
                    for (int i = 1; i < args.length; i++)
                        options.put(":" + args[i], TXDB.get(args[0], args[i]));

                    EnumSchemaElement ese = new EnumSchemaElement(app, options, ValueSyntax.decode(":" + args[1]), EntryMode.SYM, TXDB.get("Symbol"));
                    setSDBEntry(args[0], ese);
                } else if (c == 'E') {
                    HashMap<String, String> options = new HashMap<String, String>();
                    for (int i = 2; i < args.length; i += 2) {
                        String ctx = "SDB@" + args[0];
                        options.put(args[i], TXDB.get(ctx, args[i + 1]));
                    }
                    EnumSchemaElement e = new EnumSchemaElement(app, options, ValueSyntax.decode(args[2]), EntryMode.INT, TXDB.get(args[0], args[1]));
                    setSDBEntry(args[0], e);
                } else if (c == 'M') {
                    // Make a proxy (because we change the backing element all the time)
                    ensureSDBProxy(args[2]);
                    mergeRunnables.add(new Runnable() {
                        @Override
                        public void run() {
                            // Proxies are bad for this.
                            EnumSchemaElement mergeA = (EnumSchemaElement) schemaTrueDatabase.get(args[0]);
                            EnumSchemaElement mergeB = (EnumSchemaElement) schemaTrueDatabase.get(args[1]);
                            HashMap<String, UIEnumChoice.Option> finalMap = new HashMap<String, UIEnumChoice.Option>();
                            finalMap.putAll(mergeA.lookupOptions);
                            finalMap.putAll(mergeB.lookupOptions);
                            SchemaElement ise = new EnumSchemaElement(app, finalMap.values(), mergeB.defaultVal, mergeB.entryMode, mergeB.buttonText);
                            setSDBEntry(args[2], ise);
                        }
                    });
                } else if (c == ']') {
                    workingObj.aggregate.add(new ArrayElementSchemaElement(app, Integer.parseInt(args[0]), TXDB.get(outerContext, args[1]), handleChain(args, 2), null, false));
                } else if (c == 'i') {
                    readFile(args[0]);
                } else if (c == 'D') {
                    // D <name> <default value> <outer path, including root> <'1' means hash> <inner path> [interpretation ID / empty string] [data schema]
                    final String[] root = PathSyntax.breakToken(args[2]);
                    String interpret = null;
                    SchemaElement dataSchema = null;
                    if (args.length == 7) {
                        interpret = args[5];
                        if (interpret.length() == 0)
                            interpret = null;
                        dataSchema = getSDBEntry(args[6]);
                    } else if (args.length == 6) {
                        interpret = args[5];
                    } else if (args.length != 5) {
                        throw new RuntimeException("Expects D <name> <default value> <outer path, including root> <'1' means hash> <inner path> [interpretation ID / empty string] [data schema]");
                    }
                    ensureSDBProxy(args[0]);
                    dictionaryUpdaterRunnables.add(new DictionaryUpdaterRunnable(app, args[0], root[0], PathSyntax.compile(root[1]), args[3].equals("1"), PathSyntax.compile(args[4]), Integer.parseInt(args[1]), interpret, dataSchema));
                } else if (c == 'd') {
                    // OLD SYSTEM
                    System.err.println("'d'-format is old. It'll stay around but won't get updated. Use 'D'-format instead. " + args[0]);
                    // Cause a proxy to be generated. (NOTE: This *must* be referenced via nocache proxy!)
                    ensureSDBProxy(args[0]);
                    dictionaryUpdaterRunnables.add(new DictionaryUpdaterRunnable(app, args[0], args[2], new IFunction<IRIO, IRIO>() {
                        @Override
                        public IRIO apply(IRIO rubyIO) {
                            for (int i = 3; i < args.length; i++)
                                rubyIO = rubyIO.getIVar(args[i]);
                            return rubyIO;
                        }
                    }, false, null, Integer.parseInt(args[1]), null, null));
                } else if (c == 'A') {
                    // This is needed so the engine actually understands which autotiles map to what
                    int p = 0;
                    app.autoTiles = new ATDB[args.length / 2];
                    for (int i = 0; i < args.length; i += 2) {
                        app.autoTiles[p] = new ATDB(args[i]);
                        // This is needed to make actual autotile *placement* work.
                        // In theory, it's independent of the AutoTiles setup,
                        //  so long as the AutoTiles setup's using the same sprite-sheets.
                        // In practice, it's only been tested with the default AutoTiles.txt setup.
                        if (!args[i + 1].equals("."))
                            app.autoTiles[p].calculateInverseMap(args[i + 1]);
                        p++;
                    }
                } else if (c == 'C') {
                    if (args[0].equals("md")) {
                        // This is getting changed over for sanity reasons.
                        // It's not used right now, so it's safe to move it over.
                        // The new syntax is Cmd <DisambiguatorSyntax> ["Name!"]
                        outerContext = fPfx + "/commandBuffer";
                        workingObj = new AggregateSchemaElement(app, new SchemaElement[] {});
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
                            schemaDatabase.put("indent", new ROIntegerSchemaElement(app, 0));
                        } else {
                            schemaDatabase.put("indent", new IntegerSchemaElement(app, 0));
                        }
                        schemaTrueDatabase.put("indent", schemaDatabase.get("indent"));
                    }
                    if (args[0].equals("objectDB"))
                        app.odbBackend = args[1];
                    if (args[0].equals("recommendMkdir"))
                        recommendedDirs.add(args[1]);
                    if (args[0].equals("dataPath"))
                        app.dataPath = args[1];
                    if (args[0].equals("dataExt"))
                        app.dataExt = args[1];
                    if (args[0].equals("versionId"))
                        app.sysBackend = args[1];
                    if (args[0].equals("defaultCB")) {
                        workingObj = new AggregateSchemaElement(app, new SchemaElement[] {});
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
                        workingObj.aggregate.add(new GenposSchemaElement(app, args[1], aS, bS, cS, dS, Integer.parseInt(args[6])));
                    }
                    if (args[0].equals("magicR2kSystemDefaults")) {
                        // Really special schema
                        workingObj.aggregate.add(new R2kSystemDefaultsInstallerSchemaElement(app, Integer.parseInt(args[1])));
                    }
                    if (args[0].equals("name") || args[0].equals("logic")) {
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
                        final String textF = args[0].equals("name") ? TXDB.get(fPfx + "/" + args[1], text) : text;

                        final PathSyntax[] argumentsPS = new PathSyntax[arguments.size()];
                        int idx = 0;
                        for (String ps : arguments)
                            argumentsPS[idx++] = PathSyntax.compile(ps);

                        TXDB.nameDB.put(args[1], new IFunction<IRIO, String>() {
                            @Override
                            public String apply(IRIO rubyIO) {
                                LinkedList<IRIO> parameters = new LinkedList<IRIO>();
                                for (PathSyntax arg : argumentsPS) {
                                    IRIO res = arg.get(rubyIO);
                                    if (res == null)
                                        break;
                                    parameters.add(res);
                                }
                                return FormatSyntax.formatNameExtended(textF, rubyIO, parameters.toArray(new IRIO[0]), null);
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
        CMDB r = new CMDB(this, arg);
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
        SchemaElement ise = new NameProxySchemaElement(app, text, true);
        schemaDatabase.put(text, ise);
        return ise;
    }

    // Use if and only if you deliberately need the changing nature of a proxy (this disables the cache)
    public void ensureSDBProxy(String text) {
        if (schemaDatabase.containsKey(text)) {
            // Implicitly asserts that this is a proxy.
            ((NameProxySchemaElement) schemaDatabase.get(text)).useCache = false;
        } else {
            NameProxySchemaElement npse = new NameProxySchemaElement(app, text, false);
            schemaDatabase.put(text, npse);
        }
    }

    public LinkedList<ObjectInfo> listFileDefs() {
        LinkedList<ObjectInfo> fd = new LinkedList<ObjectInfo>();
        for (String s : schemaDatabase.keySet())
            if (s.startsWith("File."))
                fd.add(new ObjectInfo(app, s.substring(5), s));
        return fd;
    }

    public @Nullable SchemaElement findSchemaFor(@NonNull IObjectBackend.ILoadedObject ilo) {
        return findSchemaFor(AppMain.objectDB.getIdByObject(ilo), ilo.getObject());
    }

    public @Nullable SchemaElement findSchemaFor(@Nullable String objId, @NonNull IRIO object) {
        if (objId != null)
            if (AppMain.schemas.hasSDBEntry("File." + objId))
                return AppMain.schemas.getSDBEntry("File." + objId);
        if (object.getType() == 'o')
            return AppMain.schemas.getSDBEntry(object.getSymbol());
        return null;
    }

    public void startupSanitizeDictionaries() {
        for (DictionaryUpdaterRunnable dur : dictionaryUpdaterRunnables)
            dur.sanitize();
        for (Runnable merge : mergeRunnables)
            merge.run();
    }

    public void updateDictionaries(IObjectBackend.ILoadedObject map) {
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

    private class NameProxySchemaElement extends SchemaElement implements IProxySchemaElement {
        private final String tx;
        private boolean useCache;
        private SchemaElement cache = null;

        public NameProxySchemaElement(App app, String text, boolean useCach) {
            super(app);
            tx = text;
            useCache = useCach;
        }

        @Override
        public SchemaElement getEntry() {
            if (cache != null)
                return cache;
            SchemaElement r = schemaTrueDatabase.get(tx);
            if (r == null)
                throw new RuntimeException("Schema used " + tx + ", but it didn't exist when invoked.");
            if (useCache)
                cache = r;
            return r;
        }

        @Override
        public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
            return getEntry().buildHoldingEditor(target, launcher, path);
        }

        @Override
        public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
            getEntry().modifyVal(target, path, setDefault);
        }
    }
}
