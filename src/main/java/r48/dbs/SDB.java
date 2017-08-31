/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

import gabien.IGrInDriver;
import gabien.IImage;
import gabien.ui.IFunction;
import gabien.ui.ISupplier;
import r48.AppMain;
import r48.DictionaryUpdaterRunnable;
import r48.RubyIO;
import r48.schema.*;
import r48.schema.arrays.ArbIndexedArraySchemaElement;
import r48.schema.arrays.StandardArraySchemaElement;
import r48.schema.displays.EPGDisplaySchemaElement;
import r48.schema.integers.IntBooleanSchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.integers.LowerBoundIntegerSchemaElement;
import r48.schema.integers.ROIntegerSchemaElement;
import r48.schema.specialized.*;
import r48.schema.specialized.cmgb.EventCommandArraySchemaElement;
import r48.schema.specialized.tbleditors.BitfieldTableCellEditor;
import r48.schema.specialized.tbleditors.DefaultTableCellEditor;
import r48.schema.specialized.tbleditors.ITableCellEditor;
import r48.ui.ISpritesheetProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The ultimate database, more or less, since this houses the data definitions needed to do things like edit Events.
 * Not required for reading maps.
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
    // Spritesheet definitions are quite opaque lists of numbers defining how a grid sheet should appear. See spriteSelector.
    private HashMap<String, IFunction<String, ISpritesheetProvider>> spritesheets = new HashMap<String, IFunction<String, ISpritesheetProvider>>();
    private HashMap<String, String> spritesheetN = new HashMap<String, String>();

    public SDB() {
        schemaDatabase.put("nil", new OpaqueSchemaElement());
        schemaDatabase.put("int", new IntegerSchemaElement(0));
        schemaDatabase.put("roint", new ROIntegerSchemaElement(0));
        schemaDatabase.put("int+0", new LowerBoundIntegerSchemaElement(0, 0));
        schemaDatabase.put("int+1", new LowerBoundIntegerSchemaElement(1, 1));
        schemaDatabase.put("index", new AMAISchemaElement());
        schemaDatabase.put("float", new FloatSchemaElement("0"));
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

            HashMap<Integer, String> commandBufferNames = new HashMap<Integer, String>();
            HashMap<Integer, SchemaElement> commandBufferSchemas = new HashMap<Integer, SchemaElement>();

            String outerContext = fPfx + "/NONE";

            @Override
            public void newObj(int objId, String objName) {
                outerContext = fPfx + "/commandBuffer";
                commandBufferNames.put(objId, TXDB.get(outerContext, objName));
                workingObj = new AggregateSchemaElement(new SchemaElement[] {});
                commandBufferSchemas.put(objId, workingObj);
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
                            return new FloatSchemaElement(args[point++]);
                        // To translate, or not to? Unfortunately these can point at files.
                        // (later) However, context makes it obvious
                        if (text.equals("string="))
                            return new StringSchemaElement(TXDB.get(outerContext, args[point++]), '\"');
                        if (text.equals("string_="))
                            return new StringSchemaElement(TXDB.get(outerContext, args[point++]).replace('_', ' '), '\"');
                        // Before you go using these - They are based on *visual* length, and are not hard limits.
                        if (text.equals("stringLen")) {
                            int l = Integer.parseInt(args[point++]);
                            return new StringLenSchemaElement("", l);
                        }
                        if (text.equals("stringLen=")) {
                            String txt = args[point++];
                            int l = Integer.parseInt(args[point++]);
                            return new StringLenSchemaElement(TXDB.get(outerContext, txt), l);
                        }
                        if (text.equals("stringLen_=")) {
                            String txt = args[point++];
                            int l = Integer.parseInt(args[point++]);
                            return new StringLenSchemaElement(TXDB.get(outerContext, txt).replace('_', ' '), l);
                        }
                        //
                        if (text.equals("hwnd")) {
                            // These need their own translation mechanism
                            String a = args[point++];
                            if (a.equals("."))
                                a = null;
                            return new HWNDSchemaElement(a, args[point++]);
                        }
                        if (text.equals("optIV")) {
                            String base = args[point++];
                            String a = TXDB.get(outerContext, base);
                            return new IVarSchemaElement(base, a, get(), true);
                        }
                        if (text.equals("iV")) {
                            String base = args[point++];
                            String a = TXDB.get(outerContext, base);
                            return new IVarSchemaElement(base, a, get(), false);
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
                        if (text.equals("condHide")) {
                            final String path = args[point++];
                            SchemaElement hide = get();
                            return new HiddenSchemaElement(hide, new IFunction<RubyIO, Boolean>() {
                                @Override
                                public Boolean apply(RubyIO rubyIO) {
                                    return PathSyntax.parse(rubyIO, path).type == 'T';
                                }
                            });
                        }
                        if (text.equals("path")) {
                            String path = args[point++];
                            SchemaElement hide = get();
                            return new PathSchemaElement(path, TXDB.get(outerContext, path), hide);
                        }
                        if (text.equals("array")) {
                            int n = Integer.parseInt(args[point++]);
                            return new StandardArraySchemaElement(get(), n, false, 0);
                        }
                        if (text.equals("arrayIdX")) {
                            int x = Integer.parseInt(args[point++]);
                            int n = Integer.parseInt(args[point++]);
                            return new StandardArraySchemaElement(get(), n, false, x);
                        }
                        if (text.equals("arrayAL1"))
                            return new StandardArraySchemaElement(get(), 0, true, 0);
                        if (text.equals("arrayIx1"))
                            return new ArbIndexedArraySchemaElement(get(), 1, 0);
                        if (text.equals("arrayIxN")) {
                            int ofx = Integer.parseInt(args[point++]);
                            int sz = Integer.parseInt(args[point++]);
                            return new ArbIndexedArraySchemaElement(get(), ofx, sz);
                        }
                        if (text.equals("arrayDAM")) {
                            int disambiguatorIndex = Integer.parseInt(args[point++]);
                            SchemaElement disambiguatorType = get();
                            SchemaElement backup = get();
                            HashMap<Integer, SchemaElement> disambiguations = new HashMap<Integer, SchemaElement>();
                            while (point < args.length) {
                                int ind = Integer.parseInt(args[point++]);
                                disambiguations.put(ind, get());
                            }
                            return new ArrayDisambiguatorSchemaElement(disambiguatorIndex, disambiguatorType, backup, disambiguations);
                        }
                        if (text.equals("flushCommandBuffer")) {
                            // time to flush it!
                            String disambiguationIVar = args[point++];
                            setSDBEntry(args[point++], new EnumSchemaElement(commandBufferNames, 0, TXDB.get("Code")));
                            HashMap<Integer, SchemaElement> baseSE = commandBufferSchemas;
                            commandBufferNames = new HashMap<Integer, String>();
                            commandBufferSchemas = new HashMap<Integer, SchemaElement>();
                            return new GenericDisambiguationSchemaElement(disambiguationIVar, baseSE);
                        }
                        if (text.equals("hash")) {
                            SchemaElement k = get();
                            return new HashSchemaElement(k, get());
                        }
                        if (text.equals("subwindow"))
                            return new SubwindowSchemaElement(get());
                        // subwindow[ This Is A Test ]
                        if (text.equals("subwindow[")) {
                            String text2 = args[point++];
                            while (!args[point].equals("]"))
                                text2 += " " + args[point++];
                            point++;
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
                            String txHR = FormatSyntax.formatExtended(TXDB.get("Browse #A"), new RubyIO().setString(tx));
                            return new SubwindowSchemaElement(new FileSelectorSchemaElement(tx), getFunctionToReturn(txHR));
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
                            return makeSpriteSelector(varPath, imgPath, imgPfx);
                        }
                        if (text.equals("r2kTonePicker")) {
                            final String rPath = args[point++];
                            final String gPath = args[point++];
                            final String bPath = args[point++];
                            final String sPath = args[point++];
                            return new TonePickerSchemaElement(rPath, gPath, bPath, sPath);
                        }
                        if (text.startsWith("table")) {
                            String eText = text;
                            boolean hasFlags = eText.endsWith("F");
                            if (hasFlags)
                                eText = eText.substring(0, eText.length() - 1);
                            boolean hasDefault = eText.endsWith("D");
                            if (hasDefault)
                                eText = eText.substring(0, eText.length() - 1);

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
                            if (eText.equals("tableSTA"))
                                return new SubwindowSchemaElement(new TilesetAllocTableSchemaElement(tilesetAllocations, iV, wV, hV, aW, aH, aI, tcf, defVals), iVT);
                            if (eText.equals("tableTS"))
                                return new SubwindowSchemaElement(new TilesetTableSchemaElement(iV, wV, hV, aW, aH, aI, tcf, defVals), iVT);
                            if (eText.equals("table"))
                                return new SubwindowSchemaElement(new RubyTableSchemaElement(iV, wV, hV, aW, aH, aI, tcf, defVals), iVT);
                            throw new RuntimeException("Unknown table type " + text);
                        }
                        if (text.equals("CTNative"))
                            return new CTNativeSchemaElement(args[point++]);

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

                        if (text.equals("mapPositionHelper")) {
                            String a = args[point++];
                            String b = args[point++];
                            String c = args[point++];
                            return new MapPositionHelperSchemaElement(a, b, c);
                        }
                        if (text.equals("eventTileHelper")) {
                            String a = args[point++];
                            String b = args[point++];
                            return new SubwindowSchemaElement(new EventTileReplacerSchemaElement(new TSDB(b), Integer.parseInt(a)), getFunctionToReturn(TXDB.get("Select Tile Graphic...")));
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
                    workingObj = new AggregateSchemaElement(new SchemaElement[] {});
                    outerContext = fPfx + "/" + args[0];
                    setSDBEntry(args[0], new ObjectClassSchemaElement(args[0], workingObj, 'o'));
                } else if (c == '.') {
                    workingObj = new AggregateSchemaElement(new SchemaElement[] {});
                    outerContext = fPfx + "/" + args[0];
                    setSDBEntry(args[0], workingObj);
                } else if (c == '@') {
                    String t = "@" + args[0];
                    workingObj.aggregate.add(new IVarSchemaElement(t, TXDB.get(outerContext, t), handleChain(args, 1), false));
                } else if (c == '+') {
                    workingObj.aggregate.add(handleChain(args, 0));
                } else if (c == '>') {
                    String backup = outerContext;
                    outerContext = args[0];
                    setSDBEntry(args[0], handleChain(args, 1));
                    outerContext = backup;
                } else if (c == 'e') {
                    HashMap<Integer, String> options = new HashMap<Integer, String>();
                    int defVal = 0;
                    for (int i = 1; i < args.length; i += 2) {
                        int k = Integer.parseInt(args[i]);
                        if (i == 1)
                            defVal = k;
                        String ctx = "SDB@" + args[0];
                        options.put(k, TXDB.get(ctx, args[i + 1]));
                    }
                    EnumSchemaElement e = new EnumSchemaElement(options, defVal, TXDB.get("Integer"));
                    setSDBEntry(args[0], e);
                } else if (c == 's') {
                    // Symbols
                    String[] syms = new String[args.length - 1];
                    for (int i = 0; i < syms.length; i++)
                        syms[i] = TXDB.get(args[0], args[i + 1]);
                    setSDBEntry(args[0], new SymEnumSchemaElement(syms));
                } else if (c == 'E') {
                    HashMap<Integer, String> options = new HashMap<Integer, String>();
                    int defVal = 0;
                    for (int i = 2; i < args.length; i += 2) {
                        int k = Integer.parseInt(args[i]);
                        if (i == 2)
                            defVal = k;
                        String ctx = "SDB@" + args[0];
                        options.put(k, TXDB.get(ctx, args[i + 1]));
                    }
                    EnumSchemaElement e = new EnumSchemaElement(options, defVal, TXDB.get(args[0], args[1].replace('_', ' ')));
                    setSDBEntry(args[0], e);
                } else if (c == 'M') {
                    mergeRunnables.add(new Runnable() {
                        @Override
                        public void run() {
                            // Proxies are bad for this.
                            EnumSchemaElement mergeA = (EnumSchemaElement) schemaTrueDatabase.get(args[0]);
                            EnumSchemaElement mergeB = (EnumSchemaElement) schemaTrueDatabase.get(args[1]);
                            HashMap<Integer, String> finalMap = new HashMap<Integer, String>();
                            finalMap.putAll(mergeA.options);
                            finalMap.putAll(mergeB.options);
                            SchemaElement ise = new EnumSchemaElement(finalMap, mergeB.defaultVal, mergeB.buttonText);
                            AppMain.schemas.setSDBEntry(args[2], ise);
                        }
                    });
                } else if (c == ']') {
                    workingObj.aggregate.add(new ArrayElementSchemaElement(Integer.parseInt(args[0]), TXDB.get(outerContext, args[1]), handleChain(args, 2), null, false));
                } else if (c == 'i') {
                    readFile(args[0]);
                } else if (c == 'D') {
                    String root = PathSyntax.breakToken(args[2]);
                    final String remainder = args[2].substring(root.length());
                    dictionaryUpdaterRunnables.add(new DictionaryUpdaterRunnable(args[0], root, new IFunction<RubyIO, RubyIO>() {
                        @Override
                        public RubyIO apply(RubyIO rubyIO) {
                            return PathSyntax.parse(rubyIO, remainder);
                        }
                    }, args[3].equals("1"), new IFunction<RubyIO, RubyIO>() {
                        @Override
                        public RubyIO apply(RubyIO rubyIO) {
                            return PathSyntax.parse(rubyIO, args[4]);
                        }
                    }, Integer.parseInt(args[1])));
                } else if (c == 'd') {
                    dictionaryUpdaterRunnables.add(new DictionaryUpdaterRunnable(args[0], args[2], new IFunction<RubyIO, RubyIO>() {
                        @Override
                        public RubyIO apply(RubyIO rubyIO) {
                            for (int i = 3; i < args.length; i++)
                                rubyIO = rubyIO.getInstVarBySymbol(args[i]);
                            return rubyIO;
                        }
                    }, false, null, Integer.parseInt(args[1])));
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
                    if (args[0].equals("dataPath"))
                        AppMain.dataPath = args[1];
                    if (args[0].equals("dataExt"))
                        AppMain.dataExt = args[1];
                    if (args[0].equals("versionId"))
                        AppMain.sysBackend = args[1];
                    if (args[0].equals("defaultCB")) {
                        workingObj = new AggregateSchemaElement(new SchemaElement[] {});
                        commandBufferSchemas.put(-1, workingObj);
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
                                    RubyIO res = PathSyntax.parse(rubyIO, arg);
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
                        point++;

                        final String imgPfx = args[point];
                        spritesheetN.put(args[point], TXDB.get(args[point] + "sprites", text2));
                        if (args[point + 1].equals("r2kCharacter")) {
                            spritesheets.put(args[point], new IFunction<String, ISpritesheetProvider>() {
                                @Override
                                public ISpritesheetProvider apply(String imgTxt) {
                                    final boolean extended = imgTxt.startsWith("$");
                                    int effectiveW = 288;
                                    int effectiveH = 256;
                                    final IImage img = AppMain.stuffRendererIndependent.imageLoader.getImage(imgPfx + imgTxt, false);
                                    if (extended) {
                                        // EasyRPG Extended Mode
                                        effectiveW = img.getWidth();
                                        effectiveH = img.getHeight();
                                    }
                                    final int useW = effectiveW / 12;
                                    final int useH = effectiveH / 8;

                                    final int cellW = useW * 3;
                                    final int cellH = useH * 4;
                                    final int useX = useW;
                                    final int useY = useH * 2;
                                    final int rowCells = 4;
                                    return createSpritesheetProviderCore(imgTxt, img, useW, useH, rowCells, cellW, cellH, useX, useY, -1);
                                }
                            });
                        } else if (args[point + 1].equals("vxaCharacter")) {
                            spritesheets.put(args[point], new IFunction<String, ISpritesheetProvider>() {
                                @Override
                                public ISpritesheetProvider apply(String imgTxt) {
                                    final IImage img = AppMain.stuffRendererIndependent.imageLoader.getImage(imgPfx + imgTxt, false);
                                    int sprW = img.getWidth() / 12;
                                    int sprH = img.getHeight() / 8;
                                    int cellW = sprW;
                                    int cellH = sprH;
                                    int ovr = -1;
                                    if (imgTxt.startsWith("!$") || imgTxt.startsWith("$")) {
                                        // Character index doesn't work on these
                                        sprW = img.getWidth() / 3;
                                        sprH = img.getHeight() / 4;
                                        cellW = 0;
                                        cellH = 0;
                                        ovr = 1;
                                    }
                                    int useX = sprW;
                                    int useY = 0;
                                    return createSpritesheetProviderCore(imgTxt, img, sprW, sprH, 4, cellW, cellH, useX, useY, ovr);
                                }
                            });
                        } else {
                            final int cellW = Integer.parseInt(args[point + 1]);
                            final int cellH = Integer.parseInt(args[point + 2]);
                            final int rowCells = Integer.parseInt(args[point + 3]);
                            final int useX = Integer.parseInt(args[point + 4]);
                            final int useY = Integer.parseInt(args[point + 5]);
                            final int useW = Integer.parseInt(args[point + 6]);
                            final int useH = Integer.parseInt(args[point + 7]);
                            spritesheets.put(args[point], new IFunction<String, ISpritesheetProvider>() {
                                @Override
                                public ISpritesheetProvider apply(final String imgTxt) {
                                    final IImage img = AppMain.stuffRendererIndependent.imageLoader.getImage(imgPfx + imgTxt, false);
                                    return createSpritesheetProviderCore(imgTxt, img, useW, useH, rowCells, cellW, cellH, useX, useY, -1);
                                }
                            });
                        }
                    }
                } else if (c != ' ') {
                    for (String arg : args)
                        System.err.print(arg + " ");
                    System.err.println("(The command " + c + " in the SDB is not supported.)");
                }
            }
        });
    }

    private ISpritesheetProvider createSpritesheetProviderCore(final String imgTxt, final IImage img, final int useW, final int useH, final int rowCells, final int cellW, final int cellH, final int useX, final int useY, final int countOvr) {
        return new ISpritesheetProvider() {
            @Override
            public int itemWidth() {
                return useW;
            }

            @Override
            public int itemHeight() {
                return useH;
            }

            @Override
            public int itemCount() {
                // Use this to inform the user of image issues
                if (imgTxt.equals(""))
                    AppMain.launchDialog(TXDB.get("The image wasn't specified."));
                if (countOvr != -1)
                    return countOvr;
                return ((img.getHeight() + (cellH - 1)) / cellH) * rowCells;
            }

            @Override
            public int mapValToIdx(int itemVal) {
                return itemVal;
            }

            @Override
            public int mapIdxToVal(int idx) {
                return idx;
            }

            @Override
            public void drawItem(int t, int x, int y, IGrInDriver igd) {
                int row = t / rowCells;
                t %= rowCells;
                igd.blitImage((t * cellW) + useX, (row * cellH) + useY, useW, useH, x, y, img);
            }
        };
    }

    public SchemaElement makeSpriteSelector(final String varPath, final String imgPath, final String imgPfx) {
        final IFunction<String, ISpritesheetProvider> args2 = spritesheets.get(imgPfx);
        return new SpritesheetCoreSchemaElement(spritesheetN.get(imgPfx), 0, new IFunction<RubyIO, RubyIO>() {
            @Override
            public RubyIO apply(RubyIO rubyIO) {
                return PathSyntax.parse(rubyIO, varPath);
            }
        }, new IFunction<RubyIO, ISpritesheetProvider>() {
            @Override
            public ISpritesheetProvider apply(RubyIO rubyIO) {
                return args2.apply(PathSyntax.parse(rubyIO, imgPath).decString());
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
        for (DictionaryUpdaterRunnable dur : dictionaryUpdaterRunnables)
            dur.actIfRequired(map);
        for (Runnable merge : mergeRunnables)
            merge.run();
    }

    public void kickAllDictionariesForMapChange() {
        for (DictionaryUpdaterRunnable dur : dictionaryUpdaterRunnables)
            dur.run();
    }
}
